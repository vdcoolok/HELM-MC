# Features in detail

## Command system

### What it is

HELM exposes every feature it has through a command line. The same command is
reachable two ways:

| Entry point | Typed in chat | Example |
| --- | --- | --- |
| Short prefix | `$name args` | `$version` |
| Standard form | `/helm name args` | `/helm version` |

The short form is the quick path. It is a single character, it does not collide
with server chat commands, and it is intercepted before the message leaves the
client, so a command line is never transmitted to the server.

The standard form is the familiar Minecraft chat command. It is registered with
the client command system, so it appears in the client's command list and
participates in normal chat command handling.

### How it behaves

Both entry points dispatch to the same implementation. For any given command:

- the same arguments are accepted
- the same arguments are validated and typed
- the same error messages are produced
- the same output is produced
- the same side effects occur
- the same aliases work
- the same help text is shown

The command list, help output and command suggestions are all generated from the
same declaration, so the two entry points cannot drift apart.

A message that does not begin with `$` is treated as ordinary chat and is sent
normally.

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

A command may declare aliases. An alias works through both entry points:

```
$version
$ver
/helm version
/helm ver
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
[HELM] Commands  $name or /helm name
  help - Lists every available command.
  version - Shows the loaded HELM version.
```

Lines are kept short so they do not wrap. Full syntax, aliases and argument
details appear when a line is hovered. Clicking a line fills that command into
the chat box without sending anything to the server.

### Adding a command

A command is declared in one place. Registering it makes it available through
`$name`, through `/helm name`, in the command list and in the help output. No
separate registration is required for either entry point.

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
as `$help` and `/helm help`, with the aliases `h` and `?`. A bare `$` also
shows this list.

## version

Reports the loaded HELM version. Available as `$version` and `/helm version`,
with the alias `ver`.

## User data

HELM stores user data inside the Minecraft game directory in a top level `HELM`
folder, on both Windows and Linux. The location is resolved through the game at
runtime rather than hardcoded, and the directory is created if it does not yet
exist.
