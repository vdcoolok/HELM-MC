# Features in detail (**Yes this is the only thing I've ever used AI on this project, to generate this useless, time taking, document for people who are curious.**)

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

### What the search believes is left

Alongside the real cost of every move, the search keeps an estimate of how many
ticks remain to the goal, so it knows which moves are worth looking at first. The
estimate is priced in the same ticks as the real costs, using
`path.costHeuristic`, whose default of `3.563` is the cost of one block of
sprinting.

Flat distance is priced the way the player can actually cover it: moving
diagonally for one block is charged `√2` blocks rather than `2`, because a
diagonal and a straight step are both single moves. Height is priced separately
and differently in each direction, because climbing and dropping cost differently:
going up is charged the cost of the part of a jump that gains a block, and coming
down is charged half the cost of falling two blocks for each block.

This matters more than it sounds. If the estimate were priced too cheaply, the
search would have almost no reason to prefer blocks that get it closer, and would
spend its whole budget wandering. Priced correctly it walks a narrow corridor
towards the goal and finishes in a fraction of the time.

### Goals that are not loaded

A goal far outside the loaded world cannot be reached in one search, because
there is nothing to walk on out there. Rather than giving up, HELM searches for
the goal's *column* at any height when the goal's chunk is not loaded, treating
any block in that column as good enough for that search. The walk therefore still
heads straight towards the goal, which loads the chunks along the way, and the
next search aims for the real goal now that more of the world is loaded. This
repeats until the goal itself is in loaded terrain.

Set `path.simplifyUnloadedGoal` to `false` to require the exact block every
time, which means a distant goal in unloaded terrain reports no path.

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

### How a pillar is done

A pillar is the only move that has to jump before it can place anything, because
the block it needs is the one the player is standing in.

The player walks to the middle of the column first. Once within a small distance
of the centre and standing still, they jump. Only once they are high enough that
the block they are standing in is below their feet does HELM place it, which
lifts them onto it and completes the move.

That order matters and is the whole move. A block cannot be placed inside the
player, so the jump has to happen before the placement, not after. If the jump is
suppressed the placement can never happen, the player never rises, and the step
sits there doing nothing until it runs out of ticks.

The jump is suppressed in exactly one case: when the block being placed on is
solid and has to be mined out of the way first, because mining is about five
times slower while the player is in the air. Once that block is gone, the jump
resumes and the pillar carries on.

### Swimming

Rising through water is a pillar that places nothing. When the block below the
player is water and the block they are rising into is water too, HELM swims the
column instead of building it: it turns to face the middle of the block above,
walks forward only while off-centre by more than a fifth of a block, and holds
there until it has risen into it.

Because nothing has to be mined first, a pillar into water skips preparing
altogether and starts swimming straight away.

Any step taken while the player is in water presses jump whenever they are more
than three fifths of a block below the height that step is heading for. That is
what carries a player up off the bottom of a lake towards something they are
reaching for, and it applies to every kind of step, not just pillars.

While climbing, a player standing on a ladder or vine holds the sneak key, so
that pressing towards the climbable block does not climb it instead of moving.

### What makes a block passable

A block is either walked through, stood on, or neither, and the answer is
cached per block state so it is worked out once rather than every time. A few
blocks need to know what is around them as well, so they are re-checked where
they are found:

| Block | Walkable when |
| --- | --- |
| Carpet | Whatever is under it is solid enough to stand on |
| Snow layer | It is thin enough to step over, and there is ground under it |
| Trapdoor, open | Always, you walk through the gap |
| Trapdoor, closed | Whatever is under it is solid enough to stand on |
| Water, still | Nothing above it and it is not a source |

A trapdoor is not mined to get through it. An open one is a doorway and a
closed one is low enough to step onto, so neither is worth breaking. A closed
trapdoor in mid air is a different matter, because there is nothing to stand on
if you try, so that one still has to go.

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

### Standing on a block that is not a full cube

Farmland, soul sand, a dirt path and a bottom slab all come up short of a full
block. Farmland stops at fifteen sixteenths, soul sand at seven eighths. A player
standing on either is a little below the height the block above them starts at,
so the height is read from the block the player is standing in rather than from
the raw position, with a slab counted as the block on top of it.

This is what keeps walking over a field of crops steady. Read raw, the player's
height dips under the block boundary every time they settle onto farmland, a step
reads that as being a block too low, and the player is told to jump. They jump,
land on farmland, dip again, and jump for as long as the field goes on. It only
shows on crops because farmland is the common short block a player walks over.

The same height is used everywhere the player's position is compared against the
world: starting a search, deciding whether a goal has been reached, holding an
anchored spot, and the centre a farm scans around.

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

### Being a block off a step's height

A step is walked by moving towards a block, not by standing on an exact square.
If the player ends up a block above or below the level the step was heading for,
the step does not give up the route. The player keeps walking towards the block
the step was heading for either way, and presses jump when they are underneath
it. Walking towards it as well as jumping is what lets a player who has dropped
a block climb back onto the ledge instead of rising and falling on the same spot.

This matters more than it sounds. A route that ends a step early and is searched
for again from wherever the player actually stands will usually begin by getting
back to the height it just lost, which is a jump. If being a block low also threw
the route away, the player would climb, walk a moment, fall a block, throw the
route away again and climb again, forever, never getting anywhere.

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

A placement target is picked by testing the five sides that a block can be built
against, in the order north, south, east, west, then down, and keeping the first
one whose face the raycast actually lands on. That order is why placing a block
underneath the player turns to look down at the top of the block below rather than
at a side: the down face is tested last, so it only wins when no side is reachable
from where the player is standing. Every one of those tests is done from a crouching
eye position, because a placement below the player is made while sneaking, and the
rotation and the raycast have to start from the same point or the two disagree
about which face is being hit.

