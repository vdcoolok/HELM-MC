# Usage

Type `$` in the chat box, then a command. Anything starting with `$` is handled
by HELM. Anything else is normal chat.

## Commands

| Command | What it does |
| --- | --- |
| `$help` | List every command |
| `$version` | Show the HELM version |
| `$goto <x> <y> <z>` | Walk to a block position |
| `$stop` | Stop walking |
| `$set` | Open the settings picker |
| `$set <name>` | Show one setting |
| `$set <name> <value>` | Change one setting |
| `$settings reset` | Restore every setting to its default |
| `$macro create <name>` | Make a new empty macro |
| `$macro edit <name>` | Open a macro for editing |
| `$macro edit` | Close the open macro |
| `$exitEditMode` | Close the open macro |
| `$macro action add <syntax>` | Add a line |
| `$macro action remove <line>` | Delete a numbered line |
| `$macro action move <from> <to>` | Move a line |
| `$macro action list` | Show the open macro's lines, numbered |
| `$macro load <name>` | Start a macro |
| `$macro stop` | Stop the running macro |
| `$macro list` | List your macros |

`goto` is also `g`, `go` or `to`. `stop` is also `cancel`, `abort` or `halt`.
`set` is also `setting`. `macro` is also `macros`. `action` is also `a`.
`remove` is also `rm` or `delete`. `exitEditMode` is also `exit`.

## Walk somewhere

```
$goto 120 64 -35
$stop
```

Coordinates are block positions. You first get `Searching for a way to x y z.`,
then one of `Path found: N steps.`, `Partial path: N steps.`, `Already at x y z.`
or `No path to x y z.` once the search finishes. The search runs off the thread
that draws the game, so the game stays responsive while it works and the answer
arrives a moment later rather than straight away.

The path is drawn in the world. Blocks to mine are outlined in red, blocks to
place in blue.

Giving a new goal while a search is still running cancels the running one.
`$stop` cancels a search too, and answers `Stopped searching.` if that is what it
caught, otherwise `Stopped.` or `Nothing to stop.`

## Change a setting

```
$set
$set movement.allowBreak
$set movement.allowBreak false
$settings reset
```

Type `$set` and a space and the picker opens: every setting in one column, with
the space beside it explaining whatever you are pointing at. Arrows move, tab or
a click chooses, typing filters, the mouse wheel scrolls. A flag then offers
`true` and `false`.

Typing filters on any part of a setting's name, not just the front, so `parkour`
finds `movement.allowParkour` even though the word is in the middle of the name.
An exact name comes first, then names starting with what you typed, then names
containing it at a word boundary, then the rest. Searching by what a setting is
called also works, so `sprint` finds `movement.sprintAllowed`.

You can also just type it. A flag accepts `true`, `false`, `yes`, `no`, `on`,
`off`, `1` or `0`.

[SETTINGS.md](SETTINGS.md) lists every setting.

## Make a macro

```
$macro create farm
$macro edit farm
$macro load farm
$macro stop
$macro list
```

While a macro is open, any `$` line that is not a real command is added to it.
A popup lists the syntaxes you can add on the left and the editing tools on the
right. `hold`, `release` and `press` show a list of every key and button.
Arrows move, tab or a click fills in, typing narrows the list.

```
$macro action add gotohere
$macro action list
```

```
1 line(s)
  1  goto -2 88 -117
```

`gotohere` records the block you are standing in and `lookathere` records the
angle you are facing. Both write the numbers into the line as an ordinary `goto`
or `lookat`, so the macro returns to that spot or angle however far you move
afterwards.

[MACROSYNTAX.md](MACROSYNTAX.md) has the full syntax.

## Where your data lives

```
<game directory>/HELM/
```

`.minecraft/HELM` on Linux and macOS, `%APPDATA%\.minecraft\HELM` on Windows.

| Path | What it holds |
| --- | --- |
| `HELM/macros/` | Your macro files |
| `HELM/settings.conf` | Every setting, as `name = value` |
| `HELM/debuglogs.log` | A record of what HELM did this session |
| `HELM/cache/` | Remembered chunks, one folder per dimension |

All plain text and all safe to read, edit or delete while the game is closed.

## If something goes wrong

| Message | What to do |
| --- | --- |
| `Unknown command: fly` | No such command, or a macro is open and read it as syntax |
| `No path to 10 64 10` | Nothing walkable connects you to that block |
| `Nothing to stop.` | You were neither walking nor searching |
| `Stopped searching.` | `$stop` caught a search that had not finished yet |
| `Unknown setting: movement.fast` | Check [SETTINGS.md](SETTINGS.md) for the exact name |
| `No macro named farm` | Check `$macro list` |
| `'loop' is missing 'endloop'` | Close the loop |

Attach `HELM/debuglogs.log` to a bug report.

## What runs today

Everything runs. Walking, mining, placing, aiming, rendering, and every macro
statement.

[FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) has the detail.