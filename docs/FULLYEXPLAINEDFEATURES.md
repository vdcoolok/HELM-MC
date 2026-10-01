# Features in detail

## Command system

### What it is

HELM exposes every feature it has through a single command line form:

| Entry point | Typed in chat | Example |
| --- | --- | --- |
| Command prefix | `$name args` | `$version` |

A line beginning with `$` is handled entirely on the client. It is intercepted
before the message leaves the client, so a command line is never transmitted to
the server and is never seen by other players. A line that does not begin with
`$` is ordinary chat and is sent normally.

`$` is a single character, it does not collide with server chat commands, and it
leaves every other client and server command untouched.

### How it behaves

There is one command tree. Names, arguments, validation, completion, error
messages and help are all resolved through it, so the command list, the help
output and the suggestion popup cannot drift away from what actually runs.

For any given command:

- the same arguments are accepted
- the same arguments are validated and typed
- the same error messages are produced
- the same output is produced
- the same side effects occur
- the same aliases work
- the same help text is shown

### Arguments

Arguments are declared with a type. Supported types are:

| Type | Accepted input |
| --- | --- |
| string | any text; an optional trailing argument consumes the rest of the line |
| integer | a whole number |
| number | a decimal number |
| boolean | `true` or `false` |

Required arguments must be present. An optional argument may be omitted. Giving
an argument a value of the wrong type reports an error naming the argument and
the offending value.

Text may be quoted to include spaces, and a backslash escapes the next
character:

```
$say "hello world"
```

### Aliases

A command may declare aliases. An alias behaves exactly like the name it
aliases:

```
$version
$ver
```

### Command suggestions

Typing `$` in the chat box opens the command suggestion list, the same way
typing `/` opens the client command list. The list is produced from the same
declarations as the commands themselves, so it cannot list a command that does
not exist or omit one that does.

The list narrows as you type. Arrow keys move through it, and tab or enter fills
in the highlighted command. Suggestions stop at a completed command name, so a
command with no arguments does not offer anything after its name.

### The command list

The command list shows one line per command:

```
[HELM] Commands  $name
  help - Lists every available command.
  version - Shows the loaded HELM version.
```

Lines are kept short so they do not wrap. Full syntax, aliases and argument
details appear when a line is hovered. Clicking a line fills that command into
the chat box without sending anything to the server.

### Adding a command

A command is declared in one place. Registering it makes it available through
`$name`, in the command list and in the help output. No separate registration,
listing or help entry is required; the rest is generated.

### Failure behaviour

| Situation | Result |
| --- | --- |
| Unknown command name | error naming the command |
| Missing required argument | error naming the argument |
| Argument of the wrong type | error naming the argument and the value |
| Unexpected trailing text | the command runs with the arguments it accepts |
| Executor throws | the failure is reported and the command returns failure |

### Cancellation

Individual commands are instantaneous and hold no state, so there is nothing to
cancel. A command that starts long running work will cancel that work when it
finishes.

### Limitations

- The `$` prefix is not configurable.
- The `$` prefix is a client side feature. Other players and the server do not
  see command lines.

## help

Lists every available command with its syntax, aliases and arguments. Available
as `$help`, with the aliases `h` and `?`. A bare `$` also shows this list.

## version

Reports the loaded HELM version. Available as `$version`, with the alias `ver`.

## goto

Walks to a block position. Available as `$goto <x> <y> <z>`, with the aliases
`g`, `go` and `to`.

The reply is one of `Path found: N steps.`, `Partial path: N steps.`,
`Already at x y z.` or `No path to x y z.` The first means the search reached
the goal, the second means it did not and the walk will go as far as it can, and
the last is a failure where nothing is drawn. When the walk itself brings the
player onto the goal it says `Arrived at x y z.`

Arguments are whole numbers. A non number is an error and nothing is calculated.

This only works in a world. Outside one it reports that and does nothing.

### How the journey works

A goal is a destination, not a single route.

1. The search runs and produces a route. Only the first search of a `$goto` is
   announced in chat; later ones only appear in the session log.
2. The route is walked one step at a time.
3. If the route runs out, or a step becomes impossible while it is being walked,
   a new search starts from the block the player is actually standing on and
   step 2 continues.
4. That repeats until the player is standing on the goal, which prints
   `Arrived at x y z.`, or until a search finds nothing usable, which prints
   `No path to x y z.`

So a wall built in the middle of the route, a block placed on it, or a server
correction that pushes the player sideways do not end the walk. They cost a
moment while the next search runs, during which the player stands still.

`$stop` ends the journey, so no further searching happens after it.

### Searching off the frame thread

A search does far too much work to sit on the thread that draws the frame. When a
goal is given, the work is handed to a worker and `Searching for a way to ...` is
printed straight away. The reply that matters arrives a moment later.

Nothing about the world is held still to make this safe. The worker is given its
own view of which chunks are loaded, captured before the work is handed over, so
it never reads the live chunk storage while the game is loading and dropping
chunks underneath it. The chunks themselves are shared rather than copied, so a
block that changes mid search can still be seen, which is the same trade the
rest of the search makes when the world changes under it.