To place, HELM needs something that is actually a block in hand. If the held slot
holds a tool, an item, or nothing at all, a throwaway block is picked from the
hotbar instead. The blocks it will use for that come from
`movement.placementBlocks`, and they are tried in the order that list is in, so
the first block on the list that is in the hotbar is the one that gets used. The
default list is dirt, cobblestone, netherrack and stone. If the hotbar holds no
placeable block at all, the step reports that it cannot be done rather than
waiting forever for something to appear.

One list covers every kind of placement: bridging a gap, stepping up, pillaring,
and placing at the end of a failed parkour jump. There is no separate list per
move, so a block added once is available to all of them. See
[SETTINGS.md](SETTINGS.md) for how the picker works.

`movement.assumeSafeWalk` takes the edge safety out of the equation: HELM places
without insisting on crouching first. It is faster and it will drop the player
off an edge if the assumption is ever wrong.

### What a step breaks

Each kind of step names the blocks it needs gone before it can be walked, in the
order it wants them gone. That order matters, because the first one still in the
way is the one the player looks at and mines, so a step that names its blocks in
the wrong order will face the player the wrong way while clearing them.

| Step | Blocks it breaks, in order |
| --- | --- |
| Step | The block above the destination, then the destination |
| Step up | The destination, then the second block above the source, then the block above the destination |
| Step down | Two above the destination, one above it, then the destination |
| Fall | The whole column from just above the source down to the destination |
| Diagonal | Nothing |
| Pillar | The second block above the source, and nothing else |
| Dig down | The destination |
| Parkour | Nothing |

A step down never names the block underneath the destination. That block is the
floor being landed on, so breaking it would drop the player through the ground
instead of onto it.

A fall names that column only as far as the destination, for the same reason, and
it does not mine anything at all unless one of the top four blocks of the column is
actually blocked. The four-block window is checked fresh every tick, so a fall whose
column is clear from the top just lets the player drop, rather than turning them
around to swing at leaves and logs further down the column that they were never
going to reach.

A pillar breaks only the block two above the player, never the block at their
own head height, and a parkour breaks nothing at all. Getting either of those
wrong means the player turns to face, and mines, a block that was never in the
way.

A pillar can also break the block it is standing in, but only when that block is
solid and in the way of the one it needs to place there. That is the one case
where a pillar mines at its own feet rather than overhead, and it stops jumping
while it does.

A diagonal breaks nothing either, and this is the one that surprises people
most. The search only offers a diagonal when the player can get round at least
one of the two blocks beside them: either side being passable at feet, knee and
head height is enough, and when both sides are blocked the diagonal is never
offered at all. So the blocks beside the player that a diagonal squeezes past
are, by construction, not worth mining, because the search already knows the
player can walk around them. It still prices the squeeze as a little more
expensive than a clean diagonal, which is what stops the route from picking a
corner-cutting diagonal every time one is available, but it never swings at one.

Mining those two blocks anyway produced a route HELM could not finish. The player
stands still, swings at a block that was never really in the way, runs out of
ticks, throws the route away, searches again, and is handed the same diagonal
with the same two blocks to break, over and over.

The player also only sprints out of a diagonal when all four blocks beside them
are clear, not just the two at their feet, so a diagonal taken past a knee-high
block is walked rather than sprinted.

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
| `movement.assumeWalkOnWater` | `false` | Treat water surfaces as solid ground |
| `movement.waterBucketFall` | `true` | Allow a fall into water at any height |
| `movement.maxFallHeight` | `3` | Longest survivable fall, in blocks |
| `movement.maxFallHeightBucket` | `60` | Same, when a water bucket is used |
| `movement.jumpPenalty` | `2.0` | Cost of every jump, because it costs hunger |
| `movement.blockPlacementPenalty` | `20.0` | Cost of placing one block |
| `movement.placementBlocks` | dirt, cobblestone, netherrack, stone | Blocks that may be placed when a route needs one |
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

Leaving the world also ends the journey, clears the controls, and releases an
anchored position and a locked angle.

## stop

Ends everything at once. Available as `$stop`, with the aliases `cancel`,
`abort` and `halt`.

It stops all four things HELM can be doing: a walk, a search, a running macro, an
anchored position, a locked angle and a farm. The reply names what it actually
caught, so `Walking stopped. Look released.` means both of those were live, and
`Nothing to stop.` means none of them were.

It also ends the goal itself, so HELM does not search again for a destination
that was just cancelled.

Leaving the world does the same thing without a message, and releases the anchor
and the locked angle, so rejoining does not leave the camera pinned.

## autogoto

Walks to a block position and then holds it. Available as `$autogoto`, with the
alias `agoto`.

It is `$goto` with one difference. Once the player arrives, HELM checks every
tick that they are still standing on the anchored block. If another player, a
bomb, or anything else moves them off it, it searches again from wherever they
ended up and walks back. The check is not instantaneous: a short delay is applied
before it reacts, so being nudged once does not start a fresh search on every
tick.

`$autogotohere`, alias `agotohere`, anchors on the block the player's feet are
in, which is the position-less form of the same thing.

Like `$goto`, a partial route is re-planned from the player's real position
rather than treated as arrival. Unlike `$goto`, arriving is not the end of it.

## autolookat

Locks the camera to an angle. Available as `$autolookat`, with the alias
`alookat`. The angle is written as pitch, a slash, then yaw, the same order
`lookat` uses.

`$autolookathere`, alias `alookathere`, locks the angle the player is facing at
the moment it is typed.

The lock is applied twice a tick: once before the player moves, and once after
the entities have ticked. The first application makes the player move as though
already facing that way, and the second one undoes any mouse movement that
happened in between, so the camera cannot be turned.

Mining and placing still work while the angle is held. The lock is applied to
the camera, not to the rotation HELM sends, so the block being looked at is the
block that gets mined.

A locked angle overrides the aim a walk would otherwise set, so a lock and a
walk at the same time fight each other. `$stop` resolves it.

Both are released by `$stop` and by leaving the world.

## mine

Searches for blocks of a given type and mines them until they run out. Available as
`$mine`, with the aliases `dig` and `excavate`.

