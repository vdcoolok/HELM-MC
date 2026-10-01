# Path finding and walking

This is the part of HELM that finds a route across the world and then walks it.

## goto

### What it is

`$goto <x> <y> <z>` works out a route to a block and walks it.

```
$goto 120 64 -35
```

Short forms: `$g`, `$go`, `$to`.

### How it behaves

The whole route is worked out before the first step. The reply is one of:

- `Path found: N steps.` when the goal was reached
- `Partial path: N steps so far.` when the goal was not reachable, so it will
  walk as far as it can and then stop
- `No path to x y z.` when there is no route at all, which is a failure and
  nothing is drawn

The search is timed. It gives up if it runs past its budget without making
progress, so a goal across an ocean fails in a reasonable time instead of
freezing the game. The clock is read once every 64 nodes, not on every node, so
timing costs almost nothing.

Three budgets apply, all adjustable under `path`:

- `path.primaryTimeoutMillis` applies until the search has actually moved more
  than five blocks from where it started. A search wedged in a hole gets the
  short budget.
- `path.failureTimeoutMillis` is the hard ceiling and applies the whole time.
- `path.maxChunkBorderFetch` caps how many moves may land in a chunk the client
  has not loaded. Reaching the cap ends the search, which is what stops a goal
  in unloaded terrain from wandering off after data that is not there.

Every step is checked against the real world as it is produced, and a route only
keeps a node when it is meaningfully closer than the best already known. That is
what keeps a search across a large area from exploring everything. That
threshold is the `0.01` constant behind `path.repropagateImprovement`, which can
be turned off to let the search revisit nodes freely at the cost of a slower
search.

### Reading the world

The search does not ask the level for a block state and wait for an answer. It
holds on to the one chunk it is currently working in, and reads blocks straight
out of that chunk's section array. This matters because a single node costs tens
of block reads across a handful of chunks, and a route of a few thousand nodes
turns into hundreds of thousands of reads.

Two behaviours come out of this that are worth knowing:

- A read outside the world's vertical range, or in a chunk the client has not
  loaded, returns air rather than failing. The search simply sees open space.
- A chunk section that holds nothing but air short circuits without any lookup
  at all, which is most sections in a cave or in open air.

Moves that would cross into an unloaded chunk are rejected outright, and each
rejection counts towards `path.maxChunkBorderFetch`.

## The chunk cache

A route that needs terrain the game is not holding has nothing to read. HELM
therefore keeps its own record of chunks it has seen, so a route can cross ground
the player has already walked past or is too far away to still be loaded.

### What is remembered

Every chunk the client loads is recorded in the background, and so is every
chunk just before the game unloads it. What is stored is not the whole world. A
route only needs to know whether a block is open, is water, is something to stay
out of, or is solid, so each block is stored as two bits saying which of those
four it is.

On top of that, two exact details are kept:

- the topmost non-air block of each of the 256 columns, stored exactly, so that
  standing on a slab looks like a slab
- the position of every block HELM tracks exactly, which is containers, shulker
  boxes, beds, ladders, vines, spawners, anvils, jukeboxes, cobwebs, beacons and
  similar

Everything else is answered by category. Solid becomes the dimension's ordinary
stone, open becomes air, water becomes water, and something to avoid becomes
lava, which is the most expensive thing to walk into. This is deliberate: the
route's decisions are about cost and passability, and those are exactly what the
two bits record.

### Tracked blocks do not come back in the overworld

Worth knowing before you rely on the cache, because it is not obvious.

Tracked positions are stored against the block's real world level, but they are
looked up against the level counted from the bottom of the world. The two are
equal only when the world starts at level zero. So:

- In the Nether and the End, which do start at level zero, a tracked block is
  found and returned exactly.
- In the overworld, which starts at level -64, they never line up. A cached
  ladder comes back as ordinary stone, a cached chest as stone, and so on. The
  route then treats them as solid, which it can path around but not through, and
  cannot climb.

The surface block of each column, which is the block you actually stand on, is
unaffected and always exact.

This is reproduced as it stands rather than corrected, so that HELM's behaviour
is identical to the behaviour it is being matched against. If you would rather
tracked positions were found in every dimension, that is a one line change.

### When it is read

A block is read from the live world when the client has that chunk loaded, and
from the cache only when it does not. That is the default and it is what
`cache.preferLoadedChunks` controls; turning it off makes the search use the
cache alone, which is rarely what you want but is useful when you want a route
computed purely from what HELM remembers.

