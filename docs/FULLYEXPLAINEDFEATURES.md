# Features in detail

## Command system

### What it is

HELM exposes every feature it has through a single command line form:

| Entry point | Typed in chat | Example |
| --- | --- | --- |
| Command prefix | `$name args` | `$version` |

A line beginning with `$` is handled entirely on the client. It is intercepted
before the message leaves the client, so a command line is never transmitted to
the server and is never seen by other players. A line that does not begin with
`$` is ordinary chat and is sent normally.

`$` is a single character, it does not collide with server chat commands, and it
leaves every other client and server command untouched.

### How it behaves

There is one command tree. Names, arguments, validation, completion, error
messages and help are all resolved through it, so the command list, the help
output and the suggestion popup cannot drift away from what actually runs.

For any given command:

- the same arguments are accepted
- the same arguments are validated and typed
- the same error messages are produced
- the same output is produced
- the same side effects occur
- the same aliases work
- the same help text is shown

### Arguments

Arguments are declared with a type. Supported types are:

| Type | Accepted input |
| --- | --- |
| string | any text; an optional trailing argument consumes the rest of the line |
| integer | a whole number |
| number | a decimal number |
| boolean | `true` or `false` |

Required arguments must be present. An optional argument may be omitted. Giving
an argument a value of the wrong type reports an error naming the argument and
the offending value.

Text may be quoted to include spaces, and a backslash escapes the next
character:

```
$say "hello world"
```

### Aliases

A command may declare aliases. An alias behaves exactly like the name it
aliases:

```
$version
$ver
```

### Command suggestions

Typing `$` in the chat box opens the command suggestion list, the same way
typing `/` opens the client command list. The list is produced from the same
declarations as the commands themselves, so it cannot list a command that does
not exist or omit one that does.

The list narrows as you type. Arrow keys move through it, and tab or enter fills
in the highlighted command. Suggestions stop at a completed command name, so a
command with no arguments does not offer anything after its name.

### The command list

The command list shows one line per command:

```
[HELM] Commands  $name
  help - Lists every available command.
  version - Shows the loaded HELM version.
```

Lines are kept short so they do not wrap. Full syntax, aliases and argument
details appear when a line is hovered. Clicking a line fills that command into
the chat box without sending anything to the server.

### Adding a command

A command is declared in one place. Registering it makes it available through
`$name`, in the command list and in the help output. No separate registration,
listing or help entry is required; the rest is generated.

### Failure behaviour

| Situation | Result |
| --- | --- |
| Unknown command name | error naming the command |
| Missing required argument | error naming the argument |
| Argument of the wrong type | error naming the argument and the value |
| Unexpected trailing text | the command runs with the arguments it accepts |
| Executor throws | the failure is reported and the command returns failure |

### Cancellation

Individual commands are instantaneous and hold no state, so there is nothing to
cancel. A command that starts long running work will cancel that work when it
finishes.

### Limitations

- The `$` prefix is not configurable.
- The `$` prefix is a client side feature. Other players and the server do not
  see command lines.

## help

Lists every available command with its syntax, aliases and arguments. Available
as `$help`, with the aliases `h` and `?`. A bare `$` also shows this list.

## version

Reports the loaded HELM version. Available as `$version`, with the alias `ver`.

## goto

Walks to a block position. Available as `$goto <x> <y> <z>`, with the aliases
`g`, `go` and `to`.

The reply is one of `Path found: N steps.`, `Partial path: N steps so far.` or
`No path to x y z.` The first means the goal was reached, the second means the
walk will go as far as it can and then stop, and the third is a failure where
nothing is drawn.

Arguments are whole numbers. A non number is an error and nothing is calculated.

This only works in a world. Outside one it reports that and does nothing.

[PATHFINDING.md](PATHFINDING.md) covers how a route is chosen, how it is walked,
and what happens when it fails.

## stop

Ends a walk at once and releases every held key. Available as `$stop`, with the
aliases `cancel`, `abort` and `halt`. It reports `Nothing to stop.` when there
was no walk in progress.

## set

Changes a setting. Also available as `$setting`.

| Form | What it does |
| --- | --- |
| `$set` | Opens a picker listing every setting beside its value |
| `$set <name>` | Prints one setting |
| `$set <name> <value>` | Changes one setting |

