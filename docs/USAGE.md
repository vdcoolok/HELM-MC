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
| `$settings list` | List every setting and its value |
| `$settings get <name>` | Show one setting |
| `$settings set <name> <value>` | Change one setting |
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

Short forms: `goto` is also `g`, `go` or `to`. `stop` is also `cancel`, `abort`
or `halt`. `settings` is also `setting` or `option`. `macro` is also `macros`,
`action` is also `a`, `remove` is also `rm` or `delete`, and `exitEditMode` is
also `exit`, `stopEdit` or `exitEdit`.

## Walk somewhere

```
$goto 120 64 -35
```

You get a line back saying either `Path found: N steps.` or
`Partial path: N steps so far.` The second one means the goal was not reachable,
so it will walk as far as it can and then stop.

The path is drawn in the world as you go. Blocks it will mine are outlined in
red, and blocks it will place are outlined in blue.

To stop at any time:

```
$stop
```

`$stop` says `Nothing to stop.` if you were not walking.

## Where walking happens

The whole walk is calculated before the first step. That means a path that goes
around a wall, over a step, down a drop, across a gap or diagonally is decided up
front, and then followed exactly.

While walking, HELM will:

- mine any block in the way, and pick the best tool for it first
- place a block to bridge a gap, or to step up, when that is cheaper than mining
- pillar up by placing a block under itself and jumping
- turn toward a block before mining or placing it
- jump, sneak and sprint at the right moments

If the world changes and a step becomes impossible, the walk is abandoned rather
than walking into a wall.

## Settings

```
$settings list
$settings get movement.allowBreak
$settings set movement.allowBreak false
$settings reset
```

`$settings get` shows the value and a one line description. `$settings set`
takes the value as one word: `true` or `false`, a whole number, a decimal, or
free text for block lists. Changes are saved straight away.

The names are listed by `$settings list`. The ones you are most likely to want:

| Name | Default | What it does |
| --- | --- | --- |
| `movement.allowBreak` | `true` | Mine blocks in the way |
| `movement.allowPlace` | `true` | Place blocks to bridge or step up |
| `movement.allowParkour` | `true` | Jump across gaps |
| `movement.allowSprint` | `true` | Sprint while walking |
| `movement.assumeStep` | `false` | Never jump while stepping up |
| `movement.movementTimeoutTicks` | `100` | Ticks one step may take before giving up |
| `mining.autoTool` | `true` | Switch to the best tool for each block |
| `mining.preferSilkTouch` | `false` | Prefer a silk touch tool when no slower |
| `mining.itemSaver` | `false` | Stop using a tool that is nearly broken |
| `mining.avoidBreaking` | *(empty)* | Block names treated as air, comma separated |
| `look.freeLook` | `true` | Send aiming to the server, not the camera |
| `look.randomLooking` | `0.01` | Degrees of random aim added each tick |
| `path.renderPath` | `true` | Draw the path |
| `path.renderPathAsLine` | `false` | Draw a plain line instead of a ribbon |
| `path.renderBlocksToBreak` | `true` | Outline blocks to mine |
| `path.renderBlocksToPlace` | `true` | Outline blocks to place |
| `path.lineWidth` | `5.0` | Thickness of the path line |
| `path.primaryTimeoutMillis` | `500` | Search time allowed before moving off the start |
| `path.failureTimeoutMillis` | `2000` | Total search time before giving up |
| `path.maxChunkBorderFetch` | `50` | Moves into unloaded chunks the search may consider |
| `cache.enabled` | `true` | Remember chunks so routes can cross terrain that is no longer loaded |
| `cache.expirySeconds` | `-1` | Forget cached chunks older than this |

Searching further or giving up sooner:

```
$settings set path.failureTimeoutMillis 5000
$settings set path.maxChunkBorderFetch 200
```

Caching a lot of terrain, or letting it go:

```
$settings set cache.expirySeconds 604800
$settings set cache.enabled false
```