A chunk counts as loaded for the purposes of the search budget if it is in the
cache as well, so a route can continue past the edge of the client's view
instead of stopping dead at it.

### What is not remembered

- A read of a block that is neither in the live world nor in the cache is air.
  The route sees open space rather than an error.
- Tracked blocks are not returned in a world with a negative floor, so ladders
  and vines in cached overworld terrain read as stone. See above.
- If a cached chunk cannot be read back from disk, that chunk is ignored and the
  rest of the region is kept. A corrupt file costs you one region, not the cache.
- The cache is a record of what the world looked like, not a record of where
  things are. It is not used for placing blocks or for interacting.

### Settings

| Name | Default | What it does |
| --- | --- | --- |
| `cache.enabled` | `true` | Remember chunks to disk at all |
| `cache.preferLoadedChunks` | `true` | Read the live world first and fall back to the cache |
| `cache.pruneFromMemory` | `true` | Release regions more than 1024 blocks away |
| `cache.queueLimit` | `2000` | Chunks that may wait to be recorded at once |
| `cache.expirySeconds` | `-1` | Forget chunks older than this; below zero never expires |
| `cache.repackOnBlockChange` | `true` | Re-read a chunk when a tracked block in it changes |

`cache.enabled` and `cache.preferLoadedChunks` are read when the world loads, so
changing either of them takes effect the next time you join a world. The rest
take effect immediately.

### Storage

```
<game directory>/HELM/cache/<namespace>/<dimension>_<height>/
```

One folder per dimension per world height, and inside it one file per region, a
region being 512 by 512 blocks. Files are named `r.<x>.<z>.rcache` and are
compressed. They are written on a background thread, roughly every ten minutes,
and when you leave the world.

Changing `cache.expirySeconds` to a positive value is the way to stop the cache
growing without bound. At the default of `-1` nothing expires, and the cache
keeps every chunk it has ever seen.

Nothing here is read from a previous storage location, and no folder outside the
game directory is ever touched.

### Shortening a partial route

When the goal is not reachable, the route is deliberately trimmed before it is
walked, so HELM does not commit the player to a long walk that is known to end
nowhere.

- `path.cutoffAtLoadBoundary` drops everything from the first position whose
  chunk the game has not loaded. It is off by default, because the search
  normally refuses to cross unloaded chunks in the first place.
- `path.cutoffFactor` and `path.cutoffMinimumLength` shorten a long unfinished
  route by a fraction. The default keeps 90% of a route once it is at least 30
  movements long, so a 1000 step route is walked for 902 steps. Routes below
  `path.cutoffMinimumLength` are never shortened.

A route that reached the goal is never shortened this way.

### Short routes

A route that does not reach the goal is only worth walking if it got more than
five blocks from where the player started. A shorter one is discarded, so a
player standing in a hole is not told to walk two steps nowhere.

That rule is only ever applied to a route that fell short. A route that actually
reached the goal is returned as found, however short it is, so a goal one block
away or standing directly underfoot is walked to normally. The two are kept
apart deliberately: the filter exists to avoid offering a hopeless walk, not to
discard a walk that succeeded.

### Failure

- The walk is abandoned if a step becomes impossible, for example because a block
  appeared in the way or the ground was removed
- The walk is abandoned if the player ends up more than three blocks from the
  nearest point of the route, or stays more than two blocks away for 200 ticks
- A single step that takes longer than its calculated cost plus
  `movement.movementTimeoutTicks` abandons the walk
- Abandoning clears every pressed key and stops mining, so the player is left
  standing still rather than holding a direction

In all of these the goal is not reached and HELM stops. It does not silently
retry.

### Cancellation

`$stop` ends a walk at once. Short forms: `$cancel`, `$abort`, `$halt`. It says
`Nothing to stop.` when there was no walk in progress.

Leaving the world also ends the walk and clears the controls.

## Walking in the right direction

Pressing forward moves the player along wherever the camera happens to be
pointing, which is not where the route goes. HELM makes the player walk the
route's direction instead.

There are two separate things to fix, and both are needed.

**The body has to face the route.** The game takes the direction of travel from
the player's own facing angle rather than from the keys alone, so HELM turns the
player to the route's direction for the part of the tick where the game reads
that angle, then turns them back. With `look.freeLook` on, which is the default,
the turn is sent to the server but not to the camera: the player's body faces the
route and the camera stays where you left it. With `look.freeLook` off the camera
follows the route as well.

