# Settings

Every part of HELM that can be changed is a setting. Settings are grouped into
five sections, and the name of a setting is its section, a dot, and the name
inside it.

| Section | Covers |
| --- | --- |
| `movement` | Walking, mining, placing, pathing, tools, timing |
| `mining` | Tool choice and what to avoid breaking |
| `look` | Aiming, free look, smoothing, reach |
| `path` | Drawing the route and search limits |
| `cache` | Remembering chunks that are no longer loaded |

## Reading and changing

```
$set
$set movement.allowBreak
$set movement.allowBreak false
$settings reset
```

`$set` on its own opens a picker listing every setting in one scrollable column,
with the setting you are pointing at explained beside it. Arrow keys move, the
mouse wheel scrolls, tab or a click chooses, and typing filters the list, so
`$set look.` leaves only the `look` settings. The full list below is the same
set.

`$set <name>` prints one setting and its value. `$set <name> <value>` changes it,
taking the value as one word: `true` or `false`, a whole number, a decimal, or
free text. A flag also accepts `yes`, `no`, `on`, `off` and `1` or `0`.

`$settings reset` puts every setting back to its default, and is also
`$settings defaults`. `set` is also `setting`.

Values are clamped to the range a setting allows, so a value outside the range
is corrected rather than rejected. A value that is not a number where a number
is expected is an error and nothing is changed.

Changing a setting takes effect immediately. Changing a tool or pathing setting
re-prices the walk that is in progress, so a change can affect a route that is
already being walked.

## Where they are stored

```
<game directory>/HELM/settings.conf
```

One `name = value` per line, written as UTF-8, written by atomically replacing
the file so a crash mid write cannot leave a half written file behind. The file
is plain text and safe to edit by hand.

If the file is missing, every setting starts at its default. If a line cannot be
parsed, that one setting keeps its default and the rest still load. If the file
cannot be written, HELM says so once in chat and the change still applies for
that session.

## movement

### What may be done

| Name | Default | What it does |
| --- | --- | --- |
| `movement.allowBreak` | `true` | Mine blocks that are in the way |
| `movement.allowPlace` | `true` | Place blocks to bridge or step up |
| `movement.placementBlocks` | dirt, cobblestone, netherrack, stone | Blocks HELM may place when a path needs one |
| `movement.allowParkour` | `true` | Jump across gaps |
| `movement.allowParkourPlace` | `true` | Place a block at the end of a failed parkour jump |
| `movement.allowParkourAscend` | `true` | Sprint up one block while parkouring |
| `movement.allowDownward` | `true` | Allow digging straight down to travel |
| `movement.allowDiagonalAscend` | `true` | Step up while moving diagonally |
| `movement.allowDiagonalDescend` | `true` | Step down while moving diagonally |
| `movement.allowJumpAtBuildLimit` | `false` | Allow parkour jumps at the very top of the world |
| `movement.walkWhileBreaking` | `true` | Keep walking forward while mining ahead |
| `movement.pauseMiningForFallingBlocks` | `true` | Wait for sand and gravel to settle before continuing |
| `movement.splicePath` | `true` | Join a newly found route onto the one being walked |
| `movement.overshootTraverse` | `true` | Accept ending one or two blocks past a flat step |
| `movement.sprintAscends` | `true` | Sprint into a step up where possible |
| `movement.sprintInWater` | `true` | Sprint while in water |
| `movement.allowOvershootDiagonalDescend` | `true` | Sprint diagonally off a descending step |
| `movement.assumeStep` | `false` | Never jump while stepping up, assuming the game steps you |
| `movement.assumeSafeWalk` | `false` | Sneak while back placing, assuming the game handles edge safety |

### What may be walked on

| Name | Default | What it does |
| --- | --- | --- |
| `movement.allowWalkOnMagmaBlocks` | `false` | Treat magma as walkable, at a slow sneak pace |
| `movement.allowVines` | `false` | Treat vines as a walkable surface |
| `movement.allowWalkOnBottomSlab` | `true` | Allow standing on bottom slabs |
| `movement.assumeWalkOnWater` | `false` | Treat water surfaces as solid ground |
| `movement.allowWaterBucketFall` | `false` | Treat a fall into water as survivable at any height |
| `movement.maxFallHeightNoWater` | `3` | Longest fall still considered, in blocks |
| `movement.maxFallHeightBucket` | `60` | Longest survivable fall with a water bucket |

Turning `movement.allowWalkOnBottomSlab` off makes paths more reliable at the
cost of refusing to use bottom slabs. Turning `movement.assumeStep` on makes
step ups smoother on servers that step you automatically, and wrong on servers
that do not.

### Costs

These change which route is chosen, not just how fast it is walked.