```
$mine oak_log
$mine 64 diamond_ore
$mine oak_log birch_log
$mine oak_log[axis=x]
```

Mining is not a route with an end point. It keeps finding blocks and mining them,
and it stops when there is nothing left worth mining, when a count is reached,
when `$stop` is typed, or when nothing it wants can be reached.

### Naming blocks

A block name is the game's own identifier, written with or without the
`minecraft:` prefix. More than one name mines all of them, nearest first.

A single block state is picked out with square brackets, and several at once
separated by commas:

```
$mine oak_log[axis=x]
$mine oak_log[axis=x,waterlogged=false]
```

The property name and its value must both be real for that block, or the command
reports which part was wrong and nothing changes. A name that is not a block at
all is reported the same way.

### The block picker

Typing anything after `$mine` opens a picker of every block in the game, in two
columns. The left is every block, narrowed live as the last word is typed, and
shown by its full name so there is no guessing about whether a space belongs in
it. The right is the blocks already named on the line, in order, and it does not
change as you type.

`Tab` completes the highlighted block into the command and `Space` types a space,
so a list can be built without spelling anything out in full:

```
$mine oak_[Tab] [Space] oak_[Tab]
```

Inside the square brackets the picker completes property names, then their values,
instead of block names. Once the closing bracket is typed the picker stops, because
there is nothing left to complete.

Clicking a row in the picker does nothing to the command, so the cursor can still
be put where it is wanted. Nothing is chosen until the command is sent.

### Choosing what it mines

| Setting | Default | What it does |
| --- | --- | --- |
| `mining.maxTargets` | `64` | Most places remembered at once |
| `mining.lowestLevel` | `0` | Lowest level a target may be on |
| `mining.highestLevel` | `2031` | Highest level a target may be on |
| `mining.onlyExposed` | `false` | Require air or liquid touching the block |
| `mining.exposedRadius` | `1` | How far around a target to look for that air |

Level settings are absolute, and `mining.lowestLevel 0` means the bottom of
whichever dimension you are in, so the same value works everywhere. A block outside
the band is never even remembered, not merely passed over later.

`mining.onlyExposed` requires air, water or lava touching the block within
`mining.exposedRadius` blocks. It is much slower to check, because every candidate
has its surroundings read, and it exists for servers that shuffle ores around: a
block that genuinely touches open space cannot have been placed by anything but the
world generator.

### Places it will not mine

A remembered position is dropped when any of these hold:

| Why | What it means |
| --- | --- |
| The chunk is loaded and the block is not wanted there | It was mined, or it was never what it looked like |
| It cannot be broken | No tool reaches it, or the tool cannot mine it at all |
| Breaking it would let something out | Bedrock above and below, or something that would flow in |
| It is outside the level band | See above |
| It was marked unreachable | A route to it could not be found, and it was skipped |

Breaking it "would let something out" is the same rule pathing already uses to
decide what may be broken at all, so mining never digs into something that a walk
would refuse to dig into either.

### Where it looks

Remembered chunks are searched first. Those cover ground you have already been
to, including chunks that are no longer loaded, which is why they are worth reading
before anything else. Whatever they do not hold is looked for in the chunks around
you, widening outwards, and inside each chunk the layers nearest your own level are
read first.

| Setting | Default | What it does |
| --- | --- | --- |
| `mining.rescanEveryTicks` | `5` | Ticks between looks, `0` looks once |
| `mining.scanRadius` | `32` | Chunks around you read when looking |
| `mining.scanLevelWindow` | `10` | How far from your level the scan keeps looking |
| `mining.cacheScanRadius` | `2` | How far around you remembered chunks are searched |
| `mining.cacheScanLimit` | `10` | How many are found there before it stops widening |
| `mining.scanWhenCacheThin` | `false` | Also read loaded chunks when the cache is thin |

The scan stops when a whole ring of chunks around you is unloaded, when the target
limit is reached and it has left your level band, or when it is still turning up
targets at your own level. That last rule is what lets a player standing in a tall
column of ore sweep all of it, while a huge flat field stops at the limit.

A scan does not finish in one tick. Reading every block in that many chunks is slow
enough to be felt as a hitch, so it spends a small slice of each tick and carries on
from where it left off, starting with the chunk you are standing in and widening
from there. Positions found so far are used as soon as they exist, so mining starts
almost immediately and keeps discovering more as the scan widens.

With `mining.rescanEveryTicks 0` the world is looked at once when mining starts and
never again. Anything that turns up later is not noticed.

`mining.scanWhenCacheThin` additionally reads the loaded world whenever the
remembered chunks hold fewer than `mining.maxTargets`. It is off by default because
it costs a great deal more time, and on most worlds the remembered chunks already
hold enough.

### Remembering chunks before it starts

Before mining begins, every chunk within `mining.repackRadius` of you is queued to
be remembered. Routes can then cross ground that has since been unloaded, which
matters underground where you move away from ground quickly.

### When nothing is known

| Setting | Default | What it does |
| --- | --- | --- |
| `mining.exploreWhenUnknown` | `true` | Walk away when no target is known |
| `mining.stripLevel` | `-59` | Level held while doing so |
| `mining.skipUnreachable` | `true` | Mark an unreachable target and try the next |

When nothing is found anywhere it looks, and `mining.exploreWhenUnknown` is on, it
walks away from where the job started and keeps its distance while it does, so new
chunks load and something turns up. The route it takes is not aimed at a block at
all: it is aimed away from the start and at `mining.stripLevel`, so it neither
climbs nor digs while it wanders.

Turning exploration off does not necessarily mean giving up at once. When no route
can be found to any known block, `mining.skipUnreachable` marks the closest one
unreachable and tries the next. Only when the last one is gone does it stop.

```
$set mining.exploreWhenUnknown false
$set mining.skipUnreachable false
```

### Sight only

`mining.sightOnly` never acts on a block you cannot actually see. It is off by
default, and turning it on is the difference between mining where the blocks are
and mining where they are not.

