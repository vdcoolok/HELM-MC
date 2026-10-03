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
| `$mine <blocks>... [<count>]` | `dig`, `excavate` | one or more block names, then an optional whole number |
| `$follow <names>...` | `chase`, `stalk` | one or more mob and player names |
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
| `Following <names>.` | `$follow` started |
| `Nothing left to follow.` | Nothing matched for `follow.waitTicks`, so following stopped |
| `Following stopped.` | `$stop` caught a running follow |
| `Expected at least one mob or player name.` | `$follow` was typed with no names |
| `Not a mob: name.` | An entity type was something other than a mob, so nothing changed |
| `Nothing here called name.` | No mob or player by that name, so nothing changed |
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
$mine diamond_ore 64
$mine oak_log birch_log
$mine iron_ore gold_ore 128
```

With no count it mines every one it can find. With a count it stops once that many
of the matching items are carried. More than one block name mines all of them,
nearest first. The count goes last.

The picker of every block in the game opens once you start a block name, and stays
closed once you type the count:

```
$mine oak_
$mine oak_log 30
```

Block names are the game's own, with or without the `minecraft:` prefix. A single
state can be picked out with square brackets, and more than one at a time separated
by commas:

```
$mine oak_log[axis=x]
$mine oak_log[axis=x,waterlogged=false]
```

`Tab` completes the highlighted block, and an unreadable name is reported without
changing anything. Inside the brackets the picker completes property names and then
their values:

```
$mine oak_[Tab] [Space] oak_[Tab]
$mine oak_log[axis=[Tab]
```

Every block it has found is outlined while it works, and so is the block it is
breaking. Turn those off with:

```
$set mining.renderTargets false
$set outline.blocksToBreak false
$set outline.enabled false
```

Overhead breaking is what takes a tree down. Turn it off with:

```
$set mining.breakOverhead false
```

`$mine` drives HELM for as long as it lasts. `$goto`, `$autogoto`, `$autolookat`,
`$farm` and starting a macro each end the mine first.

```
$set mining.lowestLevel -59
$set mining.highestLevel 64
$set mining.maxTargets 32
$set mining.sightOnly true
$set mining.onlyExposed true
$set mining.exploreWhenUnknown false
```

Levels are absolute, and `mining.lowestLevel 0` means the bottom of whatever
dimension you are in.

See [SETTINGS.md](SETTINGS.md) for every setting and
[FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) for how it behaves.

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

## Following

`$follow` walks after mobs and players, and keeps going until `$stop` or until
nothing matches for long enough.

Type `$follow` and the picker opens straight away. The left column is every mob
in the game with its kind beside it, and the right column is the players in the
world. Typing narrows both at once, `←` and `→` move between the columns, `↑` and
`↓` move within one, and clicking or pressing `Tab` writes the highlighted name
in. Press `Space` and pick again to add another:

```
$follow [Tab]
$follow zom[Tab] [Space] ske[Tab] [Space] Notch[Tab]
```

There is no keyword and no mode to choose. A name is either a mob or a player
and HELM works out which.

```
$follow zombie
$follow zombie skeleton
$follow Notch
$follow Notch Steve zombie
```

Mob names are the game's own, with or without the `minecraft:` prefix. Only mobs
are listed: anything the game itself files as not being a mob, such as a boat, an
armour stand, an arrow or a dropped item, is not a thing you can walk after, so it
is not offered. Writing one by hand is refused by name. Player names have to be
someone who is in the world right now, and are matched without regard to
capitalisation.

Every name you write is followed at once.

While following, HELM works towards the closest match. As the target moves, the
spot it wants to stand on moves with it, and the walk is dropped and searched
again whenever the spot it was heading for stops counting.

How close HELM tries to stand is `follow.radius` across and
`follow.verticalRadius` up or down. `follow.offsetDistance` with
`follow.offsetDirection` and `follow.verticalOffset` move the spot away from the
target. Holding a fixed gap of exactly five blocks:

```
$set follow.radius 0
$set follow.verticalRadius 0
$set follow.offsetDistance 5
$set follow.offsetDirection 180
$set follow.maxTargetDistance 64
$follow Notch
```

`follow.maxTargetDistance` and `follow.minTargetDistance` stop anything outside
that range from being considered at all. `follow.waitTicks` is how long HELM
keeps going when nothing matches, which covers a target briefly out of sight.
When it runs out HELM says `Nothing left to follow.` and stops by itself.

In a crowd, `follow.keepTarget` stops the follow switching between every mob of
the same kind, and `follow.closestOnly` goes the other way and works towards the
single nearest:

```
$set follow.keepTarget true
$set follow.closestOnly true
$set follow.ignoreSameKind true
```

What HELM may do on the way is `follow.sprint`, `follow.breakBlocks` and
`follow.placeBlocks`. Turning the last two off means it never modifies the world
to reach the target:

```
$set follow.breakBlocks false
$set follow.placeBlocks false
$set follow.sprint false
```

Following never turns your camera by default. Turn it on to watch the target
while HELM is standing still, and `follow.maxLookPitch` stops the camera tilting
too far when the target is above or below:

```
$set follow.lookAtTarget true
$set follow.maxLookPitch 60
```

`$follow` drives HELM for as long as it lasts. `$goto`, `$autogoto`,
`$autolookat`, `$farm`, `$mine` and starting a macro each end the follow first,
and `$stop` ends it on its own.

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