`cache.enabled` and `cache.preferLoadedChunks` apply the next time you join a
world. The rest apply straight away. See
[PATHFINDING.md](PATHFINDING.md) for what the cache holds.

## Make a macro

```
$macro create farm
```

## Open it

```
$macro edit farm
```

## Add lines

While a macro is open, any `$` line that is not a real command is added to it as
a macro line. You also get a popup listing the syntaxes you can add, on the left,
and the editing tools on the right.

The grey hint goes away as soon as you start typing the argument yourself.

`hold`, `release` and `press` want an input, so instead of a grey hint you get a
list of every key and button you can use, each with what it is:

```
$hold      ->  SPACE   - space bar          M1         - left mouse button
              A       - letter a            M2         - right mouse button
              SHIFT   - left shift          M3         - middle mouse button
              RSHIFT  - right shift         SCROLLUP   - scroll wheel up
              ESCAPE  - esc                 SCROLLDOWN - scroll wheel down
```

All 26 letters, all 10 numbers, F1 to F25, space, shift, ctrl, alt, enter, tab
and esc are in there too. It narrows as you type, and tab or a click fills in the
one you want. The list is long, so it scrolls with the mouse wheel.

Filling in a row keeps the `$`, so the line is ready to send straight away.

Enter always sends the line as typed. It never fills in the popup, so you never
have to press enter twice. Escape is never taken by the popup either, so it
closes the chat box the way it always does.

The syntax and what each tool inserts are listed in
[MACROSYNTAX.md](MACROSYNTAX.md).

## See every command

Type `$` on its own, or `$help`.

## Where your data lives

```
<game directory>/HELM/
```

That is `.minecraft/HELM` on Linux and macOS, and
`%APPDATA%\.minecraft\HELM` on Windows. The folder is made for you the first time
you join a world.

| Path | What it holds |
| --- | --- |
| `HELM/macros/` | Your macro files. You can add your own |
| `HELM/settings.conf` | Every setting, one per line, as `name = value` |
| `HELM/debuglogs.log` | A record of what HELM did this session |
| `HELM/cache/` | Remembered chunks, one folder per dimension |

`settings.conf` is plain text. You can read it, edit it by hand, and back it up.
If it is missing or unreadable, HELM starts from the defaults.

`debuglogs.log` is plain text, one line per event, rewritten from scratch every
time the game starts. It records the goal you asked for, where the player was,
what the search decided and why it stopped, and what happened to each step of the
walk. It stays under half a megabyte by dropping the oldest lines. It is there so
that a problem can be described accurately rather than guessed at, so attaching
it to a bug report is welcome. Nothing is written to it except HELM's own
events, and it is never sent anywhere.

If the file is missing after a session, the game directory was not writable; the
rest of HELM carries on regardless.

`cache/` is written on a background thread and is safe to delete while the game
is closed. Deleting it costs you remembered terrain, nothing else. Set
`cache.expirySeconds` to a positive number if you would rather it shrank on its
own.

## If something goes wrong

| Message | What to do |
| --- | --- |
| `Unknown command: fly` | That command does not exist, or a macro is open and it was read as syntax |
| `No path to 10 64 10` | Nothing walkable connects you to that block |
| `Nothing to stop.` | You were not walking |
| `Unknown setting: movement.fast` | Check `$settings list` for the exact name |
| `No macro named farm` | Check `$macro list` for the exact name |
| `A macro named farm already exists` | Pick another name, or open the existing one |
| `There is no line 9 (the macro has 5)` | The macro has fewer lines than that |
| `Unknown command 'wut'` | Not valid syntax; run `$macro action add` with no line to see the list |
| `'loop' is missing 'endloop'` | Close the loop |
| `No macro is open for editing` | `$exitEditMode` was used with nothing open |

## What runs today

Walking, mining, placing, aiming and rendering all run. On the macro side,
`wait` and `loop` work. The rest are accepted and checked when a macro loads,
but report that they are not available yet if a macro reaches them.

See [FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) for how any of this
behaves, and [FEATURES.md](FEATURES.md) for the list.
