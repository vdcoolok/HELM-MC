# Usage

Type `$` in the chat box, then a command. Anything starting with `$` is handled
by HELM. Anything else is normal chat.

## Commands

| Command | What it does |
| --- | --- |
| `$help` | List every command |
| `$version` | Show the HELM version |
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

Short forms: `macro` is also `macros`, `action` is also `a`, `remove` is also
`rm` or `delete`, and `exitEditMode` is also `exit`, `stopEdit` or `exitEdit`.
Every form of `exitEditMode` works with or without `macro` in front.

## Make a macro

```
$macro create farm
```

## Open it

```
$macro edit farm
```

## Add lines

While a macro is open, type its syntax straight into chat and press enter:

```
$wait 1s
$loop
$endloop
```

Or name each line:

```
$macro action add wait 1s
```

## Check what is in it

```
$macro action list
```

```
[HELM] 3 line(s)
[HELM]   1  wait 1s
[HELM]   2  loop
[HELM]   3  endloop
```

## Change a line

```
$macro action move 3 1
$macro action remove 2
```

While a macro is open you can type the short form of any of these and press
enter, which does the same thing:

```
$move 3 1
$remove 2
$list
$exit
```

`$list` here shows the open macro's lines. To list your macros instead, use
`$macro list`. The short `$list` only works while a macro is open.

## Close it

```
$exitEditMode
```

## Run it

```
$macro load farm
```

## Stop it

```
$macro stop
```

## See what you have

```
$macro list
```

## In the editing popup

While a macro is open, typing in chat opens a popup of everything you can type
there: macro syntax on the left, macro tools on the right.

| Key | Does |
| --- | --- |
| Type | Narrows the list |
| Space | Closes the popup and shows what goes after it |
| Mouse wheel | Scrolls a long list |
| Up / Down | Move within a column |
| Left / Right | Move between columns |
| Tab | Fill in the selected row |
| Click | Fill in that row |
| Enter | Ignore the popup and send what you typed |
| Escape | Close the chat box, as usual |

Once you type a space the popup closes and the chat bar shows what that command
wants next, in grey:

```
$move      ->  popup, pick move
$move      ->  <from> <to>          once you add the space
$wait      ->  <duration>
$goto      ->  <x> <y> <z>
$lookat    ->  <pitch> / <yaw>
$loop      ->  [count]
```

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

## Where your macros live

```
<game directory>/HELM/macros/
```

That is `.minecraft/HELM/macros` on Linux and macOS, and
`%APPDATA%\.minecraft\HELM\macros` on Windows. The folder is made for you the
first time you join a world. You can put macro files in it yourself.

## If something goes wrong

| Message | What to do |
| --- | --- |
| `Unknown command: fly` | That command does not exist, or a macro is open and it was read as syntax |
| `No macro named farm` | Check `$macro list` for the exact name |
| `A macro named farm already exists` | Pick another name, or open the existing one |
| `There is no line 9 (the macro has 5)` | The macro has fewer lines than that |
| `Unknown command 'wut'` | Not valid syntax; run `$macro action add` with no line to see the list |
| `'loop' is missing 'endloop'` | Close the loop |
| `No macro is open for editing` | `$exitEditMode` was used with nothing open |

## What runs today

`wait` and `loop` work. The rest are accepted and checked when a macro loads,
but report that they are not available yet if a macro reaches them.

See [FULLYEXPLAINEDFEATURES.md](FULLYEXPLAINEDFEATURES.md) for how any of this
behaves, and [FEATURES.md](FEATURES.md) for the list.