That view is taken by looking up the loaded chunk array by name, because the
client keeps it in a type that is not visible from outside its own package. If a
future version of the game renames it, the search says so once in the log and
carries on using the stored chunk cache instead, which is slower but still
correct. Nothing about it stops the game from starting.

Settings are read at the moment the work is handed over, so changing a setting
while a search runs cannot change what that search is comparing against.

Only one search runs at a time. Asking for a new goal while one is in flight
cancels the running one, because its answer is for a goal nobody is walking to
any more. `$stop` cancels one too, and says so.

The workers are daemons, so a search that is somehow still running can never hold
the game open when you close it, and idle ones are reaped.

### Budgets

Three budgets apply, all adjustable under `path`. They bound how much searching
happens, not how long the frame is held, because the frame is never held:

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

Two behaviours come out of that which are worth knowing:

- A read outside the world's vertical range, or in a chunk the client has not
  loaded, returns air rather than failing. The search simply sees open space.
- A chunk section that holds nothing but air short circuits without any lookup
  at all, which is most sections in a cave or in open air.
- Air answers immediately, without walking the list of blocks that stop the
  player. Air is by far the most common thing a search reads.
- Every distinct block state is asked once whether the player can stand on it,
  can walk through it, or can be jumped through, and the answer is kept against
  that state. Later questions about the same state anywhere in the world are a
  table lookup. This is what keeps a search fast: a route of a few thousand nodes
  reads the same handful of block states tens of thousands of times, and deciding
  the answer means testing the state against every block that changes the answer,
  plus building that block's collision shape.
- A question whose answer genuinely depends on what is next to the block, such as
  a carpet, a snow layer, a slab or a liquid surface, is remembered as needing
  its neighbours checked rather than as an answer, so only those pay for the extra
  reads.

### How a route is chosen

Every way of getting from one block to another has a cost in ticks. Roughly, one
block of flat walking costs 4.63 ticks, sprinting 3.56, a step up 7.80, a ladder
up 8.51, a drop one block 4.53, and a 100 block fall 17.29. Mining a block
costs one divided by how fast the held tool breaks it, plus a flat penalty, which
is why a path prefers going around when going around is not much longer. Placing
a block has its own flat penalty, and every jump has one as well, because
jumping costs hunger.

The fall costs come from integrating the same gravity and drag the game uses, so
a longer fall really does cost more than a shorter one in the right proportion.

A route is built out of these moves, and only these:

| Move | What it does |
| --- | --- |
| Step | Flat, onto a column that is already solid, bridging the gap if it is not |
| Step up | One block up, jumping if needed |
| Drop | One block down onto something solid |
| Fall | Any distance down where the landing is already solid |
| Diagonal | Flat, one block across and one block sideways, level or not |
| Pillar | Straight up, placing a block underneath |
| Dig down | Straight down, mining under itself |
| Parkour | A jump of up to four blocks across a gap |

A step that needs bridging, and a step that needs mining, are both still steps.
What changes is the cost, because mining and placing are priced in.

### What makes a block passable

The classification is the part that decides whether a route is sensible, so it
follows the same rules the game itself uses.

A block can be walked through when it is air, a door, a fence gate, a carpet, or
thin snow, or a water surface that is not a source block. It cannot be walked
through when it is fire, a cobweb, a cactus, a berry bush, a bubble column, a
powder snow block, a shulker box, a slab, a trapdoor, a pointed dripstone, a big
dripleaf, a skull, a cocoa pod or a flame.

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

When no landing is within reach, `movement.allowParkourPlace` lets a block be
placed on the far side to land on instead, provided there is something to place
it against. Without that there is no way across at all, because the gap has to
be crossed in one jump.

A three block jump is held back until the player is at least 0.7 blocks away
from the centre of the block it launched from. Jumping at the last moment is
what stops the player from walking slowly off the edge of a two block gap
instead of clearing it. A jump that also goes up a block is not held back that
way, because there is no gap to fall into.

Looking for a gap costs something on ground that has none, since the only way to
know a gap is not there is to check. That is the trade for never having to build
a bridge over a gap that could simply be jumped.

A jump is launched from the block one step along from where the player started,
not from the far side, and backing up to line it up is also one step at a time.
Those positions are about the player's own footing, so they stay the same however
wide the gap is. What is one step along is always one block, never the whole
distance being jumped.

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

### Short routes and partial routes

A route that does not reach the goal is only worth walking if it got more than
five blocks from where the player started. A shorter one is discarded, so a
player standing in a hole is not told to walk two steps nowhere.

That rule is only ever applied to a route that fell short. A route that actually
reached the goal is returned as found, however short it is, so a goal one block
away or standing directly underfoot is walked to normally. The two are kept
apart deliberately: the filter exists to avoid offering a hopeless walk, not to
discard a walk that succeeded.

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

## Walking a route

### Between two steps

A step that finishes releases every key it was holding. The next step therefore
starts from nothing, and if it did not get started until the following tick the
player would spend a whole tick standing still between steps. On flat ground a
step takes only a few ticks, so losing one to every boundary turns a walk into a
series of little hops.

