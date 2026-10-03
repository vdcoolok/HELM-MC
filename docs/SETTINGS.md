# Settings

Every part of HELM that can be changed is a setting. Settings are grouped into
seven sections, and the name of a setting is its section, a dot, and the name
inside it.

| Section | Covers |
| --- | --- |
| `movement` | Walking, mining, placing, pathing, tools, timing |
| `mining` | Tool choice and what to avoid breaking |
| `look` | Aiming, free look, smoothing, reach |
| `path` | Drawing the route and search limits |
| `cache` | Remembering chunks that are no longer loaded |
| `farm` | What `$farm` harvests, replants and sweeps for |
| `outline` | The glowing silhouettes drawn around blocks and dropped items |

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

A colour setting takes `#RRGGBB`, `0xRRGGBB`, six hex digits on their own such as
`4CE0E0`, or a plain whole number. Colours are shown and stored as `#RRGGBB`.

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
| `movement.placementBlocks` | `dirt, cobblestone, netherrack, stone` | Blocks that may be placed |
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

`movement.placementBlocks` is a list of block names separated by spaces or commas,
and it opens a picker of every block. Earlier names win when several are in the
hotbar. A name that is not a block in the game is refused.

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
| `movement.ticksBetweenInventoryMoves` | `1` | Ticks between inventory moves, `0` allows one every tick |
| `movement.allowInventory` | `false` | Fetch tools, placement blocks and farm supplies from anywhere you carry them |
| `movement.inventoryMoveOnlyIfStationary` | `false` | Stop moving before rearranging the inventory |

Lowering `movement.movementTimeoutTicks` makes HELM give up sooner on a step
that is stuck, which is useful on a laggy server. Raising
`movement.maxCostIncrease` makes it more tolerant of the world changing under
it.

`movement.allowInventory` turns the whole inventory subsystem on, so a tool, a
placement block or a packet of seeds sitting in slot 20 is used rather than
ignored. Off is the default because it sends container clicks to the server,
which is the part of HELM a server is most likely to object to.

| Setting | What it controls |
| --- | --- |
| `movement.allowInventory` | Whether items may be moved into the hotbar at all |
| `movement.ticksBetweenInventoryMoves` | How many ticks must pass between two moves |
| `movement.inventoryMoveOnlyIfStationary` | Whether HELM must wait until you have stopped |

Nothing is rearranged while a chest, crafting table or any other container is
open, because the visible container would then not be your own inventory. Slots
`0` and `8` are kept stocked with the best tool against stone and with a
placement block, and anything fetched on demand goes to a spare slot between them.

See [FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) for what each part
does and what it costs.

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
| `mining.breakAllowedAnyway` | *(empty)* | Blocks `$mine` will still mine while breaking is off |
| `mining.maxTargets` | `64` | Most places remembered at once |
| `mining.lowestLevel` | `0` | Lowest level a target may be on |
| `mining.highestLevel` | `2031` | Highest level a target may be on |
| `mining.onlyExposed` | `false` | Require air or liquid touching the block |
| `mining.exposedRadius` | `1` | How far around a target to look for that air |
| `mining.sightOnly` | `false` | Never act on a block you cannot see |
| `mining.sightDiagonals` | `false` | With sight only, accept a block touching a visible one |
| `mining.stripLevel` | `-59` | Level held while exploring for unknown blocks |
| `mining.exploreWhenUnknown` | `true` | Walk away when no target is known |
| `mining.skipUnreachable` | `true` | Mark an unreachable target and try the next |
| `mining.rescanEveryTicks` | `5` | Ticks between looks, `0` looks once |
| `mining.scanWhenCacheThin` | `false` | Also read loaded chunks when the cache is thin |
| `mining.followDroppedItems` | `true` | Walk over drops of the right type |
| `mining.dropWaitMillis` | `250` | Milliseconds to wait after a break for a drop |
| `mining.digIntoVein` | `true` | Path into the block behind a target in the same vein |
| `mining.digThroughAir` | `true` | Count air beside a target as part of the vein |
| `mining.breakOverhead` | `true` | Break a block directly above without moving |
| `mining.stopRouteWhenMined` | `true` | Stop a route whose destination has gone |
| `mining.repackRadius` | `40` | Chunks remembered before mining starts |
| `mining.scanRadius` | `32` | Chunks around you read when looking |
| `mining.scanLevelWindow` | `10` | How far from your level the scan keeps looking |
| `mining.cacheScanRadius` | `2` | How far around you remembered chunks are searched |
| `mining.cacheScanLimit` | `10` | How many are found there before it stops widening |
| `mining.renderTargets` | `true` | Outline every block the job has found |

`mining.breakAllowedAnyway` takes block names the same way `mining.avoidBreaking`
does. While `movement.allowBreak` is off, `$mine` will only mine blocks on this
list, and refuses to start when none of the blocks it was given is on it:

```
$set mining.breakAllowedAnyway "chest,dirt,minecraft:oak_log"
```

The level settings are absolute, and `mining.lowestLevel 0` means the bottom of
whichever dimension you are in, so the same value works everywhere.

`mining.lowestLevel` and `mining.highestLevel` bound where a target may be at all,
so `$mine diamond_ore` will not chase one down below bedrock or up in the sky.

`mining.breakOverhead` is what takes a tree down without walking anywhere. With it
off, `$mine oak_log` approaches each log from the side and mines it the same way as
anything else.

`mining.sightOnly` forces mining to behave as a player would: only blocks that can
actually be seen from where you are standing become targets. It always keeps
looking, even when `mining.exploreWhenUnknown` is off, and it is much slower than
the usual search because the world has to be read directly rather than through
remembered chunks.

`mining.scanWhenCacheThin` additionally reads the chunks around you whenever the
remembered ones hold fewer than `mining.maxTargets`. It is off by default because it
costs a great deal more time for most worlds.

`mining.digThroughAir` only matters while `mining.digIntoVein` is on. With it off, a
single loose block stops the dig immediately because the air beside it is not part
of the vein.

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

## farm

| Name | Default | What it does |
| --- | --- | --- |
| `farm.replantAfterHarvest` | `true` | Plant again whatever is harvested |
| `farm.replantNetherWart` | `false` | Plant nether wart again |
| `farm.rescanEveryTicks` | `5` | Ticks between looking around for crops again |
| `farm.maxTargets` | `256` | Most blocks to look for in one sweep |
| `farm.renderCropsESP` | `true` | Outline the ripe crops farming is working through |
| `farm.renderItemsESP` | `true` | Outline the dropped items farming still wants |

`farm.replantAfterHarvest` is what makes farming worth doing. With it on, the
empty farmland and bare jungle logs the sweep finds become targets in their own
right, not just things to walk past. Turning it off also changes what counts as
ready for sugar cane, bamboo and cactus: on, only the top block of each stalk is
taken so the rest is left to grow; off, every block of a stalk is taken.

`farm.replantNetherWart` only has an effect while `farm.replantAfterHarvest` is
on. With it off, soul sand is never looked for, so nether wart is harvested and
left bare.

`farm.rescanEveryTicks` is how often HELM looks around for more crops when it has
nothing else to do. The positions from one sweep are kept and re-checked against
the live world every tick, so a block that is harvested disappears from the list
immediately. The sweep itself is the expensive part and is what this setting
spaces out.

While a route is being walked no sweep happens at all, because the targets it is
walking towards are already known. The next sweep runs on the first tick after
that route finishes, so nothing is missed, only deferred.

A sweep itself is spread over a second or two of ticks rather than done in one,
because reading every block in that many chunks at once is felt as a hitch. Its
results are used as soon as there are any, so `$farm` starts working straight
away rather than waiting for the sweep to finish. Setting this to `0` sweeps
once, when `$farm` starts, and never looks again, so anything that grows or is
planted after that will not be noticed.

`farm.maxTargets` caps how many blocks one sweep returns, so sweeping a huge flat
world cannot run away. The sweep stops as soon as it has this many, unless it is
still finding crops at your own level, in which case it keeps going until it
leaves that band or runs out of loaded chunks.

`farm.renderCropsESP` outlines every ripe crop the last sweep found, whether or
not HELM has walked to it yet, so the whole field is marked. Turning it off leaves
the route and the blocks to break drawn, and the crops still harvested, just not
glowing. `farm.renderItemsESP` outlines the dropped items HELM broke its own crops
into and still wants to collect. Neither has any effect unless `outline.enabled` is
also on.

See [FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) for what farming
harvests and in what order.

## outline

| Name | Default | What it does |
| --- | --- | --- |
| `outline.enabled` | `true` | Master switch for every glowing silhouette |
| `outline.blocksToBreak` | `true` | Outline the blocks the current path is going to mine |
| `outline.breakColour` | `#E04C4C` | Colour around blocks that will be mined |
| `outline.cropColour` | `#B4E04C` | Colour around ripe crops |
| `outline.dropColour` | `#4CE0E0` | Colour around dropped items worth collecting |
| `outline.targetColour` | `#E0C24C` | Colour around the blocks a mining job is working through |

`outline.enabled` off turns off every silhouette at once, including the two farm
toggles, and leaves the route drawing untouched.

`outline.blocksToBreak` draws the outline for the path the navigator is walking.
It is separate from `path.renderBlocksToBreak`, which draws a line box around the
same blocks and can be seen through walls, so both can be on at once or either
can be turned off.

The three colours are ordinary `#RRGGBB` values, and only the silhouette changes.
Silhouettes are drawn through the same outline pass the game uses for glowing
entities, so they are hidden by terrain rather than showing through it.