**The move itself has to be worked out the right way.** That calculation happens
inside the entity update, which runs before the point above. So for the whole of
that calculation HELM puts the route's angle in place, and puts the camera's
angle back immediately after. Without this, the player walks wherever the camera
points even though the body is turned correctly.

The angle that goes to the server is measured from the rotation the server
already knows about, not from the camera. That matters precisely because the
camera and the body are deliberately pointing in different directions: measuring
from the camera would produce a wildly wrong turn.

Jumping is handled the same way, because a jump's horizontal push is also taken
from the facing angle.

The order of these steps within a tick matters and is easy to get wrong. The aim
is worked out at the end of a tick, and the rotation is only cleared at the start
of the next one, once the movement has already used it. Clearing it at the end of
the same tick instead leaves the game with no aim to move by, and the player walks
wherever the camera points.

## How a route is chosen

### Cost

Every way of getting from one block to another has a cost in ticks. Roughly, one
block of flat walking costs 4.63 ticks, sprinting 3.56, a step up 7.80, a ladder
up 8.51, a drop one block 4.53, and a 100 block fall 17.29. Mining a block
costs one divided by how fast the held tool breaks it, plus a flat penalty, which
is why a path prefers going around when going around is not much longer.

The fall costs come from integrating the same gravity and drag the game uses, so
a longer fall really does cost more than a shorter one in the right proportion.

### The moves

A route is built out of these moves, and only these:

| Move | What it does |
| --- | --- |
| Step | Flat, onto a column that is already solid |
| Step with a bridge | Flat, onto a column that has to be built first |
| Step up | One block up, jumping if needed |
| Drop | One block down, or further |
| Fall | Any distance down where the landing is already solid |
| Diagonal | Flat, one block across and one block sideways |
| Diagonal up or down | A diagonal that also changes level |
| Pillar | Straight up, placing a block underneath |
| Dig down | Straight down, mining under itself |
| Parkour | A jump of up to four blocks across a gap |

A step with a bridge and a step that needs mining are both still steps. What
changes is the cost, because mining and placing are priced in.

### What makes a block passable

The classification is the part that decides whether a route is sensible, so it
follows the same rules the game itself uses.

A block can be walked through when it is air, a door, a fence gate, a carpet, or
thin snow, or a water surface that is not a source block. It cannot be walked
through when it is fire, a cobweb, a cactus, a berry bush, a bubble column, a
powder snow block, a shulker box, a slab, a trapdoor, a pointed dripstone, a
big dripleaf, a skull, a cocoa pod or a flame.

A block can be stood on when it is a full cube, a ladder, a vine when vines are
enabled, farmland, a dirt path, soul sand, a chest or ender chest, glass, stairs,
a slab other than a bottom one when those are disabled, or the surface of water
or lava.

Lava is never walked on. Water is walked on only when the block above it is not
water, which is why a path will not try to run across a deep lake.

### What a path will avoid

- Lava, fire, cactus, sweet berries, cobwebs, an end portal and bubble columns
- Breaking a block next to a liquid, or a liquid source, because the liquid would
  spill
- Breaking ice, because it melts
- Breaking a block with a falling block above it, which would fall on the player
- Breaking a block next to a falling block that has nothing under it
- Blocks on the `mining.avoidBreaking` list, which are treated as if they were
  air

Magma blocks are avoided unless `movement.allowWalkOnMagmaBlocks` is on, in which
case they are walked on at a slow sneak pace and sprinting is given up on them.

### Parkour

A parkour jump is only considered when the block in front is empty, the block
below it is not something to walk on, and nothing hazardous is in the landing
zone. How far it can reach depends on what is underfoot: four blocks while
sprinting, three while walking, two on soul sand or magma.

A parkour jump also has to not overshoot into something hazardous, which is
checked before the jump is committed to.

### Corner cases the cost model covers

- A one block gap is bridged, a wider one is jumped
- Sand and gravel are waited for rather than mined through, because mining
  underneath them drops them on the player
- A step into water costs more than a step on land, and a step onto a water
  surface costs more again
- A fall into water stops the fall, and a fall past the survivable height is not
  taken unless a water bucket would save it
- Climbing a ladder costs its own rate, and stepping off a ladder downward costs
  a different one
- A move that needs a block placed on the side the player is coming from is
  rejected, because the player would be standing where it needs to be placed

## While walking

### Mining