| Name | Default | What it does |
| --- | --- | --- |
| `movement.blockPlacementPenalty` | `20.0` | Cost of placing one block |
| `movement.blockBreakAdditionalPenalty` | `2.0` | Added to every block mined |
| `movement.jumpPenalty` | `2.0` | Added to every jump, because jumping costs hunger |
| `movement.walkOnWaterOnePenalty` | `3.0` | Added to stepping onto a water surface |
| `movement.avoidBreakingMultiplierEnabled` | `false` | Treat the avoid list as air |
| `movement.avoidBreakingMultiplier` | `0.1` | How cheap breaking an avoided block is |

Raising the placement penalty makes HELM prefer mining over placing. Raising the
break penalty makes it break as few blocks as possible.

### Choosing which blocks to place

`movement.placementBlocks` is the list of blocks HELM is allowed to place when a
route needs one put down: bridging a gap, stepping up, pillaring, or placing at
the end of a failed parkour jump. One list covers all of them, so it is set once
rather than per move.

The default is dirt, cobblestone, netherrack and stone, in that order. The order
matters: earlier blocks win when several of them are in the hotbar.

It is a list of blocks, so it opens a picker of every block in the game rather
than taking a single value. The picker has two columns: every block on the left,
narrowed as you type and shown by its full name such as `minecraft:oak_planks`,
and the blocks currently on the list on the right. It is a search aid, not an
editor: nothing is chosen until the command is sent.

Type the blocks after the setting name and press `Enter`. That replaces the
whole list:

```
$set movement.placementBlocks dirt cobblestone oak_planks
```

Spaces or commas both separate blocks. Names may be written bare or with the
`minecraft:` prefix, in any case, and anything that is not a block in the game is
ignored. Leaving the list empty means no block on it is preferred, so HELM falls
back to whatever placeable block is in the hotbar.

### Timing and giving up

| Name | Default | What it does |
| --- | --- | --- |
| `movement.movementTimeoutTicks` | `100` | Ticks one step may take before the walk is abandoned |
| `movement.maxCostIncrease` | `10.0` | How much dearer a step may get before the walk is abandoned |
| `movement.costVerificationLookahead` | `5` | How many steps ahead are checked for having become impossible |
| `movement.maxPathHistoryLength` | `300` | Moves walked before the earliest ones are discarded |
| `movement.pathHistoryCutoffAmount` | `50` | Moves discarded when the history is trimmed |
| `movement.blockBreakSpeed` | `6` | Ticks between mining attempts |
| `movement.rightClickSpeed` | `4` | Ticks between placing attempts |
| `movement.ticksBetweenInventoryMoves` | `1` | Ticks between inventory moves |
| `movement.allowInventory` | `false` | Allow rearranging the inventory while walking |
| `movement.inventoryMoveOnlyIfStationary` | `false` | Stop moving before rearranging the inventory |

Lowering `movement.movementTimeoutTicks` makes HELM give up sooner on a step
that is stuck, which is useful on a laggy server. Raising
`movement.maxCostIncrease` makes it more tolerant of the world changing under
it.

### Tools

| Name | Default | What it does |
| --- | --- | --- |
| `movement.considerPotionEffects` | `true` | Account for haste and mining fatigue when pricing breaks |
| `movement.assumeExternalAutoTool` | `false` | Never switch tools, because another mod already does |

If another mod already switches tools for you, turning
`movement.assumeExternalAutoTool` on stops HELM fighting it.

## mining

| Name | Default | What it does |
| --- | --- | --- |
| `mining.autoTool` | `true` | Switch to the fastest tool for every block mined |
| `mining.preferSilkTouch` | `false` | Prefer a silk touch tool when it is no slower |
| `mining.useSwordToMine` | `true` | Allow swords to be chosen as a mining tool |
| `mining.itemSaver` | `false` | Stop using a tool once it is nearly broken |
| `mining.itemSaverThreshold` | `10` | Durability left on a tool when the item saver stops using it |
| `mining.avoidBreaking` | *(empty)* | Comma separated block names the search routes around |
| `mining.considerPotionEffects` | `true` | Account for haste and mining fatigue |

`mining.avoidBreaking` takes block names, with or without the `minecraft:`
prefix, comma separated. Unknown names are ignored rather than causing an error,
so a typo in one entry does not break the rest:

```
$set mining.avoidBreaking "chest,dirt,minecraft:oak_log"
```

A listed block is treated as something the player will not walk through and will
not break, so the search routes around it rather than through it. Put the blocks
you would rather not have mined here, for example `cherry_leaves` or
`short_grass`, and HELM will path around them instead of tunnelling through.

`movement.avoidBreakingMultiplier` then makes breaking one of them cheap rather
than impossible, for when going through is genuinely the shorter route.

Tool choice prefers the fastest tool, then the cheaper material, then a silk
touch tool if one is preferred and no slower. With `mining.itemSaver` on, a tool
that is nearly broken is skipped entirely, which means HELM will walk a longer
way rather than destroy a pickaxe.

## look

