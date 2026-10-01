# Macro syntax

A macro is a short text file describing a sequence of actions HELM performs for
you. Macros are written by hand in a deliberately simple, readable syntax.

## Where macros are stored

Macros live in a `macros` folder inside HELM's data folder, which sits in the
Minecraft game directory:

```text
<game directory>/HELM/macros/
```

The game directory is `.minecraft` on Linux and macOS and `%APPDATA%\.minecraft`
on Windows. The folder is created automatically the first time you list or load
a macro.

A macro named `test` is the file `test.macro` in that folder. The folder is
scanned when a macro is loaded, so adding a file takes effect immediately with
no restart.

Macro names may contain letters, digits, underscores and hyphens. The
`.macro` extension is optional, so `macro create test` and `macro create
test.macro` refer to the same macro.

`macro edit` and `macro load` complete the names of the macros you already
have, so pressing tab offers them instead of a bare placeholder.

## File format

A macro is a plain UTF-8 text file with one command per line.

- Whitespace around a line is ignored.
- Blank lines are ignored.
- Everything from a `#` to the end of a line is a comment.
- Command names are not case sensitive, so `WAIT` and `wait` are the same.
- Arguments are separated by spaces or tabs.

## Commands

### wait

Pauses the macro for a period of time.

```text
wait <duration>
```

| Duration | Meaning |
| --- | --- |
| `500ms` | milliseconds |
| `10s` | seconds |
| `2m` | minutes |
| `20t` | ticks, 20 to a second |
| `20` | ticks, when no unit is given |

Decimal values are accepted, so `2.5s` is valid and waits two and a half
seconds.

A macro that is nothing but `wait 10s` finishes after 10 seconds of game time.

### goto

Walks to a position.

```text
goto <x> <y> <z>
```

All three coordinates are required and may be negative.

### gotohere

Shorthand for walking to whatever the crosshair is on, without working out its
coordinates.

```text
gotohere
```

Aim at the target, type this, and the coordinates are written into the line:

```text
1 line(s)
  1  goto -2 88 -117
```

The line is now an ordinary `goto`. The target is fixed at the moment you add
it, so the macro keeps going to that spot however far you walk away afterwards.
Aim somewhere else and add another line to capture a different spot.

Aim at nothing and nothing is added, rather than a line that walks nowhere.

`gotohere` is only shorthand in the editor. A macro file never holds it, so a
file containing one is rejected by name when it loads.

### lookat

Turns the player to a given angle.

```text
lookat <pitch> / <yaw>
```

Pitch is the vertical angle and is limited to the range -90 to 90. Yaw is the
horizontal angle in degrees.

Pitch comes first, then a slash, then yaw. Spaces around the slash are
optional, so these two lines are identical:

```text
lookat 14 / 240
lookat 14/240
```

Yaw is wrapped into the range -180 to 180, so a yaw of 240 is treated as -120.

### lookathere

Shorthand for turning to an angle without working it out yourself.

```text
lookathere
```

Your current pitch and yaw are written into the line:

```text
1 line(s)
  1  lookat 12.4/-88.1
```

The line is now an ordinary `lookat` and the angle is fixed at the moment you
add it, exactly as `gotohere` fixes a position.

### hold

Presses and keeps an input held down.

```text
hold <input>
```

### release

Releases an input that was being held.

```text
release <input>
```

### press

Taps an input once, pressing and releasing it in a single step. This is the
short form of `hold` followed by `release`.

```text
press <input>
press M1
```

Use `press` for a single click, and `hold` with `release` when you need the
input to stay down.

### loop

Repeats a block of commands.

```text
loop
    ...commands...
endloop

loop <count>
    ...commands...
endloop
```

With no count the loop repeats forever until the macro is stopped. With a
count the loop repeats exactly that many times and then continues after the
`endloop`.

Loops can be nested.

Every `loop` must have a matching `endloop`, and an `endloop` without a `loop`
is an error.

## Inputs

`hold` and `release` accept any of the following names, in any case.

| Name | Input |
| --- | --- |
| `a` to `z` | letter keys |
| `0` to `9` | number row keys |
| `f1` to `f25` | function keys |
| `space` | space |
| `shift` | left shift |
| `ctrl` | left control |
| `alt` | left alt |
| `enter` or `return` | enter |
| `tab` | tab |
| `escape` | escape |
| `m1` or `left` | left mouse button |
| `m2` or `right` | right mouse button |
| `m3` or `middle` | middle mouse button |
| `scrollup` or `wheelup` | scroll wheel up |
| `scrolldown` or `wheeldown` | scroll wheel down |

## Errors

A macro is fully checked before it runs. If anything is wrong, the macro does
not start and the problem is reported with its line number:

```text
[HELM] 'loop' is missing 'endloop'
[HELM] Unknown input: M9 on line 1
[HELM] Invalid duration: 10seconds
```