The picker opens as soon as `$set` is typed with a space after it, so it is never
something that has to be remembered to reach. Each setting is one row showing
its name and current value, arranged so each section is its own column, which
means the sections read side by side rather than one after another. Arrow keys
move within the list and between columns, tab or a click puts the setting under
the cursor into the chat box, and typing filters the list as you go.

Once a setting's name is complete the list becomes the values that setting
accepts. A flag offers `true` and `false`, with the opposite of the current
value first, so it can be flipped with two presses. Anything else offers its
current value, so it can be typed over rather than guessed at.

`$settings` remains only to reset everything, as `$settings reset`, which is also
`$settings defaults`.

A value outside a setting's allowed range is clamped rather than rejected. A
value of the wrong kind is an error and nothing changes; a word where a number
belongs is an error rather than being read as zero, and a word where a flag
belongs is an error rather than quietly turning it off. Changing a setting takes
effect immediately, and re-prices a walk that is already in progress.

Settings are stored in `settings.conf` inside the game directory, as plain text,
written as UTF-8 and replaced atomically. A missing or unreadable file falls back
to the defaults without losing anything.

[SETTINGS.md](SETTINGS.md) lists every setting with its default and what it does.

## exitEditMode

### What it is

Closes the open macro without running it. Available as `$exitEditMode`, with the
aliases `exit`, `stopEdit` and `exitEdit`. Every alias also works with `macro` in
front, and a bare `$macro edit` closes the open macro too.

### Visibility

The command exists only while a macro is open. With nothing open it is absent
from the command list, absent from the suggestions, and typing it reports an
unknown command rather than doing nothing. The message that opens a macro states
how to close it.

### Settings

None.

### Known limitations

None.

## macro

### What it is

`macro` starts, stops and lists macros. It is available as `$macro`, with the
alias `macros`.

### Commands

| Action | Effect |
| --- | --- |
| `create <name>` | create an empty macro |
| `edit <name>` | open a macro for editing |
| `edit` | close the open macro |
| `action add <syntax>` | append a line, or list every available command |
| `action remove <line>` | remove a numbered line |
| `action list` | show the open macro's lines as a numbered list |
| `action move <from> <to>` | move a line, shifting the rest |
| `load <name>` | parse and start the named macro |
| `stop` | stop the running macro |
| `list` | list the macros in the macro folder |

### Where macros come from

Macros are plain UTF-8 text files in the `macros` folder inside HELM's data
folder in the Minecraft game directory. The folder is created on demand. The
full syntax is documented in MACROSYNTAX.md.

### Editing

A macro can be edited from chat without touching files. Opening a macro with
`macro edit <name>` makes subsequent lines of macro syntax append to it, and
`macro action` manages the lines of the open macro by number.

While a macro is open, typing in chat opens a popup with the macro language on
the left and the line utilities on the right.

| Column | Contents |
| --- | --- |
| Left | every macro language command, with its syntax |
| Right | `exitEditMode` and the `macro action` line operations |

It is drawn in the same style as the game's own command popup, and takes over
from it rather than drawing behind it. That means a translucent dark background
with an outline, rows 12 pixels tall, grey text, and the row under the cursor
turning yellow. Each row is the name followed by a dash and what it inserts, and
the part after the dash is dimmer than the name so the two read as separate
things.

A row's description is the arguments only, never the full syntax, so a name is
never printed twice in a row.

There are two popup modes. `NAMES` is the two column list of everything a line
typed there could become. `INPUTS` is a list of every key and mouse button, shown
when the line has reached an argument that wants an input, which is what `hold`,
`release` and `press` take.

The input list is generated from the same table the parser uses to resolve an
input, so everything offered is guaranteed to be accepted. Nothing can appear in
the list and then be rejected. It carries a description for each entry, and splits
into keyboard on the left and mouse and scroll on the right.

That list is 82 entries, or 77 lines once two columns are used, which is taller
than most screens. The popup clamps itself to the space above the input box and
scrolls the rest with the mouse wheel, with a dotted mark on the edge that has
more. Moving the selection with the arrow keys scrolls it into view. A list that
fits entirely does not scroll.

The popup only exists while a name is being picked. Once a space appears in the
line it closes, because the rest of the line is an argument rather than a choice,
and the chat bar shows the arguments instead as grey ghost text. The arguments
come from the same declarations the rows do, scanned out of each usage string, so
`wait <duration>` yields `<duration>` and `goto <x> <y> <z>` yields all three.
A name that takes no arguments, such as `gotohere` or `exit`, shows no ghost.

