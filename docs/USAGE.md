# Usage

Every HELM command is available through two equivalent entry points:

| Entry point | Typed in chat | Handled by |
| --- | --- | --- |
| Short prefix | `$name args` | HELM, before the message is sent |
| Standard form | `/helm name args` | the client command system |

Both forms dispatch to the same command and produce the same output. A command
registered once is available through both forms automatically, including its
aliases.

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

Example output:

```
[HELM] Commands  (use $name or /helm name)
  $help
      Lists every available command.  (aliases: h, ?)
  $version
      Shows the loaded HELM version.  (aliases: ver)
```

Cancelling: this command has no long running state, so there is nothing to
cancel.

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