With it on:

| Behaviour | Detail |
| --- | --- |
| Only visible blocks become targets | Read from the world directly, within 10 blocks of you |
| Only blocks you can reach become targets | Checked with a reach of 20 blocks, not your real reach |
| It always keeps looking | Even with `mining.exploreWhenUnknown` off |
| It stops re-reading remembered chunks | The whole world has to be read, since memory would be seeing |
| `mining.sightDiagonals` widens it | Also accepts a block only touching one already visible |

The reach of 20 is deliberate. A block you can see but not yet reach should still
become a target, otherwise HELM would walk to it and then find there was nothing to
mine. It does not grant the ability to mine at that distance.

Sight only is much slower, because every visible block has to be read directly
rather than looked up in remembered chunks, and every candidate has a reach check
against it.

### Getting to a block

The route to a block is a normal HELM route, with the same breaking and placing
rules as walking anywhere else. What differs is where the route is aimed.

| Case | Aimed at |
| --- | --- |
| Nothing above or below worth mining | The block itself |
| A block of the same vein below | Directly under it, two below, or the block below that |
| The block above is a falling block | The block itself or the one under it |
| Digging into the vein is off | The block itself, or the one under it |

Two settings control this:

| Setting | Default | What it does |
| --- | --- | --- |
| `mining.digIntoVein` | `true` | Aim into the vein rather than at the block's edge |
| `mining.digThroughAir` | `true` | Count air beside a block as part of the vein |

`mining.digIntoVein` is what lets one break take two or three, when a vein runs
through the block behind the one that was found. Turning it off means every block is
approached from outside the vein, which is slower but breaks fewer blocks.

`mining.digThroughAir` only matters while digging into the vein is on. With it off,
a single loose block stops the dig immediately, because the air beside it is not
part of the vein. With it on, a vein of one block surrounded by air is treated as a
vein of three, which is what a real vein of that shape looks like underground.

### Breaking what is overhead

When a block that is still wanted sits directly above you, and you are on the
ground, HELM stops and breaks it instead of walking somewhere first. This is what
takes a tree down.

| Setting | Default | What it does |
| --- | --- | --- |
| `mining.breakOverhead` | `true` | Break a block directly above without moving |
| `mining.stopRouteWhenMined` | `true` | Stop a route whose destination has gone |

Overhead breaking needs a block that can actually be broken from where you stand, so
it falls back to walking when the block is out of reach or would release something.
Turning it off makes `$mine oak_log` approach each log from the side, the same way
as anything else.

`mining.stopRouteWhenMined` stops a walk the moment the block it was heading for is
gone, rather than finishing a route that no longer leads anywhere. It makes mining
faster, since no time is spent walking into locations that no longer hold anything,
at the cost of occasionally leaving a drop behind.

### Waiting for drops

| Setting | Default | What it does |
| --- | --- | --- |
| `mining.followDroppedItems` | `true` | Walk over drops of the right type |
| `mining.dropWaitMillis` | `250` | Milliseconds to wait after a break for a drop |

When a drop of the right type is lying on the ground, its position becomes a target
in its own right, so HELM walks over and picks it up rather than leaving it.

Breaking a block and immediately moving on is a race the drop often loses, because
it takes a moment to appear. So the position of a block that has just been broken is
held as a target for `mining.dropWaitMillis` afterwards, giving the drop time to
appear and be collected. It is refreshed while it is still being looked at, so
looking at it extends the wait rather than cutting it short.