The ghost appears only before the first argument is typed. It is not filled in
while an argument is being typed, because the number of arguments already given
cannot always be counted from the tokens: `lookat 14/240` is a single token
holding two values.

Each row shows the text it inserts, so the short name of a utility still types
the full command. Tab fills in the selected row and takes the first match when
nothing is selected. Up and down move inside a column, left and right cross
between them, escape closes the popup, and clicking a row takes it.

While a macro is open, the tool names in the right column can be typed instead of
tabbed. `$list`, `$add wait 1s`, `$remove 2`, `$move 3 1` and `$exit` all run
without the `macro action` in front, and `rm` and `delete` work as well as
`remove`.

There are two different list commands and they are kept apart on purpose.
`macro list` lists the macro files and works at any time. The short `$list` is an
editor tool, so it only exists while a macro is open and it lists that macro's
lines, numbered. `$macro action list` is the same thing written out in full, and
like the rest of `action` it is hidden when nothing is open. This is one list of shortcuts, and the popup and the dispatcher both
read it, so a shortcut cannot be offered without also being runnable.

A shortcut is only expanded once, and only when the line does not already
resolve to a real command. So it can never shadow one, and expanding it can never
expand it again. Outside a macro being open the shortcuts do not exist, so `$list`
is an unknown command rather than a surprising action.

The popup claims only the arrow keys and tab, which have no other meaning while
a chat box is open. Everything else is left to the game.

Enter sends the line exactly as typed rather than accepting the highlighted row,
and escape closes the chat box rather than dismissing the popup. Both were
mistakes: enter being swallowed meant a typed line could never be sent while the
popup was open, and escape being swallowed meant the chat box could not be closed
at all until the popup happened to close first.

Filling in a row replaces only the word under the cursor and never the `$`
prefix, so a filled in line is still a command line.

The popup sizes itself to the screen: it keeps inside the window, drops to one
column when two will not fit, and leaves off a description that will not fit
beside its name rather than cutting it.

Columns are packed. If a filter leaves rows in only one of the two columns, that
column is drawn on its own rather than leaving a gap, and no divider is drawn
between nothing. Navigation moves between the columns that are actually on
screen, so left and right cannot land on one that is not drawn.

The popup appears only when a macro is open, only while the line begins with `$`,
and only while the line does not already say `macro`. Typing `$macro` hands over
to the game's own command popup, so the macro actions remain reachable while
editing.

`edit` and `load` complete the names of macros that already exist, so pressing tab
offers them. `list` shows one macro per line.

The `action` group only exists while a macro is open, so it is absent from the
the action group and from suggestions otherwise. `action add` with no line prints
every available macro command with its syntax and an example.

`action move` lifts a line out and inserts it at the new position, so the lines
between shift along to fill the gap. A position outside the macro is refused
and the file is left untouched.

While a macro is open, a line that does not start with a known macro command is
refused and the available commands are listed, so a typo is never written into
the file. Commands that are real commands, such as `help`, still run.

Because `loop` and `endloop` are separate lines, a macro is usually incomplete
while it is being built. Adding to an incomplete macro still works and reports
that it is not runnable yet.

### How it behaves

A macro is parsed completely before it starts. A syntax error stops the load
and reports the line, so a macro that would misbehave never begins.

Once running, the macro advances one statement per game tick. `wait` suspends
the macro for its duration and resumes afterwards, and `loop` repeats a block
of statements either forever or a given number of times. Loops may be nested.

Starting a macro while another is running replaces the running one.

### Failure behaviour

| Situation | Result |
| --- | --- |
| No macro with that name | `No macro named <name>` |
| Creating a macro whose name is taken | `A macro named <name> already exists` |
| The file cannot be read | error, the macro does not start |
| The macro does not parse | error naming the line, the macro does not start |
| A line number that does not exist | `There is no line <n> (the macro has <m>)` |
| A line that is not valid syntax | the line is refused and the available commands are listed |
| An unclosed loop | `'loop' is missing 'endloop'` |
| The macro reaches an unavailable statement | the macro stops and reports it |
| `stop` with nothing running | error, nothing changes |

Unavailable statements are reported rather than skipped, so a macro never
appears to run while quietly doing nothing. A refused line is never written, so a
mistake cannot corrupt a macro.

