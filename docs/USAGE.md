# Usage

Type `$` in the chat box, then a command. Anything starting with `$` is handled
by HELM and never sent to the server. Anything else is normal chat.

## Commands

| Command | Aliases | Arguments |
| --- | --- | --- |
| `$help` | `h`, `?` | none |
| `$version` | `ver` | none |
| `$goto <x> <y> <z>` | `g`, `go`, `to` | three whole numbers |
| `$autogoto <x> <y> <z>` | `agoto` | three whole numbers |
| `$autogotohere` | `agotohere` | none |
| `$autolookat <pitch>/<yaw>` | `alookat` | one angle |
| `$autolookathere` | `alookathere` | none |
| `$farm [<range>]` | `farming`, `harvest` | optional whole number of blocks |
| `$mine [<count>] <blocks>...` | `dig`, `excavate` | optional whole number, then one or more block names |
| `$stop` | `cancel`, `abort`, `halt` | none |
| `$set` | `setting` | none, opens the picker |
| `$set <name>` | `setting` | one setting name |
| `$set <name> <value>` | `setting` | one setting name, one value |
| `$settings reset` | `defaults` | none |
| `$exitEditMode` | `exit`, `stopEdit`, `exitEdit` | none |
| `$macro <subcommand>` | `macros` | see below |
| `$macro create <name>` | | one name |
| `$macro edit [<name>]` | | one name, or none to close |
| `$macro load <name>` | | one name |
| `$macro list` | | none |
| `$macro action <subcommand>` | `a` | see below |
| `$macro action add [<syntax>]` | `a` | one line to add |
| `$macro action remove <line>` | `rm`, `delete` | one line number |
| `$macro action move <from> <to>` | | two line numbers |
| `$macro action list` | | none |
| `$macro exitEditMode` | `exit`, `stopEdit`, `exitEdit` | none |

`$macro action` and `$exitEditMode` are only offered while a macro is open.

## Replies

| Reply | Means |
| --- | --- |
| `Searching for a way to x y z.` | Accepted, the search is running |
| `Path found: N steps.` | The route reaches the goal |
| `Partial path: N steps.` | The route falls short and will be re-planned |
| `Already at x y z.` | Your feet are already on the goal |
| `Arrived at x y z.` | The walk brought you onto the goal |
| `No path to x y z.` | Nothing walkable connects you to that block |
| `Holding x y z.` | An anchored walk was started, or you were already on it |
| `Holding p/y.` | Your facing was locked |
| `Farming everywhere.` | `$farm` started with no range |
| `Farming N blocks.` | `$farm N` started and will work within `N` blocks |
| `Nothing to harvest right now. Still watching.` | Everything it found is already harvested and the rest is still growing |
| `Farm failed.` | Nothing it wanted could be reached |
| `Walking stopped. Look released.` | `$stop` caught a walk and a held facing |
| `Walking stopped. Macro stopped.` | `$stop` caught a walk and a running macro |
| `Look released.` | `$stop` caught a held facing |
| `Macro stopped.` | `$stop` caught a running macro |
| `Farming stopped.` | `$stop` caught a running farm |
| `Mining stopped.` | `$stop` caught a running mine |
| `Mining <blocks>.` | `$mine` started |
| `Have N of <blocks>.` | The count was reached, so mining stopped |
| `No <blocks> left to mine.` | Nothing left that was worth mining, so mining stopped |
| `No way to reach any of <blocks>.` | Every known place was unreachable and skipping is off |
| `Stopped.` | `$stop` caught a walk |
| `Stopped searching.` | `$stop` caught an unfinished search |
| `Nothing to stop.` | Nothing was walking, held or running |
| `Unknown command: name` | No such command |
| `Unknown setting: name` | No such setting |
| `Not a block in the game: name` | A block list had a name that is not a block, so nothing changed |
| `Name = value` | `$set` changed one setting, or was asked about one |
| `No macro named name` | No such macro |
| `'loop' is missing 'endloop'` | The open macro has an unclosed loop |

Every reply is written to chat prefixed with `[HELM]`, so HELM's own output is
never mixed up with chat or with the server.

The `$set` reply is the setting's name and its new value, nothing more. What the
setting does is already in the picker, so it is not repeated in chat.

## Examples

```
$goto 120 64 -35
$goto -2 88 -117
$stop
```

```
$help
$version
```

```
$set
$set movement.allowBreak
$set movement.allowBreak false
$set movement.sprintAllowed true
$set movement.allowInventory true
$settings reset
```

A flag value is one of `true`, `false`, `yes`, `no`, `on`, `off`, `1`, `0`.

```
$farm
$farm 32
$stop
```

## Mining

`$mine` looks for blocks of a given type and mines them until they run out, until
a count is reached, or until `$stop`.