The handover is therefore made inside the same tick. A finished step releases its
keys, the next step is started straight away, and the forward press it asks for
lands before the tick ends. What the player experiences is one continuous
movement input across the whole route rather than a press, a gap, and a press
again.

The same applies when the player has drifted onto an earlier or later step of the
route and the walker needs to catch up: it re-points and carries on within the
tick rather than idling one.

Waiting and giving up are the two cases that do not hand over, because neither is
a step boundary. Waiting for an unloaded chunk leaves the player still on purpose,
and giving up ends the walk.

Each step also records its own cost when it begins, rather than comparing every
step against the first one. A later step being naturally more expensive than the
first is not a reason to give up on it.

### Staying on the route

Every step owns a set of blocks, not just the one it starts on and the one it
ends on. A jump across a gap owns every block it passes over, at two heights. A
step up owns the block behind it that HELM backs up into, at two heights. A
diagonal owns both corners it cuts. A long fall owns every level it passes
through on the way down, at the column it lands in.

That set is what decides three things:

- If the player is standing somewhere the current step owns, nothing happens. If
  they are not, HELM looks backwards for the earliest step that does own where
  they are and goes back to it, and otherwise looks forwards at least three steps
  ahead and jumps there. That is what puts a walking player back on the route
  after a server nudge or a knockback. Looking forwards deliberately skips the
  next two steps, because those are the ones that are deciding whether to end
  early, such as a sneak placement, and a player in the middle of one is not
  necessarily where those steps think they are.
- How far off the route counts as strayed. Distance is measured to the closest
  owned block anywhere on the route, not to the step being walked, so being
  airborne over a gap is not straying.
- Being mid-fall is measured flat, against the block the fall lands on, because
  a player halfway down a tall fall is a long way from everything in three
  dimensions and is not actually lost.

More than two blocks out starts a counter. Over two hundred ticks out abandons
the route; more than three blocks out abandons it straight away. Either way the
search starts again from where the player stands.

### When a route is thrown away

A route is abandoned when the step being walked can no longer be done, when its
price has risen by more than `movement.maxCostIncrease`, or when one of the next
few steps has become impossible. Abandoning is not fatal: the search restarts
from the player's current block.

Not every step is thrown away. A step that is already under way is left alone. A
jump across a gap, a long fall, a step up that needed a block placed, and a flat
step that still has to be bridged are all finished rather than dropped, because
stopping half way through one of those strands the player in a hole. A step up
whose landing was already there can still be dropped, and so can a flat step
whose bridge block is already down, because there is nothing half finished about
either. A diagonal is dropped unless the player is standing in a corner that
still has something under it.

Abandoning clears every pressed key and stops mining, so the player is left
standing still rather than holding a direction.

The look ahead for steps that have become impossible only runs for steps that
were planned while the chunk they land in was loaded. A step planned over
terrain the game had not loaded cannot be trusted to still be possible, so losing
it is not a reason to stop.

A single step that takes longer than its calculated cost plus
`movement.movementTimeoutTicks` is also abandoned. With the default that is 100
extra ticks, so a step that is going nowhere gives up after a little over five
seconds rather than looping forever.

### Mining and placing

A step that has a block in the way asks for it to be broken, and one that has a
gap asks for a block to be placed. Those requests are read at the start of the
next tick, before the controls are reset for it.

The order matters. Reading them after the reset would find nothing pressed,
because the reset happens first, and neither mining nor placing would ever run.
So the intents are captured first and the controls are cleared afterwards.

Mining breaks the block under the crosshair, so the aim is turned to it before the
break is asked for. A step waiting on a block to be broken makes no progress
until that block is gone, and runs out of time on the step if it never goes.

Mining is rate limited by `movement.blockBreakSpeed`, so several blocks in a row
do not get mashed out in one tick. Falling blocks above a block being mined
pause the walk until they settle, so sand and gravel do not land on the player
mid step.

### Placing

A gap is bridged by placing a block underneath where the player is about to step.
The player turns to look at the face of the block it is standing on, waits until
it is actually looking at that face, and only then places, and only once the game
reports it is crouching. The order is deliberate: sneaking is requested a tick
before the placement so the game has applied it, and the right click is only sent
after the raycast confirms the placement would land where it is meant to.

When no side can be placed against from the approach being used, HELM backs up
one block, turns to look at the face between where it came from and where it is
going, and places against that instead. That is the case a route cannot see, so
it is handled at execution time rather than during the search.

`movement.assumeSafeWalk` takes the edge safety out of the equation: HELM places
without insisting on crouching first. It is faster and it will drop the player
off an edge if the assumption is ever wrong.

### What a step breaks

A step names the blocks it needs gone before it can be walked. The list is short
and deliberate.

- the block being stepped into
- the block above that one, so there is headroom
- for a step up, the second block above where the player is standing, because
  that is where they have to fit while they jump

A step down never names the block underneath the destination. That block is the
floor being landed on, so breaking it would drop the player through the ground
instead of onto it.

