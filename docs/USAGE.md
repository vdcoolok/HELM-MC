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
| `Walking stopped. Look released.` | `$stop` caught a walk and a held facing |
| `Walking stopped. Macro stopped.` | `$stop` caught a walk and a running macro |
| `Look released.` | `$stop` caught a held facing |
| `Macro stopped.` | `$stop` caught a running macro |
| `Stopped.` | `$stop` caught a walk |
| `Stopped searching.` | `$stop` caught an unfinished search |
| `Nothing to stop.` | Nothing was walking, held or running |
| `Unknown command: name` | No such command |
| `Unknown setting: name` | No such setting |
| `No macro named name` | No such macro |
| `'loop' is missing 'endloop'` | The open macro has an unclosed loop |

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
$settings reset
```

A flag value is one of `true`, `false`, `yes`, `no`, `on`, `off`, `1`, `0`.

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