```
$mine oak_log
$mine 64 diamond_ore
$mine oak_log birch_log
$mine 128 iron_ore gold_ore
```

With no count it mines every one it can find. With a count it stops once that many
of the matching items are carried. More than one block name mines all of them,
nearest first.

The picker of every block in the game opens once you start a block name. A count
comes first and the picker stays closed while it is typed. Pressing space at the end
of a block name closes it again, since there is nothing to narrow down, and typing
the next name brings it back:

```
$mine 30
$mine 30 oak
```

Block names are the game's own, with or without the `minecraft:` prefix. A single
state can be picked out with square brackets, and more than one at a time separated
by commas:

```
$mine oak_log[axis=x]
$mine oak_log[axis=x,waterlogged=false]
```

Those work the same way as they do in vanilla commands. `Tab` completes the
highlighted block into the command, and an unreadable name is reported without
changing anything.

```
$mine oak_[Tab] [Space] oak_[Tab]
```

Every block it has found is outlined while it works, and so is the block it is
breaking. Turn those off with:

```
$set mining.renderTargets false
$set outline.blocksToBreak false
$set outline.enabled false
```

There are three ways it mines, and it uses whichever reaches the block first:

| Way | What it does |
| --- | --- |
| Break what is overhead | When the block is straight above you, stand still and break it |
| Walk to it | Search for a route, breaking and placing as needed, then mine on the way |
| Follow a drop | Walk over a dropped item of the right type and pick it up |

Overhead breaking is what takes a tree down. Turn it off with:

```
$set mining.breakOverhead false
```

`$mine` drives HELM for as long as it lasts. `$goto`, `$autogoto`, `$autolookat`,
`$farm` and starting a macro each end the mine first, because they need control of
where you walk and which way you face.

```
$set mining.maxTargets 32
$set mining.onlyExposed true
$set mining.sightOnly true
$set mining.exploreWhenUnknown false
```

### Where it looks

Remembered chunks are searched first, since they cover ground you have already
been to. Anything they do not hold is looked for in the chunks around you.

| Setting | Default | What it does |
| --- | --- | --- |
| `mining.maxTargets` | `64` | Most places remembered at once |
| `mining.rescanEveryTicks` | `5` | Ticks between looking again, `0` looks once |
| `mining.scanRadius` | `32` | Chunks around you that are read |
| `mining.scanLevelWindow` | `10` | How far from your level the scan keeps looking |
| `mining.cacheScanRadius` | `2` | How far around you remembered chunks are searched |
| `mining.cacheScanLimit` | `10` | How many are found there before it stops widening |
| `mining.repackRadius` | `40` | Chunks remembered before mining starts |

When nothing is found anywhere it looks, and `mining.exploreWhenUnknown` is on, it
walks away from where you started and keeps its distance while it does, so new
chunks load and something turns up. `mining.stripLevel` is the level it holds while
doing that, and `mining.exploreWhenUnknown false` stops it entirely.

Turning it off does not mean giving up at once. `mining.skipUnreachable` is on by
default, so when no route can be found the closest block is marked unreachable and
the next one is tried. Turn that off and the first failure ends the job.

```
$set mining.lowestLevel -59
$set mining.highestLevel 64
```

Levels are absolute, not relative to the bottom of the world, and
`mining.lowestLevel 0` means the bottom of whatever dimension you are in.

`mining.sightOnly` never acts on a block you cannot actually see, which reads much
less like seeing through stone. It keeps looking rather than giving up when nothing
turns up, and `mining.sightDiagonals` lets it also accept a block that only touches
one it can already see.

`mining.onlyExposed` requires air or liquid touching the block, within
`mining.exposedRadius`. Higher radii are much slower to check.

See [SETTINGS.md](SETTINGS.md) for the rest.

## Farming

`$farm` looks around for ripe crops and works through what it finds, one thing at
a time. It keeps going until `$stop`, or until nothing it wants can be reached.

Every ripe crop `$farm` finds is outlined, and so is every dropped item it broke
its crops into and still wants. Turn those off with:

```
$set farm.renderCropsESP false
$set farm.renderItemsESP false
$set outline.enabled false
```

When it has harvested everything that is ripe, it does not stop. It says so once,
stands where it is, and keeps checking the field as the rest grows. Anything that
ripens is picked up as soon as it is ready, without typing the command again.

```
$farm
```

Without a range it works everywhere it can see. A range limits it to that many
blocks from where you stood when you typed it:

```
$farm 32
```

What it does, in the order it prefers:

| Order | What |
| --- | --- |
| 1 | Break ripe crops, switching to the right tool first |
| 2 | Plant seeds on empty farmland, and nether wart on bare soul sand |
| 3 | Plant cocoa beans on bare jungle logs |
| 4 | Use bone meal on anything that would grow from it |
| 5 | Walk over and pick up the drops it broke its own crops into |

Ripe means fully grown wheat, carrots, potatoes and beetroot, any pumpkin or
melon, nether wart at full age, and cocoa at full age. Sugar cane, bamboo and
cactus are only harvested from the top of a stalk while
`farm.replantAfterHarvest` is on, so the rest is left to grow.

Seeds, nether wart, cocoa beans and bone meal are taken from the hotbar, or from
the off hand. Seeds are only planted while one is actually held, so HELM does
not walk across a field to stand on bare dirt it cannot plant.

`$farm` drives HELM for as long as it lasts. `$goto`, `$autogoto`, `$autolookat`
and starting a macro each end the farm first, because they need control of where
you walk and which way you face.

```
$set farm.replantAfterHarvest false
$set farm.replantAfterHarvest true
```

```
$macro create farm
$macro edit farm
$macro action add gotohere
$macro action list
$macro load farm
$stop
$macro list
$macro edit
```

## Anchoring and locking

Four commands hold on to something instead of doing it once. All of them are
released by `$stop`.

| Command | What it holds |
| --- | --- |
| `$autogoto <x> <y> <z>` | A block position |
| `$autogotohere` | The block your feet are in |
| `$autolookat <pitch>/<yaw>` | An angle |
| `$autolookathere` | The angle you are facing |

An anchored position is not just a walk. Once you arrive, HELM checks every tick
that you are still standing on it, and if anything moves you off, by another
player or a bomb, it searches again and walks back. A short delay is applied
before it reacts, so being nudged does not start a search on every tick.

A locked angle is enforced twice a tick, once before the player moves and once
after, so your mouse cannot move the camera. Mining and placing still work while
it is held, because the lock is applied to the camera rather than to the
rotation HELM sends.

A plain `$goto` releases an anchored position, so the two never fight over where
the player should be.

```
$autogotohere
$autolookathere
$stop
```

## Editing keys

| Key | What it does |
| --- | --- |
| `↑` `↓` | Move the cursor |
| `←` `→` | Move between columns, while a macro is open |
| `Tab` | Complete the highlighted entry into the command |
| Click | Choose the highlighted entry |
| `Enter` | Confirm and send |
| `Esc` | Close the picker |

Anything typed in the chat box is filtered live as you type, and the command
suggestions strip appears once `$` and a space are typed.

## Choosing blocks

Some settings are a list of blocks rather than a single value. Typing one of
those names opens a picker of every block in the game:

```
$set movement.placementBlocks
```

The picker has two columns. The left is every block in the game, narrowed as you
type, and shown by its full name such as `minecraft:oak_planks` so there is no
guessing about whether a space belongs in it. The right is the blocks currently
on the list, in order, and it does not change as you type.

It is there to help you type, not to do the work for you. Type the blocks you
want after the setting name and press `Enter`:

```
$set movement.placementBlocks dirt cobblestone oak_planks
```

That replaces the whole list, so only those three are chosen. The left column
filters on the last word you typed, and it accepts either `oak_planks` or
`oak planks` when searching, but only the underscored name is one block.

`Tab` completes the highlighted block into the command, so a list can be built
without spelling anything out in full. Start a word, press `Tab`, press `Space`,
press `Tab` again for the next one:

```
$set movement.placementBlocks dirt [Tab] [Space] oak_planks [Tab]
```

Completing a word that is already complete changes nothing, so pressing `Tab`
again on the same block is harmless.

Spaces or commas both separate blocks, names may be written bare or with the
`minecraft:` prefix, and anything that is not a block in the game is ignored. The
order the blocks are in is the order they are preferred in.

Nothing is chosen until you send the command. `Space` types a space, so a list
can be written out normally, and clicking a row leaves the setting alone so you
can still put the cursor where you want it.

## Your data

```
<game directory>/HELM/
```

`.minecraft/HELM` on Linux and macOS, `%APPDATA%\.minecraft\HELM` on Windows.

| Path | What it holds |
| --- | --- |
| `HELM/macros/` | Your macro files |
| `HELM/settings.conf` | Every setting, one per line |
| `HELM/debuglogs.log` | A record of what HELM did this session |
| `HELM/cache/` | Remembered chunks |

Attach `HELM/debuglogs.log` to a bug report.

## Further reading

| Document | Contents |
| --- | --- |
| [FEATURES.md](FEATURES.md) | One line per feature |
| [FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) | How everything behaves |
| [SETTINGS.md](SETTINGS.md) | Every setting, its default and what it does |
| [MACROSYNTAX.md](MACROSYNTAX.md) | The macro script syntax |