Common mistakes:

| Message | Cause |
| --- | --- |
| `'endloop' without 'loop'` | an `endloop` with no opening `loop` |
| `'loop' is missing 'endloop'` | a `loop` was never closed |
| `Unknown command` | a misspelled command name |
| `Expected 3 arguments` | `goto` needs exactly three coordinates |
| `Invalid pitch` or `Invalid yaw` | a non numeric angle in `lookat` |

## Managing macros

Every command below is typed in chat as `$macro ...`.

Actions can also be abbreviated: `macro` is also `macros`, `action add` is also
`action a`, and `action remove` is also `action rm` or `action delete`.

### create

Creates a new, empty macro.

```text
macro create <name>
macro create test
```

If a macro with that name already exists, nothing is changed and an error is
reported.

### edit

Opens a macro for editing.

```text
macro edit <name>
macro edit test
```

While a macro is open you can type macro syntax directly in chat and it is
appended to that macro:

```text
$macro edit test
$wait 1s
$goto -50 -50 -50
$loop
$endloop
```

Only lines that start with a known command are accepted, so a typo is reported
instead of being written into the file. Anything that is a real command, such as
`$help`, still runs normally while editing.

Close the macro with `exitEditMode`, on its own:

```text
exitEditMode
```

That is the shortest form, so it is the one offered at the end of the syntax
list while editing. It is also available as `exit`, `stopEdit` and `exitEdit`,
with or without the `macro` in front:

```text
exit
exitEditMode
stopEdit
exitEdit
macro exitEditMode
macro exitEdit
macro stopEdit
macro edit
```

`exitEditMode` only exists while a macro is open. With nothing open it does not
appear in the command list and it is not accepted, so a stale one cannot silently
do nothing. `macro edit` with no name also closes the open macro.

The message that opens a macro states how to close it, so the answer is on
screen when you need it.

The `action` group only appears while a macro is open. With nothing open, it is
missing from the actions of `macro` and from the suggestions.

Editing is per session. Closing the game closes the editor, and the file on disk
is already saved.

### The editing popup

While a macro is open, typing in chat opens a popup with two columns: the macro
language on the left, the line utilities on the right.

```
 wait        - <duration>       │ exitEditMode  - close this macro
 goto        - <x> <y> <z>      │ list          - show the lines
 gotohere                       │ add           - macro action add
 lookat      - <pitch> / <yaw>  │ remove        - macro action remove
 lookathere                     │ move          - macro action move
 hold        - <input>
 release     - <input>
 press       - <input>
 loop        - [count]
 endloop
```

Each row shows what it inserts on the right, so `list` types `macro list` and you
do not have to remember it.

It looks like the game's own command popup: a translucent dark background with an
outline, grey text, and the row under the cursor turning yellow. The grey part
after the dash is dimmer than the name, so the two are easy to tell apart.

It narrows as you type, and it matches on the name you would type:

```
$w  ->  wait
$g  ->  goto  gotohere
$l  ->  lookat  lookathere  loop  list
$zz ->  nothing, so no popup
```

Tab takes the selected row and fills it in. If nothing is selected yet, the first
match is taken. Up and down move inside a column and wrap at the ends. Left and
right cross between the two columns. Escape closes the popup. Clicking a row
takes it the same way.

Enter is left alone and sends the line exactly as typed, so you never have to
press it twice.

Every name in the right column can be typed instead of tabbed. Typing `$list`,
`$add wait 1s`, `$remove 2`, `$move 3 1` or `$exit` and pressing enter does the
same as the full command, as long as a macro is open.

`$list` shows the open macro's lines. It is not the same as `macro list`, which
lists your macro files and works at any time.

Filling in a row only ever replaces the word under the cursor. The `$` is never
touched, so `$` plus clicking `goto` gives `$goto`, ready to send.

Selecting a utility fills in the whole command, so `list` becomes `macro list` and
`add` becomes `macro action add ` with the cursor ready for the syntax to type.

The popup keeps itself inside the screen. If two columns would not fit across it
drops to one, and if a description would not fit beside its name it is left off
rather than cut in half. A column with nothing in it is not drawn at all, so
filtering down to a single result gives a single column with no empty space and
no divider.

Type a space and the popup closes, leaving the arguments in grey in the chat bar:

```
$move      ->  <from> <to>
$remove    ->  <line>
$add       ->  <syntax>
$wait      ->  <duration>
$goto      ->  <x> <y> <z>
$lookat    ->  <pitch> / <yaw>
$loop      ->  [count]
```

`hold`, `release` and `press` take an input, so they open a list instead of a
grey hint:

```
$hold   ->  SPACE  - space bar          M1  - left mouse button
           A      - letter a            M2  - right mouse button
           SHIFT  - left shift          M3  - middle mouse button
           ESCAPE - esc                 SCROLLUP / SCROLLDOWN
```