Blocks named by `mining.avoidBreaking` are treated as solid by the search rather
than as something to break, so the route goes around them instead of through
them. They are not walkable and not passable, which is what makes the search
leave them alone.

### Walking in the right direction

Pressing forward moves the player along wherever the camera happens to be
pointing, which is not where the route goes. HELM makes the player walk the
route's own direction instead, by measuring from the body.

Direction input is taken from the angle the body is facing, not the angle the
camera is pointing at, and not from the camera. That matters precisely because
the camera and the body are deliberately pointing in different directions:
measuring from the camera would produce a wildly wrong turn.

Jumping is handled the same way, because a jump's horizontal push is also taken
from the facing angle.

The order of these steps within a tick matters and is easy to get wrong. The aim
is worked out at the end of a tick, and the rotation is only cleared at the start
of the next one, once the movement has already used it. Clearing it at the end of
the same tick instead leaves the game with no aim to move by, and the player walks
wherever the camera points.

### Sprinting

Sprint is not simply held down. It is re-decided every tick. HELM will not sprint
at all when `movement.allowSprint` is off or the food bar is at six or less.

- a step is not sprinted into a wall
- a step up is sprinted into where the landing and the step after it line up and
  nothing needs mining
- a flat step immediately before a step up is dropped and turned straight into
  the step up, but only once the player is lined up on it and clear of the block
  behind their head
- a drop is sprinted out of only when the landing is walkable
- a drop straight into a step up is skipped entirely and turned into the step up
- a drop straight into another drop is not sprinted when the drop after that one
  could not be sprinted either
- a drop that would carry the player into frosted ice, or past a hazard, or past
  a column of air, is stepped down carefully instead of sprinted
- a step up that follows a drop lets go of jump once the player is high enough,
  which is what stops a stutter when coming up out of a hole
- a fall of three blocks or less that is followed by up to two flat steps in the
  same direction is turned into a slide: HELM looks at a point past the landing
  and holds forward, so the fall carries on into the flat steps instead of
  stopping dead. A fall that has to mine something on the way down, or a fall
  further than three blocks, or one with no flat steps after it, is just walked

That a drop into another drop does not sprint, and that a drop into a flat step
never sprints either, is deliberate. Sprinting out of a drop is only ever decided
for a drop into a step up, a drop into the same kind of drop, or a diagonal drop
when `movement.allowOvershootDiagonalDescend` is on. Sprinting a drop into a flat
step would carry the player past the block the route picked for them, which is the
one case where the extra speed costs more than it gains. A short fall that can be
carried on into flat steps is the exception, and it is handled by the slide above
rather than by a plain sprint.

Sprint is also given up part way through a jump across a gap on purpose. A jump
that is launched without the sprint still carries its full reach, because the
speed is already built up by the steps before it, and letting go of sprint in the
air keeps the momentum from bleeding away during the arc.

### Per-step state

Each kind of step keeps a little state while it runs, such as how many ticks it
has spent without a block placed, or whether a bridge block was already there.
That state starts fresh for every step, so one step that had to wait for a
placement does not make the next one behave as though it had been waiting too.

The state is also cleared when the route rewinds or skips, so returning to an
earlier step starts it clean rather than halfway through.

### What "looking at" means

Which block counts as the one being looked at is decided by a raycast along the
angle the player is facing, from the player's own eye position. The camera's own
raycast is never used.

That distinction is why mining works. With free look on, the body can face a
block the camera is not pointing at, and the camera can be pointing at something
else entirely. Reading the camera would mean the aim says one block and the break
acts on another, so the arm swings and nothing is mined. Reading the player's own
facing means the block being broken is the block that was aimed at, which is the
only one that can make progress.

The same raycast answers every other question about what is being looked at:
placing against a face, opening a door or a fence gate, and deciding whether the
player is already facing a block, so all of those agree on the same block.

A crouching player traces from the crouching eye height, which is what moves
where the ray starts and therefore which face of a block it meets.

### Rendering

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

### Settings

The settings that change walking and searching are grouped as follows.

