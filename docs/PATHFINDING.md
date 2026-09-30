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
freezing the game.

Every step is checked against the real world as it is produced, and a route only
keeps a node when it is meaningfully closer than the best already known. That is
what keeps a search across a large area from exploring everything.

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
