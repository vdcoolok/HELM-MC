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

Coordinates are block positions. You get back `Path found: N steps.`,
`Partial path: N steps.`, `Already at x y z.` or `No path to x y z.`

The path is drawn in the world. Blocks to mine are outlined in red, blocks to
place in blue.

## Change a setting

```
$set
$set movement.allowBreak
$set movement.allowBreak false
$settings reset
```

Type `$set` and a space and the picker opens: every setting beside its value,
one section per column. Arrows move, tab or a click chooses, typing filters. A
flag then offers `true` and `false`.

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
  1  gotohere -> -10 -60 15
```

`gotohere` and `lookathere` carry no numbers, so the list shows what they
currently point at. Aim elsewhere and list it again to check a new target.

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
| `Nothing to stop.` | You were not walking |
| `Unknown setting: movement.fast` | Check [SETTINGS.md](SETTINGS.md) for the exact name |
| `No macro named farm` | Check `$macro list` |
| `'loop' is missing 'endloop'` | Close the loop |

Attach `HELM/debuglogs.log` to a bug report.

## What runs today

Walking, mining, placing, aiming and rendering run. In macros, `wait` and
`loop` run; the rest are accepted and checked on load but report that they are
not available yet if a macro reaches them.

[FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) has the detail.