A block in the way is mined before the step continues. Before mining, the best
tool for that block is selected. The player turns to face the block, and only
starts mining once they are actually looking at it, so a swing is not wasted.

Mining is rate limited by `movement.blockBreakSpeed`, so several blocks in a row
do not get mashed out in one tick. Falling blocks above a block being mined
pause the walk until they settle, so sand and gravel do not land on the player
mid step.

### Placing

Placing happens when it is cheaper than mining, which in practice means bridging
a gap, stepping up onto an empty column, and pillaring.

A block is placed against one of its five neighbours, never the one the player is
standing in the way of. The player turns to face that neighbour, and the block is
placed once they are looking at it.

When a placement needs the player to stay put at an edge, they sneak for the
placement. `movement.assumeSafeWalk` turns that off if the server already
handles edge safety and the sneak causes problems.

The throwaway block used is chosen from the hotbar, preferring common filler
blocks. If nothing placeable is held, the step is reported unreachable rather
than trying anyway.

### Aiming

Aiming is separate from walking, and never fights it.

By default, aiming is sent to the server and the camera stays where the player
left it. That is `look.freeLook`, and it is why walking around does not swing
the view.

An angle is measured from the player's eye, that is the feet position plus the
eye height, to the centre of the block being aimed at, which is the block corner
plus a half on each axis. Measuring from anything else is wrong in both axes:
the block corner is level with the feet, so the pitch comes out pointing at the
ceiling, and the yaw is skewed by half a block on each side of the travel
direction.

Walking only ever changes the yaw. The pitch is replaced with whatever pitch the
player already had, so the aim is the direction of travel and nothing more. A
walk along flat ground therefore stays level instead of tipping down at the
centre of the next block.

The yaw asked for is the one nearest the yaw the player already has. A target
that sits just past the 180 degree line is asked for at 180.1 and not at
-179.9, so the turn is a fifth of a degree rather than a full spin the wrong
way. An aim is never rewrapped into the range -180 to 180 after it is worked
out, because that would undo the nearest-angle choice and turn it back into the
long way round.

The rotation the player arrived with is remembered before the aim is applied,
and the camera is put back to that rotation afterwards. Restoring the angle that
was sent to the server instead would make the aim depend on its own result, and
the view would pull a little further off course on every step.

The aim is nudged onto whole mouse steps, so the angle asked for is the angle the
game can actually produce at the current sensitivity. A small random offset is
added each tick, larger on yaw, so the aim does not look mechanically exact.
`look.randomLooking` and `look.randomLooking113` control both, and setting them
to zero turns the effect off.

If the player is already looking at a block that needs mining, the aim is left
where it is, so the view does not jump. `look.remainWithExistingLookDirection`
controls that.

`look.smoothLook` blends the camera yaw across the last few server yaws rather
than snapping, with `look.smoothLookTicks` setting the window.

### Direction

Walking sets forward, back, strafe, jump, sneak and sprint directly. Sneaking
damps the movement impulse to 30 percent, which is what makes edge placements
reliable.

Sprint is not simply held down. It is re-decided every tick:

- a step is not sprinted into a wall
- a step up is sprinted into where the landing and the step after it line up and
  nothing needs mining
- a drop is sprinted out of only when the landing is walkable
- a drop straight into a step up is skipped entirely and turned into the step up

That last one is why walking up a slope looks smooth instead of stuttering
through every step.

### Staying on the route

If the player is not where the route expects, HELM does not push them back. It
first checks whether they are simply behind, for example after a teleport or a
knockback, and rewinds to the matching step if so. Failing that it looks ahead a
few steps for where the player actually is, and skips forward if it finds it.
Only if neither works, and the player is genuinely far away, does it give up.

## Rendering

The route is drawn in the world as it is walked.

- The route is drawn as a ribbon slightly above block centre, or as a plain line
  when `path.renderPathAsLine` is on
- Blocks to mine are outlined in red, using the real shape of the block
- Blocks to place are outlined in blue
- `path.fadePath` fades the route out with distance
- `path.lineWidth` sets the thickness
- `path.renderPath`, `path.renderBlocksToBreak` and `path.renderBlocksToPlace`
  turn each part off

The route is drawn with the same line pipeline the game itself uses, so it is
depth tested and disappears behind terrain. Drawing it through walls would need a
second pipeline, and the 26.2 rendering interface does not expose a way to build
one without reaching into private fields, which would stop the game loading. That
is why there is no setting for it.