Which items count as belonging to a block is worked out rather than listed. The
dropped item cache learns what actually came out of the blocks HELM broke, so
anything a server, mod or config changes about what a block drops is still collected
correctly. See [dropped item cache](#dropped-item-cache).

### Stopping on a count

```
$mine 64 diamond_ore
```

With a count, mining stops once that many matching items are carried. The count is
taken across the whole inventory rather than the hotbar, and it counts anything the
block drops, not the block itself.

The count is checked before anything else each tick, so it stops immediately when
the count is already reached, without walking anywhere first.

### Interaction with other features

Mining is the only thing driving HELM while it runs. `$goto`, `$autogoto`,
`$autolookat`, `$farm` and starting a macro each end the mine first, because they
need control of where you walk and which way you face, and mining has both for as
long as it lasts. `$farm` and `$mine` cannot run at once in either order.

`$stop` stops mining along with everything else.

`mining.renderTargets` outlines every block the job still knows about, not just the
ones on the current route, and `outline.blocksToBreak` continues to outline the
blocks the route itself will break. Both are off independently, and
`outline.enabled` turns off every outline.

### Settings

| Name | Default | What it does |
| --- | --- | --- |
| `mining.breakAllowedAnyway` | *(empty)* | Blocks mined even while breaking is off |
| `mining.maxTargets` | `64` | Most places remembered at once |
| `mining.lowestLevel` | `0` | Lowest level a target may be on |
| `mining.highestLevel` | `2031` | Highest level a target may be on |
| `mining.onlyExposed` | `false` | Require air or liquid touching the block |
| `mining.exposedRadius` | `1` | How far around a target to look for that air |
| `mining.sightOnly` | `false` | Never act on a block you cannot see |
| `mining.sightDiagonals` | `false` | With sight only, accept a block touching a visible one |
| `mining.stripLevel` | `-59` | Level held while exploring |
| `mining.exploreWhenUnknown` | `true` | Walk away when no target is known |
| `mining.skipUnreachable` | `true` | Mark an unreachable target and try the next |
| `mining.rescanEveryTicks` | `5` | Ticks between looks |
| `mining.scanWhenCacheThin` | `false` | Also read loaded chunks when the cache is thin |
| `mining.followDroppedItems` | `true` | Walk over drops of the right type |
| `mining.dropWaitMillis` | `250` | Milliseconds to wait after a break for a drop |
| `mining.digIntoVein` | `true` | Aim into the vein rather than at its edge |
| `mining.digThroughAir` | `true` | Count air beside a block as part of the vein |
| `mining.breakOverhead` | `true` | Break a block directly above without moving |
| `mining.stopRouteWhenMined` | `true` | Stop a route whose destination has gone |
| `mining.repackRadius` | `40` | Chunks remembered before mining starts |
| `mining.scanRadius` | `32` | Chunks around you read when looking |
| `mining.scanLevelWindow` | `10` | How far from your level the scan keeps looking |
| `mining.cacheScanRadius` | `2` | How far around you remembered chunks are searched |
| `mining.cacheScanLimit` | `10` | How many are found before the cache search stops widening |
| `mining.renderTargets` | `true` | Outline every block the job has found |

See [SETTINGS.md](SETTINGS.md) for what each one changes.

### Failure behaviour

| Situation | What happens |
| --- | --- |
| No block name given | error naming the argument |
| Name is not a block in the game | error naming it, nothing changes |
| Property does not exist on that block | error naming the block and the property |
| Value is not valid for that property | error naming the property and the value |
| Nothing left worth mining | mining stops and says so |
| No route to any known block, skipping on | the closest is marked unreachable and the next is tried |
| No route to any known block, skipping off | mining stops and says so |
| Count reached | mining stops and says so |
| Breaking turned off and no block allowed | mining refuses to start and says why |
| Leaving the world | mining stops |

### Cancellation

`$stop` stops mining, releases the walk, and releases any held position or facing.
So does starting `$goto`, `$autogoto`, `$autolookat`, `$farm` or a macro, and so does
leaving the world.

Nothing is left running. The scan stops, the remembered blocks are dropped, and no
controls stay held.

### Known limitations

- Only blocks are accepted. An item with no block form cannot be mined.
- A drop is collected by walking over it. HELM does not path to a drop that is
  somewhere awkward, such as floating in a cave it would have to climb into, unless
  the route there happens to be walkable.
- The count only sees items already carried, so it cannot stop before one break.
- Sight only reads 21 blocks cubed around you every tick, which is the single
  largest cost in the feature.
- `mining.exposedRadius` above about 3 is very slow, because every candidate has
  that many surrounding blocks read.

## farm

Harvests nearby crops and puts them back in the ground. Available as `$farm`,
with the aliases `farming` and `harvest`.

```
$farm
$farm 32
```

With no argument it works everywhere it can see. With a range it works only
within that many blocks of where the player was standing when it was typed. A
range of `0` means everywhere, the same as leaving it off.

Farming runs until `$stop`, or until nothing it wants can be reached. It is not a
route with an end point, so it does not announce arrival; it either keeps finding
work or gives up.

Running out of work is not giving up. Crops that are not ripe yet are still on the
field, and a field is not finished just because everything harvestable has been
taken. Once there is nothing ripe left, farming says so once, stands still, and
keeps looking. The blocks it found stay on its list and are re-read every tick, so
anything that ripens is picked up as soon as it is ready, with nothing typed
again.

Farming is the only thing driving HELM while it runs. `$goto`, `$autogoto`,
`$autolookat` and starting a macro each end the farm first, because they need
control of where the player goes and which way they face, and farming has both
for as long as it lasts.

### Looking around

The world is scanned outwards from the player in widening rings of chunks, and
inside each chunk the sections are visited nearest the player's own level
first. The scan looks for every crop that can be harvested, plus farmland and
jungle logs when `farm.replantAfterHarvest` is on, plus soul sand when
`farm.replantNetherWart` is also on.

The scan stops in one of three ways:

| Why it stopped | What that means |
| --- | --- |
| A whole ring was unloaded | Everything it can see is done |
| The target cap was reached and it left the player's level band | The cap is what stopped it |
| The target cap was reached and it is still finding crops at the player's level | It keeps going until it stops finding them near you |

That last rule is what stops a huge flat farm at the cap while still letting a
player standing in a tall column of crops sweep all of it.

The scan is the expensive part, and it is the only part that is spaced out.
`farm.rescanEveryTicks` controls how often, and `farm.maxTargets` controls the
cap. Everything else is re-checked against the live world every tick, so a crop
that is harvested leaves the work list immediately rather than waiting for the
next scan.

No scan happens while a route is being walked, because the targets on that route
are already known. The next scan runs on the first tick after the route ends, so
walking a long route defers the scan rather than skipping it.

A scan does not finish in one tick. Reading every block in that many chunks is
slow enough to be felt as a hitch, so the scan spends a small slice of each tick
instead and carries on from where it left off, starting with the chunk the player
is standing in and widening from there.

The positions found so far are used as soon as they exist, not when the scan
finishes, so `$farm` starts working almost immediately and keeps discovering more
as the scan widens. Nothing is decided until the scan has found something, so
`$farm` cannot report a field as empty before it has looked at it.

Once one scan has finished, the positions it found stay in use while the next one
runs. A scan starts empty and fills up over a few ticks, so using its part-finished
result instead would drop most of the known field from the outlines and from the
targets every time a rescan began. A new scan is used as it grows only when there
is nothing to lose, which is the very first scan of a farm.

Building the list of things to walk to is also not free, since it has to look
at every dropped crop in the level to decide what is worth picking up. That only
happens when a route is actually needed, not on every tick.

Only loaded chunks are read. Chunks the player has never been near are not
scanned, fetched or remembered, so `$farm` never causes the client to load the
world around you.

### Sorting what it found

Every position the scan returned is sorted into one of five lists, in this
order. The first rule that matches wins, and a position is only ever in one
list.

| Order | The position is | Because |
| --- | --- | --- |
| 1 | Empty farmland | It is farmland and the block above it is air |
| 2 | Bare soul sand | It is soul sand and the block above it is air |
| 3 | A bare jungle log | It is a jungle log with air beside it |
| 4 | Ready to harvest | The crop is grown |
| 5 | Worth bone meal | It can grow from bone meal and would actually grow |

Positions outside the range are dropped first, before any of this.

The two soil rules mean HELM knows the difference between farmland that wants
something planted in it and farmland a crop is already standing in. The same
applies to soul sand and nether wart.

A jungle log needs an open side to plant cocoa onto. Without one there is
nowhere to put the beans, so it is not a target at all.

Bone meal is the last resort because it works on anything growable, which
includes a lot of blocks that were never meant to be farmed. The check asks
whether the block would actually grow from bone meal here, not merely whether it
can be fed.

### What counts as ripe

| Crop | Ready when |
| --- | --- |
| Wheat, carrots, potatoes, beetroot | Fully grown |
| Pumpkin, melon | Always, they have no age |
| Nether wart | Fully grown |
| Cocoa | Two stages grown |
| Sugar cane, bamboo, cactus | Only the top block of a stalk, when replanting is on |

Sugar cane, bamboo and cactus are the interesting case. When
`farm.replantAfterHarvest` is on, HELM harvests them the way a player would: it
takes the top block and leaves the stalk standing to grow back. So a block of
cane is only a target if the block below it is cane. With replanting off, every
block of the stalk is a target and the whole thing is taken.

### Getting to the work

Anything not within reach is a goal to walk to, and the goals are built in the
same order the work is preferred:

| Order | Goal |
| --- | --- |
| 1 | Next to a ripe crop, but never on top of it |
| 2 | On the block above empty farmland, where the seed goes |
| 3 | On the block above bare soul sand |
| 4 | Beside a bare jungle log, on the open side |
| 5 | On a block that would grow from bone meal |
| 6 | On a dropped crop worth picking up |

A ripe crop is never approached from above. Standing on top of the block you are
about to break means standing on nothing, so the goal accepts any of the other
five sides, including below, and refuses only the one above.

Each goal type keeps the same shape as the equivalent walking goal, so farming
routes cost and search exactly the way `$goto` does. A farm with a hundred
targets is one goal that is satisfied by any of the hundred, priced as the
cheapest of them, which is what makes the search walk to the nearest job rather
than committing to one from across the field.

Only the categories the player can actually do are added. Empty farmland is only
a goal while a seed is in the hotbar, soul sand only with nether wart, bare logs
only with cocoa beans, and bone meal only with bone meal. Without the item
there is nothing to do at the position, so HELM does not walk over to it and
stand there.

Dropped crops are picked up simply by walking over them, which is why they are
goals at all. Only crops and their seeds count. A dropped dirt block or a sword
is not something farming is for.

### Working on one thing

Once a target is close enough to touch, farming stops walking and works on it.
Walking is resumed after, from wherever the player ended up.

Standing still while working does not give up the walk. The route it was
following, the search it was waiting on, and the step it was in the middle of are
all kept, and it picks the route back up exactly where it left off once nothing is
in reach any more. Giving those up instead would mean re-searching for every
crop, and in a field where something is nearly always in reach it would mean
never walking at all.

It always prefers to work on the first thing it can reach in its own order, so a
ripe crop within reach is taken before a bare field ten blocks away is planted.
The first reachable candidate in that order is the one that is worked on.

### Standing on a job it cannot do

The bot walks to a job and then cannot do it, usually because it is standing on the
very block it meant to work on and the thing it is holding does not suit it. That
is a normal situation, not a failure, and it must not send it round in circles.

So when it arrives and still finds nothing it can do, it stays where it is and
waits. It looks again when one of these happens:

| What changed | What it does |
| --- | --- |
| The player moved | Looks again from the new spot |
| It worked something | Looks again for what is now nearest |
| A rescan finished and found a different amount of work | Looks again for the new work |

Anything else, including the case where a rescan finished and found exactly the
same work as before, leaves it waiting. Searching again for the same work from the
same spot could only reach the same answer, and doing it once per tick printed a
line in chat and rebuilt every target on every tick.

While it waits it says `Nothing to harvest right now. Still watching.` once, and
says nothing further until it has something to do.

| Work | What it does |
| --- | --- |
| Harvest | Aims at the crop, switches to the right tool, then mines |
| Plant on soil | Aims at the top face, so the seed lands on the farmland not beside it |
| Plant on a log | Aims at the open side, so the beans stick to the face |
| Bone meal | Aims at the plant and uses it |

Aiming is not assumed to have worked. Before using anything, HELM checks that the
line from the player's eyes would actually strike the face it needs: upward for
soil, and the chosen open side for a log. If it would miss, that candidate is
skipped and the next one is tried.

### Aiming at a face, not just its middle

Planting, and planting on a log, both need a particular face of a block, so HELM
aims at a face rather than at the block. A face is not a single spot, and the
middle of one is often not the spot that is closest.

The middle of the top face of a block of farmland is half a block away from the
player on each horizontal axis. A block that is within reach on its near edge can
be well outside reach at its middle, so aiming only at the middle makes HELM give
up on farmland it could have planted. The same applies to a log.

So a face is treated as nine spots, near the corners, the middle of each edge, and
the middle of the face, and they are tried in the order of their distance from
the player's eyes. The nearest spot on the face is tried first, which is the one
most likely to be in reach, and the middle of the face is among the nine, so
nothing that used to work stops working.

A spot only counts if the line to it really does strike the required face of the
right block. A spot on the far edge of a face can be caught by the side of the
block instead, and that spot is passed over rather than used, which is what stops
HELM from planting on the side of the farmland.

Because the required face is the same whichever spot on it is struck, the seed
lands on the same block it did before.

### Where the reach limit comes from

Whether a block is close enough is first decided from the block the player is
standing on to the corner of the target block. That is a cheap test and is only
used to rule blocks out early. What actually decides is the line from the
player's eyes, measured against the reach setting and re-checked for each spot.

Mining and using go through the same aiming and control path a walk uses, so
free look, smoothing and the reach setting all apply to farming exactly as they
do to mining.

Items are taken from the hotbar, or from the off hand. The off hand only gets
used when the wanted item is nowhere in the hotbar, and then the main hand is
moved onto something harmless, because the main hand would otherwise place or
eat whatever the off hand is holding instead. Items further back in the inventory
are not used at all, so putting seeds straight into the hotbar is what makes
planting work.

### When it gives up

| Reason | Reply | What happens |
| --- | --- | --- |
| Nothing it wants could be reached | `Farm failed.` | Farming ends |
| The search could not be started | `Farm failed.` | Farming ends |

Both end the farm and clear the goal, so HELM does not keep searching for work
that is not there.

Running out of ripe crops is not on that list, because it does not end anything.
That case answers `Nothing to harvest right now. Still watching.` once, stands
still, and carries on watching the field until `$stop`.

Leaving the world ends a farm the same way `$stop` does, without a message, and
empties the dropped item cache.

### Settings

| Name | Default | What it does |
| --- | --- | --- |
| `farm.replantAfterHarvest` | `true` | Plant again whatever is harvested |
| `farm.replantNetherWart` | `false` | Plant nether wart again |
| `farm.rescanEveryTicks` | `5` | Ticks between scans |
| `farm.maxTargets` | `256` | Cap on blocks found by one scan |
| `farm.renderCropsESP` | `true` | Outline the ripe crops farming is working through |
| `farm.renderItemsESP` | `true` | Outline the dropped items farming still wants |

See [SETTINGS.md](SETTINGS.md) for what each one changes.

### Dropped item cache

Farming picks up what it broke. Which items that is, is worked out rather than
guessed from a list, so anything a server, mod or config changes about a crop
still gets collected.

Whenever HELM breaks a block it writes the position into a ledger, at most 256
positions, newest last. An entry is dropped once it is 200 ticks old, so a place
stays watched for ten seconds after HELM stops mining it, and mining the same
spot again refreshes it rather than adding a duplicate.

Each tick, if the ledger has anything in it, every dropped item within two blocks
of a watched position is looked at. An item that is not already known is checked
against those positions, and if it is close enough to one, its item type is
remembered as something HELM wants. Anything already known and still fresh is
skipped without being checked against the positions, which is what keeps this
cheap once a farm is running.

An item type stays wanted for five minutes, and that is refreshed every time
another one of that type turns up. The five minutes is the same lifetime a
dropped item has in the game, so nothing is forgotten while it is still lying on
the ground. The cache is emptied when the world is left, and it is not written to
disk, so a new session starts knowing nothing until HELM breaks something.

Two things feed off the same cache. Farming walks over and picks up any dropped
item on the ground that is worth having, and the same check decides whether the
item gets an outline. A drop that is still wanted is drawn glowing; one that has
not been wanted, or has been forgotten, is drawn normally.

### Drops that land in water

An item that falls in water does not sink. The game nudges it very gently
upwards every tick while it is in the fluid, so it rides on the surface instead of
settling on the bottom, and it is not resting on a block while it does.

Where HELM aims at a floating drop is therefore not where the drop is. The drop
rests on top of the water, so the block directly above it is air. Air is not a
place the bot can be, so aiming there would leave the drop unreachable and the
bot would quietly move on to the next job. Instead the drop is aimed at the water
block it is floating on, which is exactly the block a swimmer occupies.

That is the same block the path finder already treats as swimmable, so the route
runs out across the surface of the water and the bot swims. A drop in the middle
of a wide pool is reached the same way as one at the edge.

Aiming one block lower does not put the bot out of reach of the item. The area
the game collects from is the player's own box grown by half a block above and
below and a block to the sides, and a swimmer standing in the water block under a
drop is inside that.

A drop that is neither on the ground nor on water is left alone for now. That is
one still falling through the air, which would only be a moving target.

Mining works the same way as soon as it breaks blocks of its own, because both
go through the same breaking path. Nothing extra is needed for that.

### Known limitations

- Only farmland, soul sand and jungle logs are replanted. A harvested crop on any
  other block is left bare.
- Seeds, nether wart, cocoa beans and bone meal are only fetched from deeper in
  the inventory when `movement.allowInventory` is on. See [inventory](#inventory).
- The cache watches two blocks around a broken position. An item thrown further
  than that by the server, or teleported away, is not noticed.

## inventory

Moves items between the inventory and the hotbar while HELM is walking, so tools,
placement blocks and farm supplies can be used from anywhere you are carrying
them rather than from the nine hotbar slots alone.

Off by default. Turn it on with:

```
$set movement.allowInventory true
```

### What it changes

| Situation | With it off | With it on |
| --- | --- | --- |
| The only tool for a block is in slot 12 | Mines by hand, slowly | Swaps it into slot 0 and mines with it |
| The best tool for a block is in slot 12 | Uses whatever is on the hotbar | Swaps the better one in if it is genuinely faster |
| Every placement block is in the inventory | Refuses to bridge or pillar | Pulls one up and places it |
| Seeds are in the inventory | Bare farmland is not a target | Bare farmland becomes a target, and the seeds are pulled up to plant it |
| The best tool against stone is only in the inventory | Left where it is | Kept stocked in slot 0 |

The last row is the reason two slots are reserved.

### Two reserved slots

| Slot | Kept holding |
| --- | --- |
| `0` | The fastest tool you carry against stone |
| `8` | A block from `movement.placementBlocks` |

Both are stocked in the background rather than on demand, so by the time a route
needs a pickaxe or a bridge block it is usually already in hand and nothing is
swapped at all. Stocking only happens when the hotbar has nothing suitable, so
it never displaces something you put there yourself.

Everything fetched on demand goes to a spare slot between 1 and 7, preferring an
empty one, so slots 0 and 8 keep their jobs. Which spare is used is picked at
random among the free ones, rather than always the first, so a long walk does not
keep landing on the same slot and overwriting it.

### When a swap happens

A swap is a real container click, sent to the server. It is never done more than
one at a time, and it waits for these before going out:

| Gate | Setting | Default |
| --- | --- | --- |
| Enough ticks have passed since the last one | `movement.ticksBetweenInventoryMoves` | `1` |
| The player has stopped, if asked to | `movement.inventoryMoveOnlyIfStationary` | `false` |
| Nothing else is open | | |

The last one matters. If a chest, a crafting table or an ender chest is open, the
visible container is not your own inventory and swapping slots would move the
wrong things. HELM leaves the inventory alone entirely while any other container
is open.

A swap that cannot go out yet is remembered and retried on the following tick,
so a fetch that is one tick short of the gap still happens rather than being
dropped. Something the current step needs takes priority over background
stocking.

`movement.inventoryMoveOnlyIfStationary` exists for servers that dislike
container clicks while the player is moving. Turning it on means HELM only swaps
once you have genuinely stopped, at the cost of a few ticks of delay.

### Choosing what to fetch

| Need | Order of preference |
| --- | --- |
| A tool | Whatever mines the block fastest, preferring the hotbar over the rest of the inventory |
| A placement block | A `movement.placementBlocks` entry from anywhere, then any placeable block at all |
| Seeds, nether wart, cocoa beans, bone meal | The hotbar, then the off hand, then the rest of the inventory |

Tools are compared on actual mining speed against the block in front of you, so
a tool only comes out of the inventory when it really is faster than what you are
already holding. The item saver and the sword setting are respected the same way
they are for the hotbar, so a nearly broken tool or a sword is not fetched.

A block from the off hand is still usable for farm supplies without being in the
hotbar, which is why that is checked before the inventory: the main hand is moved
onto something harmless instead, and the off hand does the work.

### Cost of turning it on

- Container clicks are visible to the server. On a server that checks them, this
  is the setting most likely to be noticed.
- Slots 0 and 8 are HELM's to use. Anything you keep in them can be swapped out.
- The hotbar will be visibly rearranged the first time HELM walks with this on.

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

A colour setting takes `#RRGGBB`, `0xRRGGBB`, six hex digits on their own such as
`4CE0E0`, or a plain whole number. Colours are shown and stored as `#RRGGBB`, so
the file stays readable.

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

## outline

### What it is

Glowing silhouettes around the things HELM is working on: the blocks a route is
going to mine, the ripe crops farming is working through, and the dropped items
those crops broke into and are still worth collecting.

### How it looks

These are not lines drawn around boxes. Each block and item is drawn as its own
shape into the outline framebuffer the game already uses for glowing entities,
and the game then edges, blurs and blends that back over the world. That is the
same treatment a glowing mob gets, so a crop is a glowing crop and an ore block
is a glowing block, with the shape of the block itself rather than a wireframe
cube around it.

Because it goes through the game's outline pass, terrain hides a silhouette the
same way it hides a glowing mob. There is no through-walls version of this.
`path.blocksIgnoreDepth` still applies to the separate line boxes.

### What gets an outline

| Target | Where it comes from | Colour |
| --- | --- | --- |
| Blocks to break | The blocks the route being walked has to mine | `outline.breakColour` |
| Ripe crops | Every ripe crop the last farm scan found | `outline.cropColour` |
| Dropped items | Items the dropped item cache wants | `outline.dropColour` |

Each kind is asked separately, so a block that is both a path break and a ripe
crop is only drawn once. A position is only drawn if the block is still there,
and at most 512 are drawn in one frame.

An entity the game has already made glow is left alone, so a spectator outline
or a glowing mob keeps its own colour rather than being repainted.

### Settings

| Name | Default | What it does |
| --- | --- | --- |
| `outline.enabled` | `true` | Master switch for every silhouette |
| `outline.blocksToBreak` | `true` | Outline the blocks the current path is going to mine |
| `outline.breakColour` | `#E04C4C` | Colour around blocks that will be mined |
| `outline.cropColour` | `#B4E04C` | Colour around ripe crops |
| `outline.dropColour` | `#4CE0E0` | Colour around dropped items worth collecting |

`farm.renderCropsESP` and `farm.renderItemsESP` gate the two farm outlines. See
[SETTINGS.md](SETTINGS.md).

### Interaction

Turning `outline.enabled` off turns all of it off and leaves route drawing
alone. Turning `outline.blocksToBreak` off stops the block silhouettes while
`path.renderBlocksToBreak` keeps drawing line boxes, so the two can be used
independently.

### Cancellation and failure

There is nothing to cancel. These are a rendering layer over whatever else is
running, so `$stop` ends the walk or the farm and the silhouettes go with it
because their targets are gone. Nothing fails and nothing is retried; a block
that has already been broken, or is outside the loaded world, is simply not drawn.

### Known limitations

- Silhouettes are hidden by terrain, like glowing mobs are.
- Block silhouettes use the block's own model, so a block drawn only by a special
  renderer, such as a chest, contributes nothing.
- The item outline is only ever asked about dropped items. Entities are not
  outlined by this.

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
| `list` | list the macros in the macro folder |

There is no `stop` here on purpose. `$stop` is the only way to stop anything, so
there is one thing to remember rather than two that behave differently.

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
| `$stop` with nothing running | error, nothing changes |

Unavailable statements are reported rather than skipped, so a macro never
appears to run while quietly doing nothing. A refused line is never written, so a
mistake cannot corrupt a macro.

While a macro is open, a line that is not a command is read as macro syntax rather
than as an unknown command, because that is what typing there means. The error
lists the syntaxes available, so a mistyped `$exit` says what is valid instead of
leaving it to be guessed.

### Cancellation

`$stop` is the only way to stop anything, and it stops all of it: the walk, the
running macro, an anchored position and a locked angle. There is deliberately no
`$macro stop`, so there is one way to stop and no way to leave half of it
running. The reply says what was actually caught, so `$stop` with nothing running
is reported rather than silently accepted.

Leaving the world ends a macro too, and releases an anchor and a locked angle, so
rejoining does not leave the camera pinned.

When a macro stops for any reason, anything it was holding down is let go of. A
`hold` left at the end of a macro therefore cannot leave a key stuck after the
macro finishes.

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