| Setting | Default | What it does |
| --- | --- | --- |
| `movement.allowBreak` | `true` | Blocks in the way may be mined |
| `movement.allowPlace` | `true` | Blocks may be placed to bridge, step up and pillar |
| `movement.allowSprint` | `true` | Allow sprinting while pathing |
| `movement.sprintAscends` | `true` | Sprint into a step up where possible |
| `movement.overshootTraverse` | `true` | Accept ending one or two blocks past a flat step |
| `movement.overshootDiagonalDescend` | `true` | Sprint diagonally off a descending step |
| `movement.sprintInWater` | `true` | Sprint while in water |
| `movement.magmaWalk` | `true` | Treat magma as walkable at a sneak pace |
| `movement.vinesWalk` | `true` | Treat vines as a walkable surface |
| `movement.bottomSlabWalk` | `true` | Allow standing on bottom slabs |
| `movement.assumeStep` | `true` | Never jump while stepping up |
| `movement.assumeSafeWalk` | `true` | Place without insisting on crouching first |
| `movement.walkWhileBreaking` | `true` | Keep walking forward while breaking ahead |
| `movement.pauseMiningForFallingBlocks` | `true` | Wait for sand and gravel to settle |
| `movement.allowParkour` | `true` | Allow jumping across gaps |
| `movement.parkourPlace` | `true` | Place a block at the end of a failed jump |
| `movement.parkourAscend` | `true` | Sprint up a single block while parkouring |
| `movement.diagonalAscend` | `true` | Allow stepping up while moving diagonally |
| `movement.diagonalDescend` | `true` | Allow stepping down while moving diagonally |
| `movement.allowDownward` | `true` | Allow digging straight down to travel |
| `movement.assumeWalkOnWater` | `true` | Treat water surfaces as solid ground |
| `movement.waterBucketFall` | `true` | Allow a fall into water at any height |
| `movement.maxFallHeight` | `3` | Longest survivable fall, in blocks |
| `movement.maxFallHeightBucket` | `60` | Same, when a water bucket is used |
| `movement.jumpPenalty` | `2.0` | Cost of every jump, because it costs hunger |
| `movement.blockPlacementPenalty` | `20.0` | Cost of placing one block |
| `movement.blockBreakAdditionalPenalty` | `2.0` | Added to every block mined |
| `movement.walkOnWaterOnePenalty` | `3.0` | Added to stepping onto a water surface |
| `movement.maxCostIncrease` | `10.0` | How much dearer a step may get before the route is dropped |
| `movement.movementTimeoutTicks` | `100` | Ticks a single step may overrun before the route is dropped |
| `movement.costVerificationLookahead` | `5` | How many steps ahead are checked for becoming impossible |
| `movement.jumpAtBuildLimit` | `false` | Allow parkour jumps at the top of the world |
| `path.primaryTimeoutMillis` | `500` | Milliseconds before a search that has not moved is cut short |
| `path.failureTimeoutMillis` | `2000` | Milliseconds before a search gives up |
| `path.maxChunkBorderFetch` | `50` | Moves into unloaded chunks a search may consider |
| `path.repropagateImprovement` | `true` | Require a cost improvement before revisiting a node |
| `path.cutoffAtLoadBoundary` | `false` | Drop the tail of a route that runs into unloaded chunks |
| `path.cutoffMinimumLength` | `30` | Routes shorter than this are never shortened |
| `path.cutoffFactor` | `0.9` | How much of a long unfinished route is kept |

[SETTINGS.md](SETTINGS.md) has the authoritative list with descriptions.

### Failure

- The walk is abandoned if a step becomes impossible, for example because a block
  appeared in the way or the ground was removed
- The walk is abandoned if the player ends up more than three blocks from the
  nearest point of the route, or stays more than two blocks away for 200 ticks
- A single step that overruns its budget abandons the route
- A search that cannot find anything usable from where the player stands ends the
  journey with `No path to x y z.`

None of these lose the goal. Every one of them restarts the search from the
player's current block, and only a search that finds nothing usable ends the
journey.

### Cancellation

`$stop` ends the journey at once. Short forms: `$cancel`, `$abort`, `$halt`. It
says `Nothing to stop.` when there was no walk or search in progress, and
`Stopped searching.` when it caught a search that had not finished yet.

Leaving the world also ends the journey and clears the controls.

## stop

Ends a journey at once and releases every held key. Available as `$stop`, with
the aliases `cancel`, `abort` and `halt`.

It also ends the goal itself, so HELM does not search again for a destination
that was just cancelled. It reports `Stopped searching.` when it caught a search
that had not finished yet, `Stopped.` when it caught a walk, and
`Nothing to stop.` when there was neither.

Leaving the world does the same thing without a message.

## set

Changes a setting. Also available as `$setting`.

| Form | What it does |
| --- | --- |
| `$set` | Opens a picker listing every setting beside its value |
| `$set <name>` | Prints one setting |
| `$set <name> <value>` | Changes one setting |

The picker opens as soon as `$set` is typed with a space after it, so it is never
something that has to be remembered to reach. Each setting is one row showing
its name and current value.

The rows are a single column, capped at eighteen visible, with the mouse wheel
scrolling the rest. One column rather than several because the list is easier to
read top to bottom than across, and the height cap keeps it off the rest of the
screen.

The horizontal space beside the list is spent on the setting under the cursor
rather than on more settings. It shows the setting's readable label, its
explanation, what it is set to now, and what its default is. A setting that has
been changed says so, which is otherwise hard to spot in a list of eighty. The
text is wrapped to the width available and clipped if it runs past the bottom.

While the cursor is on a setting that is not the selected one, that one is
described instead. Once the selection scrolls off screen, the top visible row is
described, so the panel never explains something you cannot see.

Arrow keys move through the list and scroll it to follow, tab or a click puts the
setting under the cursor into the chat box, and typing filters the list as you go.

Filtering looks for what was typed anywhere in a setting's name, not only at the
front of it, so a word that describes what a setting does finds it even when the
name is phrased differently. Results are ordered so the closest match is under
the cursor: an exact name first, then names beginning with it, then names
containing it where a word starts, then anywhere else. A setting's display name
is only consulted when its setting name does not match at all, so a setting is
never pushed down the list by a label that happens to read well.

