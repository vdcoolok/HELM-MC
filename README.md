<div align="center">

# HELM

### Minecraft client automation for Fabric

**Minecraft automation mod. Macros, pathfinding, mining and farming. A better Baritone.**

[Documentation](docs/FULLYEXPLAINEDFEATURES.md) ·
[Commands](docs/USAGE.md) ·
[Features](docs/FEATURES.md) ·
[Settings](docs/SETTINGS.md) ·
[Macro syntax](docs/MACROSYNTAX.md) ·
[Report an issue](https://github.com/vdcoolok/HELM-MC/issues) ·
[License](LICENSE)

</div>

---

<div align="center">

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-5C7CFA?style=for-the-badge&logo=modrinth&logoColor=white&labelColor=1B1B1F)](https://minecraft.net)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.19.3%2B-DB4C3C?style=for-the-badge&logo=fabric&logoColor=white&labelColor=1B1B1F)](https://fabricmc.net)
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.161.0%2B26.2-DB4C3C?style=for-the-badge&logo=fabric&logoColor=white&labelColor=1B1B1F)](https://modrinth.com/mod/fabric-api)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white&labelColor=1B1B1F)](https://openjdk.org)
[![Client only](https://img.shields.io/badge/Client--only-8B5CF6?style=for-the-badge&labelColor=1B1B1F)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-LGPL--3.0-8B5CF6?style=for-the-badge&labelColor=1B1B1F)](COPYING)

[![Stars](https://img.shields.io/github/stars/vdcoolok/HELM-MC?style=for-the-badge&logo=github&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/stargazers)
[![Forks](https://img.shields.io/github/forks/vdcoolok/HELM-MC?style=for-the-badge&logo=github&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/network/members)
[![Watchers](https://img.shields.io/github/watchers/vdcoolok/HELM-MC?style=for-the-badge&logo=github&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/watchers)

[![Last commit](https://img.shields.io/github/last-commit/vdcoolok/HELM-MC?style=for-the-badge&logo=git&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/commits)
[![Commit activity](https://img.shields.io/github/commit-activity/m/vdcoolok/HELM-MC?style=for-the-badge&logo=git&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/commits)
[![Issues](https://img.shields.io/github/issues/vdcoolok/HELM-MC?style=for-the-badge&logo=github&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/issues)
[![Pull requests](https://img.shields.io/github/issues-pr/vdcoolok/HELM-MC?style=for-the-badge&logo=github&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/pulls)
[![Contributors](https://img.shields.io/github/contributors/vdcoolok/HELM-MC?style=for-the-badge&logo=github&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC/graphs/contributors)
[![Top language](https://img.shields.io/github/languages/top/vdcoolok/HELM-MC?style=for-the-badge&logo=openjdk&logoColor=white&labelColor=1B1B1F)](https://github.com/vdcoolok/HELM-MC)

</div>

---

<div align="center">

<a href="https://star-history.com/#vdcoolok/HELM-MC&Date">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=vdcoolok/HELM-MC&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=vdcoolok/HELM-MC&type=Date" />
    <img alt="Star History" src="https://api.star-history.com/svg?repos=vdcoolok/HELM-MC&type=Date" />
  </picture>
</a>

</div>

---

## What it does

HELM is a client side automation mod. Everything runs on your machine, nothing is
sent to the server, and every command is typed in your own chat box.

| Area | What you get |
| --- | --- |
| **Navigation** | `goto` any block, and keep going: the route is re-planned from wherever you actually end up |
| **Holding** | Anchor a position and walk back if you are knocked off it, or lock the camera to a direction, until you stop |
| **Movement** | Continuous walking with no gap at a step boundary, real sprinting, jumps, gaps, diagonals and drops |
| **World** | Breaks what is in the way, places what is missing, picks the right tool, all without a keypress |
| **Aiming** | Free look, so mining and placing never drag the camera around |
| **Farming** | Harvest ripe crops, replant the ground, and pick up the drops it broke its own crops into |
| **Outlines** | Glowing silhouettes around blocks to mine, ripe crops and the drops worth collecting |
| **Macros** | A readable script language for anything the commands do not already cover |
| **Storage** | Everything you author lives in one readable folder inside your game directory |

Full details of every feature are in
[FULLYEXPLAINEDFEATURES.md](docs/FULLYEXPLAINEDFEATURES.md). The one line index
is in [FEATURES.md](docs/FEATURES.md).

## Requirements

| | Version |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.161.0+26.2 |
| Java | 25 |

HELM is client only. It does not need to be installed on a server, and a server
never sees a HELM command.

## Commands

Type `$` in the chat box, then the command. Anything starting with `$` is handled
by HELM and is never sent to the server.

```
$goto 120 64 -35
$stop
$help
```

| Command | What it does |
| --- | --- |
| `$help` | List every command |
| `$version` | Show the HELM version |
| `$goto <x> <y> <z>` | Walk to a block position |
| `$autogoto <x> <y> <z>` | Walk there and keep going back if moved off it |
| `$autogotohere` | Hold the block you are standing in |
| `$autolookat <pitch>/<yaw>` | Lock the camera to an angle |
| `$autolookathere` | Lock the angle you are facing now |
| `$stop` | Stop walking, macros, anchors and locked angles |
| `$set` | Open the settings picker |
| `$set <name> [<value>]` | Show or change one setting |
| `$settings reset` | Restore every setting to its default |
| `$macro <create\|edit\|load\|list>` | Work with macros |

[USAGE.md](docs/USAGE.md) has the full list, every alias, and the exact reply
each command gives.

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/installer) for Minecraft
   26.2.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2.
3. Drop `HELM` and Fabric API into the `mods` folder of that instance.

## Building

The build produces `HELM<version>-<minecraft version>.jar`, for example
`HELM0.1.0-26.2.jar`, and copies it to the repository root.

| Platform | Command |
| --- | --- |
| Windows | `build.bat` |
| Linux, macOS | `./build.sh` |

To build without the convenience scripts:

| Platform | Command |
| --- | --- |
| Windows | `gradlew.bat build` |
| Linux, macOS | `./gradlew build` |

Both Gradle wrappers are committed, so no separate Gradle installation is needed.
The wrapper selects the required Gradle and Java versions.

## Running the development client

| Platform | Command |
| --- | --- |
| Windows | `gradlew.bat runClient` |
| Linux, macOS | `./gradlew runClient` |

## Where your data lives

Everything you author is stored inside the Minecraft game directory, in a top
level `HELM` folder:

```
<game directory>/HELM/
```

The game directory is `.minecraft` on Linux and macOS and `%APPDATA%\.minecraft`
on Windows. HELM resolves this through the game at runtime and never hardcodes
it, so the folder is named the same on both platforms.

| Path | What it holds |
| --- | --- |
| `HELM/macros/` | Your macro files |
| `HELM/settings.conf` | Every setting, one per line |
| `HELM/debuglogs.log` | A record of what HELM did this session |
| `HELM/cache/` | Remembered chunks, so routes can cross unloaded terrain |

The text files are safe to read, edit or delete while the game is closed.
`cache` is safe to delete at any time; you only lose remembered terrain.

## Project layout

Source lives under `src/main/java/dev/helm`. Folders are created as the features
they serve are added, so every folder in the tree contains code that does
something.

| Package | Responsibility |
| --- | --- |
| `access` | Reaching into the game where no accessor exists yet |
| `aim` | Where the body is pointed, and whether the camera is free |
| `command` | Command declaration, parsing, execution and dispatch |
| `control` | What the player is pressing |
| `diag` | The session log and its formatting |
| `drops` | Which items came out of the blocks HELM broke, and whether they are wanted |
| `input` | Key and button names |
| `interaction` | Mining and placing |
| `macro` | The macro language, its editor and its runtime |
| `movement` | Step execution, route following and sprint decisions |
| `navigate` | Goals, journeys and the walk loop |
| `outline` | Glowing silhouettes around blocks and dropped items |
| `pathfinding` | Search, costs, moves and world rules |
| `render` | Drawing the route |
| `rotation` | Applying and capturing rotations |
| `setting` | Every setting, and where they are stored |
| `storage` | Where files are written |
| `tools` | Tool choice, break strength and block lists |
| `world` | Reading the world, and the chunk cache |
| `mixin` | Mixins declared by `helm.mixins.json` |

Minecraft and Fabric specific hooks live in `mixin`; everything else is
expressed in terms of small, focused classes.

## Contributing

1. Fork the repository and create a branch.
2. Make your change, including the documentation updates it requires.
3. Build on the platform you have available:

   | Platform | Command |
   | --- | --- |
   | Windows | `build.bat` |
   | Linux, macOS | `./build.sh` |

4. Commit with a short message that describes the change.
5. Open a pull request.

Windows and Linux are both first class. No platform specific tooling is required:
building on either needs only its own Gradle wrapper, which is committed. A change
that would only work on one platform is not acceptable.

Attach `HELM/debuglogs.log` to a bug report. It is the fastest way to see what
HELM thought it was doing.

## Star history

<div align="center">

<a href="https://star-history.com/#vdcoolok/HELM-MC&Date">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=vdcoolok/HELM-MC&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=vdcoolok/HELM-MC&type=Date" />
    <img alt="Star History" src="https://api.star-history.com/svg?repos=vdcoolok/HELM-MC&type=Date" />
  </picture>
</a>

</div>

## Star history

The chart above is generated by [star-history.com](https://star-history.com) and
refreshes each time the page is loaded. It needs the repository to be public, so
it shows as unavailable while the repository is private.

## License

HELM is licensed under the GNU Lesser General Public License v3.0. See
[LICENSE](LICENSE) for the LGPL terms and [COPYING](COPYING) for the GNU General
Public License v3.0 that it incorporates.
