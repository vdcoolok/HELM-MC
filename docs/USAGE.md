# Usage

Every HELM command is available through two equivalent entry points:

| Entry point | Typed in chat | Handled by |
| --- | --- | --- |
| Short prefix | `$name args` | HELM, before the message is sent |
| Standard form | `/helm name args` | the client command system |

Both forms dispatch to the same command and produce the same output. A command
registered once is available through both forms automatically, including its
aliases. Typing `$` also opens the command suggestion list.

A line that begins with `$` is never sent to the server. Lines that do not begin
with `$` are normal chat and are unaffected.

## help

Lists every available command with its syntax, aliases and arguments.

| Entry point | Command |
| --- | --- |
| Short prefix | `$help` |
| Standard form | `/helm help` |

Arguments: none.

Aliases: `h`, `?`.

Example output, one chat line each:

```
[HELM] Commands  $name or /helm name
  help - Lists every available command.
  version - Shows the loaded HELM version.
```

Each command line can be hovered to see its full syntax, aliases and arguments,
and clicked to fill the command into the chat box.

Cancelling: this command has no long running state, so there is nothing to
cancel.

## Command suggestions

Typing `$` in the chat box opens a list of the available commands, in the same
way that typing `/` opens the list of client commands. The list narrows as you
type, arrow keys move through it, and tab or enter fills in the selected
command.

Suggestions stop once a complete command name has been typed, so a command that
takes no arguments shows nothing further.

## version

Reports the loaded HELM version.

| Entry point | Command |
| --- | --- |
| Short prefix | `$version` |
| Standard form | `/helm version` |

Arguments: none.

Aliases: `ver`.

Example output:

```
[HELM] version 0.0.1-26.2
```

Cancelling: this command has no long running state, so there is nothing to
cancel.

## Unknown commands

An unrecognised name reports an error through both entry points in the same
format:

```
[HELM] Unknown command: fly
```

## Invoking a command with a bare prefix

A `$` with nothing after it, or `$` followed only by whitespace, shows the
command list.

## Where user data is stored

HELM stores its user data inside the Minecraft game directory, in a top level
`HELM` folder:

```
<game directory>/HELM/
```

The game directory is `.minecraft` on Linux and macOS and `%APPDATA%\.minecraft`
on Windows. HELM never hardcodes either location; it asks the game for the
directory at runtime. This is the same folder on both platforms.