Every letter, every number, F1 to F25, space, shift, ctrl, alt, enter, tab and
esc are listed too, and the list narrows as you type. It is long, so it scrolls
with the mouse wheel.

The grey text disappears once you start typing the argument. It is not filled in
while an argument is being typed, because the arguments already given cannot
always be counted from the words: `lookat 14/240` is one word holding two values.

The popup appears only when a macro is open, only while the line begins with
`$`, and only while the line does not already say `macro`. Typing `$macro` hands
over to the game's command popup, so `macro list` and the rest keep working while
editing.

### action add

Appends a line to the open macro. This is the explicit form of typing syntax
directly.

```text
macro action add <syntax>
macro action add wait 1s
macro action add lookat 14/240
```

Run it with no line to see every available command with its syntax and an
example:

```text
macro action add
```

```text
[HELM] macro syntax
[HELM]   wait <duration>         pause for a time, e.g. wait 1s
[HELM]   goto <x> <y> <z>        walk to a position, e.g. goto -50 -50 -50
[HELM]   gotohere                walk to where you are aiming, e.g. gotohere
[HELM]   lookat <pitch> / <yaw>  turn to an angle, e.g. lookat 14/240
[HELM]   lookathere              turn to where you are aiming, e.g. lookathere
[HELM]   hold <input>            press and keep an input down, e.g. hold M1
[HELM]   release <input>         let an input go, e.g. release M1
[HELM]   press <input>           tap an input once, e.g. press M1
[HELM]   loop [count]            repeat a block until endloop, e.g. loop 3
[HELM]   endloop                 close a loop, e.g. endloop
[HELM] add one with: macro action add <line>
```

Any of those can be added, including `loop` and `endloop`. A command that does
not exist reports the available ones.

Because `loop` and `endloop` are separate lines, a macro is often incomplete
while you are still building it. Adding a line to an incomplete macro still
works, and tells you it is not runnable yet rather than refusing.

### action remove

Removes a numbered line.

```text
macro action remove <line>
macro action remove 3
```

### action list

Shows the open macro as a numbered list. Only available while a macro is open.

```text
macro action list
```

```text
[HELM] 3 line(s)
[HELM]   1  wait 1s
[HELM]   2  loop
[HELM]   3  endloop
```

Every line is shown exactly as it is stored, and a stored line always carries
its own numbers, so a shorthand never appears here:

```text
[HELM] 2 line(s)
[HELM]   1  goto -2 88 -117
[HELM]   2  lookat 12.4/-88.1
```

### action move

Moves a line to a new position. The line is lifted out and put back at the new
position, and every line between shifts along to fill the gap.

```text
macro action move <from> <to>
macro action move 5 2
```

Moving line 5 to line 2 in a five line macro:

```text
before:  1 wait 1s   2 goto 1 2 3   3 lookat 0/0   4 hold M1   5 release M1
after:   1 wait 1s   2 release M1   3 goto 1 2 3    4 lookat 0/0  5 hold M1
```

A position outside the macro is refused and the macro is left untouched.

### load

Starts a macro by name.

```text
macro load <name>
macro load test
```

Starting a macro while another is running replaces it.

### stop

Stops the running macro.

```text
macro stop
```

### list

Lists the macros available in the macro folder.

```text
macro list
```

```text
[HELM] 3 macro(s) in macros/
[HELM]   dig
[HELM]   test
[HELM]   walk-loop
```

### Discovering the actions

Every action is a tab stop, so the list is discoverable while typing. Typing
`$macro ` followed by a space offers the actions, and `$macro action ` offers
the line operations.

Running `macro` with no action, or with something that is not an action, shows
what it can do:

```text
[HELM] macro
  create - Makes a new empty macro.
  edit - Opens a macro, or closes the open one.
  action - Manages the lines of the open macro.
  load - Starts a macro.
  stop - Stops the running macro.
  list - Lists available macros.
```

`action` only appears while a macro is open.

`macro action` does the same for its own operations:

```text
[HELM] macro action
  add - Adds a line to the open macro.
  remove - Deletes a numbered line.
  list - Shows the lines as a numbered list.
  move - Moves a line, shifting the rest.
```

## A full example

```text
# face a direction, then hold and release the left mouse button
goto -50 -50 -50
wait 10s
lookat 14 / 240
hold M1
wait 200ms
release M1

# sweep the view back and forth
loop
    lookat 15/230
    lookat 14/230
    lookat 16/230
endloop

# aim at something, then walk to it and click it
# these two read your aim now and store the numbers
lookathere
gotohere
press M1
```

## Current availability

Every statement runs. `goto` searches and walks, waiting for the walk to finish
before the next line, and stops the macro with an error if there is no path.
`hold` and `press` drive the game's own key bindings, so anything bound to a
key can be held; a key that does nothing while playing is reported rather than
silently ignored.