Once a setting's name is complete the list becomes the values that setting
accepts. A flag offers `true` and `false`, with the opposite of the current
value first, so it can be flipped with two presses. Anything else offers its
current value, so it can be typed over rather than guessed at.

`$settings` remains only to reset everything, as `$settings reset`, which is also
`$settings defaults`.

A value outside a setting's allowed range is clamped rather than rejected. A
value of the wrong kind is an error and nothing changes; a word where a number
belongs is an error rather than being read as zero, and a word where a flag
belongs is an error rather than quietly turning it off. Changing a setting takes
effect immediately, and re-prices a walk that is already in progress.

Settings are stored in `settings.conf` inside the game directory, as plain text,
written as UTF-8 and replaced atomically. A missing or unreadable file falls back
to the defaults without losing anything.

[SETTINGS.md](SETTINGS.md) lists every setting with its default and what it does.

## exitEditMode

### What it is

Closes the open macro without running it. Available as `$exitEditMode`, with the
aliases `exit`, `stopEdit` and `exitEdit`. Every alias also works with `macro` in
front, and a bare `$macro edit` closes the open macro too.

### Visibility

The command exists only while a macro is open. With nothing open it is absent
from the command list, absent from the suggestions, and typing it reports an
unknown command rather than doing nothing. The message that opens a macro states
how to close it.

### Settings

None.

### Known limitations

None.

## macro

### What it is

`macro` starts, stops and lists macros. It is available as `$macro`, with the
alias `macros`.

### Commands

| Action | Effect |
| --- | --- |
| `create <name>` | create an empty macro |
| `edit <name>` | open a macro for editing |
| `edit` | close the open macro |
| `action add <syntax>` | append a line, or list every available command |
| `action remove <line>` | remove a numbered line |
| `action list` | show the open macro's lines as a numbered list |
| `action move <from> <to>` | move a line, shifting the rest |
| `load <name>` | parse and start the named macro |
| `stop` | stop the running macro |
| `list` | list the macros in the macro folder |

### Where macros come from

Macros are plain UTF-8 text files in the `macros` folder inside HELM's data
folder in the Minecraft game directory. The folder is created on demand. The
full syntax is documented in MACROSYNTAX.md.

### Editing

A macro can be edited from chat without touching files. Opening a macro with
`macro edit <name>` makes subsequent lines of macro syntax append to it, and
`macro action` manages the lines of the open macro by number.

While a macro is open, typing in chat opens a popup with the macro language on
the left and the line utilities on the right.

| Column | Contents |
| --- | --- |
| Left | every macro language command, with its syntax |
| Right | `exitEditMode` and the `macro action` line operations |

It is drawn in the same style as the game's own command popup, and takes over
from it rather than drawing behind it. That means a translucent dark background
with an outline, rows 12 pixels tall, grey text, and the row under the cursor
turning yellow. Each row is the name followed by a dash and what it inserts, and
the part after the dash is dimmer than the name so the two read as separate
things.

A row's description is the arguments only, never the full syntax, so a name is
never printed twice in a row.

There are two popup modes. `NAMES` is the two column list of everything a line
typed there could become. `INPUTS` is a list of every key and mouse button, shown
when the line has reached an argument that wants an input, which is what `hold`,
`release` and `press` take.

The input list is generated from the same table the parser uses to resolve an
input, so everything offered is guaranteed to be accepted. Nothing can appear in
the list and then be rejected. It carries a description for each entry, and splits
into keyboard on the left and mouse and scroll on the right.

That list is 82 entries, or 77 lines once two columns are used, which is taller
than most screens. The popup clamps itself to the space above the input box and
to a fixed row count, and scrolls the rest with the mouse wheel, with a dotted
mark on the edge that has more. Moving the selection with the arrow keys scrolls
it into view. A list that fits entirely does not scroll.

The popup only exists while a name is being picked. Once a space appears in the
line it closes, because the rest of the line is an argument rather than a choice,
and the chat bar shows the arguments instead as grey ghost text. The arguments
come from the same declarations the rows do, scanned out of each usage string, so
`wait <duration>` yields `<duration>` and `goto <x> <y> <z>` yields all three.
A name that takes no arguments, such as `exit`, shows no ghost.

The ghost appears only before the first argument is typed. It is not filled in
while an argument is being typed, because the number of arguments already given
cannot always be counted from the tokens: `lookat 14/240` is a single token
holding two values.

Each row shows the text it inserts, so the short name of a utility still types
the full command. Tab fills in the selected row and takes the first match when
nothing is selected. Up and down move inside a column, left and right cross
between them, escape closes the popup, and clicking a row takes it.

While a macro is open, the tool names in the right column can be typed instead of
tabbed. `$list`, `$add wait 1s`, `$remove 2`, `$move 3 1` and `$exit` all run
without the `macro action` in front, and `rm` and `delete` work as well as
`remove`.