While a macro is open, a line that is not a command is read as macro syntax rather
than as an unknown command, because that is what typing there means. The error
lists the syntaxes available, so a mistyped `$exit` says what is valid instead of
leaving it to be guessed.

### Cancellation

A macro can be stopped with `$macro stop`. Leaving the world also ends it.
There is no way to interrupt a macro from inside its own text at present.

### Settings

None. The prefix is fixed and the macro folder is fixed.

### Known limitations

`wait` and `loop` run. `goto`, `gotohere`, `lookat`, `lookathere`, `hold`,
`release` and `press` are recognised and validated when a macro loads, but a
macro that reaches one of them stops and reports that it is not available yet. Editing is per session, so closing the
game closes the open macro, though everything written is already saved to disk.
There is no way to insert a line at a chosen position without moving it there
first.

## User data

HELM stores user data inside the Minecraft game directory in a top level `HELM`
folder, on both Windows and Linux. The location is resolved through the game at
runtime rather than hardcoded. The folder and its `macros` subfolder are created
the first time a world is joined, with no command needed. Macros live in the
`macros` folder, settings live in `settings.conf` next to it, and the session log
is written to `debuglogs.log`.

| Path | What it holds |
| --- | --- |
| `HELM/macros/` | Macro files. You can add your own |
| `HELM/settings.conf` | Every setting, one per line, as `name = value` |
| `HELM/debuglogs.log` | What HELM did this session, one line per event |
| `HELM/cache/` | Remembered chunks, one folder per dimension |

All three are plain UTF-8 text. No previous storage location is read or migrated
from; HELM has only ever used this folder.

### The chunk cache

`cache/` is the only folder here that is not text. It holds a compressed record
of the chunks HELM has seen, so that path finding can work out a route across
terrain the game is not currently holding. It is written on a background thread
and is never needed for correctness, only for reach.

| Path | What it holds |
| --- | --- |
| `HELM/cache/<namespace>/<dimension>_<height>/` | One folder per dimension per world height |
| `.../r.<x>.<z>.rcache` | One compressed file per 512 by 512 block region |

It is safe to delete while the game is closed; you lose remembered terrain and
nothing else. `cache.expirySeconds` set to a positive value is the way to have it
shrink on its own.

See [PATHFINDING.md](PATHFINDING.md) for what is remembered, when it is read, and
every setting that affects it.
### The session log

`debuglogs.log` is written from the moment the game starts, without any command
or setting to turn on, and is deleted and recreated on the next start. Each line
carries a wall clock time, the game tick it happened on, and the area it came
from, so a sequence of events can be read back in order.

It records:

- startup, including the settings that were loaded and the world that was joined
- every `$goto`: the requested position, the player's position and rotation, the
  relevant movement settings, how long the search took, how many nodes it looked
  at, and why it stopped
- the shape of the resulting route, its first steps and its last steps
- each step of a walk as it is attempted, with the step kind, the cost the step
  was planned at and the cost it actually costs now
- why a walk was abandoned, in words: a step that stopped being possible, a cost
  that rose too far, too long on one step, or straying from the route
- a halt, skip or rewind when the player's actual position no longer matches the
  step the walk expects

### How it is kept small

The log is written so a whole session stays readable rather than becoming
megabytes. Three kinds of entry are used. Events happen once and are always
written. Repeats are only written when the value they describe actually changes,
and when it has not, a note saying how many times in a row it has been
unchanged. Pulses are written at most once every two seconds per key. A summary
line is written after 25, 100 and 1000 identical repeats so nothing goes
unreported entirely.

If the file reaches half a megabyte, the oldest four thousand lines are dropped.

### Failure

Nothing in the logging can take the game down. Every write is guarded, and the
first failure switches logging off for the session and leaves everything else
running. If the game directory is not writable, the file simply is not created
and HELM reports it once rather than every time a world is joined.

The log contains no other mod's output and no account or server details beyond
the dimension you are in. It is not sent anywhere; it is only useful if you
choose to attach it.

If the directory cannot be created, for example because the game directory is
read only, HELM reports it once in chat rather than every time a world is
joined, and the features that need it remain unavailable. If only the settings
file cannot be written, HELM says so once and the change still applies for that
session. If only the log cannot be written, HELM stays silent and everything else
works.