| Name | Default | What it does |
| --- | --- | --- |
| `look.freeLook` | `true` | Send aiming to the server rather than turning the camera |
| `look.blockFreeLook` | `false` | Free look stays on while a block is being mined |
| `look.elytraFreeLook` | `true` | Free look stays on while gliding |
| `look.smoothLook` | `false` | Camera yaw follows an average of recent server yaw |
| `look.elytraSmoothLook` | `false` | Camera follows the average while gliding |
| `look.smoothLookTicks` | `5` | How many recent rotations the camera averages |
| `look.remainWithExistingLookDirection` | `true` | Prefer the direction the player is already facing |
| `look.antiCheatCompatibility` | `true` | Rotations are sent to the server rather than applied on the client |
| `look.randomLooking` | `0.01` | Degrees of random yaw and pitch added every tick |
| `look.randomLooking113` | `2.0` | Occasional larger random yaw offset |
| `look.blockReachDistance` | `4.5` | How far away a block may be and still be mined |

With `look.freeLook` off, the camera turns to face the route while walking and
to face what is being mined and placed. That is what to use when the server
refuses client side rotations.

`look.antiCheatCompatibility` only has an effect while `look.freeLook` is on.
With both on, which is the default, a walk sends the angle to the server and
leaves the camera alone. Turning `look.antiCheatCompatibility` off while leaving
`look.freeLook` on stops the angle being sent at all, which is the only way to
turn aiming off completely. Mining still turns the camera either way, because
`look.blockFreeLook` is off by default.

With `look.randomLooking` and `look.randomLooking113` both zero, aiming is
exact, which is more conspicuous.

`look.blockReachDistance` is how far HELM is willing to reach for a block. Lower
it if the server enforces a shorter reach than the client thinks it has.

## path

| Name | Default | What it does |
| --- | --- | --- |
| `path.enable` | `true` | Whether navigation is allowed to run at all |
| `path.renderPath` | `true` | Draw the route |
| `path.renderPathAsLine` | `false` | Draw a plain line instead of a ribbon |
| `path.renderGoal` | `true` | Draw the goal |
| `path.renderBlocksToBreak` | `true` | Outline blocks to mine |
| `path.renderBlocksToPlace` | `true` | Outline blocks to place |
| `path.fadePath` | `false` | Fade the route out with distance |
| `path.lineWidth` | `5.0` | Thickness of the route line, in pixels |
| `path.goalLineWidth` | `3.0` | Thickness of the goal line, in pixels |
| `path.primaryTimeoutMillis` | `500` | Milliseconds the search may run before it has moved away from the start |
| `path.failureTimeoutMillis` | `2000` | Milliseconds the search may run in total before it gives up |
| `path.maxChunkBorderFetch` | `50` | How many moves into unloaded chunks the search may consider |
| `path.costHeuristic` | `3.563` | Price the search gives each block of distance remaining. Matches the cost of one block of sprinting |
| `path.simplifyUnloadedGoal` | `true` | When the goal is in an unloaded chunk, aim for that column at any height so the walk still heads towards it |
| `path.repropagateImprovement` | `true` | Require a minimum cost improvement before a node is revisited |
| `path.cutoffAtLoadBoundary` | `false` | Drop the tail of a route that runs into unloaded chunks |
| `path.cutoffMinimumLength` | `30` | Routes shorter than this are never shortened |
| `path.cutoffFactor` | `0.9` | How much of a long unfinished route is kept |
| `path.ignoreDepth` | `true` | Draw the route even where terrain is in the way |
| `path.blocksIgnoreDepth` | `true` | Draw the blocks to break and place through terrain |

Turning `path.renderPath` off leaves walking working with nothing drawn, which is
useful when another mod is already drawing something.

The route is drawn as a translucent line, at 40% opacity, with a flat top edge so
it reads as a narrow ribbon rather than a bare wire. A run of steps in the same
direction is drawn as one straight line rather than as a separate segment per
step, so a long straight walk looks straight. With `path.renderPathAsLine` on,
the top edge is dropped and only the centre line is drawn.

Both the route and the block outlines are drawn through terrain by default, so you
can see where you are going while standing somewhere else. Turn
`path.ignoreDepth` off to have the world hide them.

## cache

| Name | Default | What it does |
| --- | --- | --- |
| `cache.enabled` | `true` | Remember chunks to disk at all |
| `cache.preferLoadedChunks` | `true` | Read the live world first and fall back to the cache |
| `cache.pruneFromMemory` | `true` | Release cached regions more than 1024 blocks away |
| `cache.queueLimit` | `2000` | Chunks that may wait to be recorded at once |
| `cache.expirySeconds` | `-1` | Forget chunks older than this; below zero never expires |
| `cache.repackOnBlockChange` | `true` | Re-read a chunk when a tracked block in it changes |

`cache.enabled` and `cache.preferLoadedChunks` are read when a world loads, so
changing them takes effect the next time you join a world. The other four apply
immediately.

`cache.expirySeconds` is the setting to reach for if the cache is growing without
bound. Setting it to something like `604800`, one week, keeps recent terrain and
drops the rest.

See [FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) for what the cache
remembers and how it is read.