There are two different list commands and they are kept apart on purpose.
`macro list` lists the macro files and works at any time. The short `$list` is an
editor tool, so it only exists while a macro is open and it lists that macro's
lines, numbered. `$macro action list` is the same thing written out in full, and
like the rest of `action` it is hidden when nothing is open. This is one list of shortcuts, and the popup and the dispatcher both
read it, so a shortcut cannot be offered without also being runnable.

A shortcut is only expanded once, and only when the line does not already
resolve to a real command. So it can never shadow one, and expanding it can never
expand it again. Outside a macro being open the shortcuts do not exist, so `$list`
is an unknown command rather than a surprising action.

The popup claims only the arrow keys and tab, which have no other meaning while
a chat box is open. Everything else is left to the game.

Enter sends the line exactly as typed rather than accepting the highlighted row,
and escape closes the chat box rather than dismissing the popup. Both were
mistakes: enter being swallowed meant a typed line could never be sent while the
popup was open, and escape being swallowed meant the chat box could not be closed
at all until the popup happened to close first.

Filling in a row replaces only the word under the cursor and never the `$`
prefix, so a filled in line is still a command line.

The popup sizes itself to the screen: it keeps inside the window, drops to one
column when two will not fit, and leaves off a description that will not fit
beside its name rather than cutting it.

Columns are packed. If a filter leaves rows in only one of the two columns, that
column is drawn on its own rather than leaving a gap, and no divider is drawn
between nothing. Navigation moves between the columns that are actually on
screen, so left and right cannot land on one that is not drawn.

The popup appears only when a macro is open, only while the line begins with `$`,
and only while the line does not already say `macro`. Typing `$macro` hands over
to the game's own command popup, so the macro actions remain reachable while
editing.

`edit` and `load` complete the names of macros that already exist, so pressing tab
offers them. `list` shows one macro per line.

The `action` group only exists while a macro is open, so it is absent from the
the action group and from suggestions otherwise. `action add` with no line prints
every available macro command with its syntax and an example.

`action move` lifts a line out and inserts it at the new position, so the lines
between shift along to fill the gap. A position outside the macro is refused
and the file is left untouched.

While a macro is open, a line that does not start with a known macro command is
refused and the available commands are listed, so a typo is never written into
the file. Commands that are real commands, such as `help`, still run.

Because `loop` and `endloop` are separate lines, a macro is usually incomplete
while it is being built. Adding to an incomplete macro still works and reports
that it is not runnable yet.

### How it behaves

A macro is parsed completely before it starts. A syntax error stops the load
and reports the line, so a macro that would misbehave never begins.

Once running, the macro advances one statement per game tick. `wait` suspends
the macro for its duration and resumes afterwards, and `loop` repeats a block
of statements either forever or a given number of times. Loops may be nested.

Starting a macro while another is running replaces the running one.

### Failure behaviour

| Situation | Result |
| --- | --- |
| No macro with that name | `No macro named <name>` |
| Creating a macro whose name is taken | `A macro named <name> already exists` |
| The file cannot be read | error, the macro does not start |
| The macro does not parse | error naming the line, the macro does not start |
| A line number that does not exist | `There is no line <n> (the macro has <m>)` |
| A line that is not valid syntax | the line is refused and the available commands are listed |
| An unclosed loop | `'loop' is missing 'endloop'` |
| The macro reaches an unavailable statement | the macro stops and reports it |
| `stop` with nothing running | error, nothing changes |

Unavailable statements are reported rather than skipped, so a macro never
appears to run while quietly doing nothing. A refused line is never written, so a
mistake cannot corrupt a macro.

While a macro is open, a line that is not a command is read as macro syntax rather
than as an unknown command, because that is what typing there means. The error
lists the syntaxes available, so a mistyped `$exit` says what is valid instead of
leaving it to be guessed.

### Cancellation

A macro can be stopped with `$macro stop`. Leaving the world also ends it.
There is no way to interrupt a macro from inside its own text at present.

### Settings

None. The prefix is fixed and the macro folder is fixed.

### Known limitations

Editing is per session, so closing the game closes the open macro, though
everything written is already saved to disk. There is no way to insert a line at
a chosen position without moving it there first.

## User data

HELM stores user data inside the Minecraft game directory in a top level `HELM`
folder, on both Windows and Linux. The location is resolved through the game at
runtime rather than hardcoded. The folder and its `macros` subfolder are created
the first time a world is joined, with no command needed. Macros live in the
`macros` folder, settings live in `settings.conf` next to it, and the session log
is written to `debuglogs.log`.

| Path | What it holds |
| --- | --- |
| `HELM/macros/` | Macro files. You can add your own |
| `HELM/settings.conf` | Every setting, one per line, as `name = value` |
| `HELM/debuglogs.log` | What HELM did this session, one line per event |
| `HELM/cache/` | Remembered chunks, one folder per dimension |

All three are plain UTF-8 text. No previous storage location is read or migrated
from; HELM has only ever used this folder.

### The chunk cache

`cache/` is the only folder here that is not text. It holds a compressed record
of the chunks HELM has seen, so that path finding can work out a route across
terrain the game is not currently holding. It is written on a background thread
and is never needed for correctness, only for reach.

| Path | What it holds |
| --- | --- |
| `HELM/cache/<namespace>/<dimension>_<height>/` | One folder per dimension per world height |
| `.../r.<x>.<z>.rcache` | One compressed file per 512 by 512 block region |

It is safe to delete while the game is closed; you lose remembered terrain and
nothing else. `cache.expirySeconds` set to a positive value is the way to have it
shrink on its own.

#### What is remembered

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

#### Tracked blocks do not come back in the overworld

Worth knowing before relying on the cache, because it is not obvious.

Tracked positions are stored against the block's real world level, but they are
looked up against the level counted from the bottom of the world. The two are
equal only when the world starts at level zero. So:

- In the Nether and the End, which do start at level zero, a tracked block is
  found and returned exactly.
- In the overworld, which starts at level -64, they never line up. A cached
  ladder comes back as ordinary stone, a cached chest as stone, and so on. The
  route then treats them as solid, which it can path around but not through, and
  cannot climb.

The surface block of each column, which is the block actually stood on, is
unaffected and always exact.

This is reproduced as it stands rather than corrected, so that HELM's behaviour
is identical to the behaviour it is being matched against.

#### When it is read

A block is read from the live world when the client has that chunk loaded, and
from the cache only when it does not. That is the default and it is what
`cache.preferLoadedChunks` controls; turning it off makes the search use the
cache alone, which is rarely what you want but is useful when a route has to be
computed purely from what HELM remembers.

A chunk counts as loaded for the purposes of the search budget if it is in the
cache as well, so a route can continue past the edge of the client's view instead
of stopping dead at it.

#### What is not remembered

- A read of a block that is neither in the live world nor in the cache is air.
  The route sees open space rather than an error.
- Tracked blocks are not returned in a world with a negative floor, so ladders
  and vines in cached overworld terrain read as stone.
- If a cached chunk cannot be read back from disk, that chunk is ignored and the
  rest of the region is kept. A corrupt file costs one region, not the cache.
- The cache is a record of what the world looked like, not a record of where
  things are. It is not used for placing blocks or for interacting.

#### Settings

| Name | Default | What it does |
| --- | --- | --- |
| `cache.enabled` | `true` | Remember chunks to disk at all |
| `cache.preferLoadedChunks` | `true` | Read the live world first and fall back to the cache |
| `cache.pruneFromMemory` | `true` | Release regions more than 1024 blocks away |
| `cache.queueLimit` | `2000` | Chunks that may wait to be recorded at once |
| `cache.expirySeconds` | `-1` | Forget chunks older than this; below zero never expires |
| `cache.repackOnBlockChange` | `true` | Re-read a chunk when a tracked block in it changes |

`cache.enabled` and `cache.preferLoadedChunks` are read when the world loads, so
changing either of them takes effect the next time a world is joined. The rest
take effect immediately.

Files are named `r.<x>.<z>.rcache` and are compressed. They are written on a
background thread, roughly every ten minutes, and when you leave the world.
Nothing here is read from a previous storage location, and no folder outside the
game directory is ever touched.

### The session log

`debuglogs.log` is written from the moment the game starts, without any command
or setting to turn on, and is deleted and recreated on the next start. Each line
carries a wall clock time, the game tick it happened on, and the area it came
from, so a sequence of events can be read back in order.

It records:

- startup, including the settings that were loaded and the world that was joined
- every `$goto`: the requested position, the player's position and rotation, the
  relevant movement settings, how long the search took, how many nodes it looked
  at, and why it stopped
- the shape of the resulting route, its first steps and its last steps
- each step of a walk as it is attempted, with the step kind, the cost the step
  was planned at and the cost it actually costs now
- why a walk was abandoned, in words: a step that stopped being possible, a cost
  that rose too far, too long on one step, or straying from the route
- a halt, skip or rewind when the player's actual position no longer matches the
  step the walk expects

### How it is kept small

The log is written so a whole session stays readable rather than becoming
megabytes. Three kinds of entry are used. Events happen once and are always
written. Repeats are only written when the value they describe actually changes,
and when it has not, a note saying how many times in a row it has been
unchanged. Pulses are written at most once every two seconds per key. A summary
line is written after 25, 100 and 1000 identical repeats so nothing goes
unreported entirely.

If the file reaches half a megabyte, the oldest four thousand lines are dropped.

### Failure

Nothing in the logging can take the game down. Every write is guarded, and the
first failure switches logging off for the session and leaves everything else
running. If the game directory is not writable, the file simply is not created
and HELM reports it once rather than every time a world is joined.

The log contains no other mod's output and no account or server details beyond
the dimension you are in. It is not sent anywhere; it is only useful if you
choose to attach it.

If the directory cannot be created, for example because the game directory is
read only, HELM reports it once in chat rather than every time a world is
joined, and the features that need it remain unavailable. If only the settings
file cannot be written, HELM says so once and the change still applies for that
session. If only the log cannot be written, HELM stays silent and everything else
works.
