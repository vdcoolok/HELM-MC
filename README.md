# HELM

Minecraft client automation mod for Fabric. Navigation, movement, world
interaction, combat, inventory handling and schematic construction.

- Minecraft: 26.2
- Fabric Loader: 0.19.3 or newer
- Fabric API: 0.161.0+26.2
- Java: 25

## Documentation

| Document | Contents |
| --- | --- |
| [docs/FEATURES.md](docs/FEATURES.md) | Short index of every feature |
| [docs/USAGE.md](docs/USAGE.md) | How to use every command |
| [docs/FULLYEXPLAINEDFEATURES.md](docs/FULLYEXPLAINEDFEATURES.md) | Detailed behaviour of every feature |
| [docs/MACROSYNTAX.md](docs/MACROSYNTAX.md) | The macro script syntax |
| [docs/PATHFINDING.md](docs/PATHFINDING.md) | How routes are found and walked |
| [docs/SETTINGS.md](docs/SETTINGS.md) | Every setting, its default and what it does |

## Building

The build produces `HELM<version>-<minecraft version>.jar`, for example
`HELM0.0.1-26.2.jar`, and copies it to the repository root.

Linux and macOS:

```
./build.sh
```

Windows:

```
build.bat
```

To build without the convenience scripts:

| Platform | Command |
| --- | --- |
| Linux, macOS | `./gradlew build` |
| Windows | `gradlew.bat build` |

Both Gradle wrappers are committed, so no separate Gradle installation is
needed. The wrapper selects the required Gradle and Java versions.

## Running the development client

| Platform | Command |
| --- | --- |
| Linux, macOS | `./gradlew runClient` |
| Windows | `gradlew.bat runClient` |

## Installing

Copy the built jar into the `mods` folder of the Minecraft instance you want to
use it with, and make sure Fabric API is installed alongside it.

## Where user data is stored

HELM stores its user data inside the Minecraft game directory, in a top level
`HELM` folder:

```
<game directory>/HELM/
```

The game directory is `.minecraft` on Linux and macOS and `%APPDATA%\.minecraft`
on Windows. HELM resolves this location through the game at runtime and never
hardcodes it, so the folder is named the same on both platforms. Macros are
stored in the `macros` folder inside it, and settings in `settings.conf` beside
it. Both are plain text and safe to edit by hand.

The folder and its `macros` subfolder are created automatically the first time
you join a world. If they cannot be created, HELM reports it once in chat and
the features that need them stay unavailable rather than failing silently.

## Project layout

Source lives under `src/main/java/dev/helm`. Folders are created as the
features they serve are added, so every folder in the tree contains code that
does something.

| Package | Responsibility |
| --- | --- |
| `command` | Command declaration, parsing, execution and dispatch |
| `mixin` | Mixins declared by `helm.mixins.json` |

Minecraft and Fabric specific hooks live in `mixin`, while command behaviour is
expressed in terms of small, focused components under `command`.

## Contributing

1. Fork the repository and create a branch.
2. Make your change, including the documentation updates it requires.
3. Build on the platform you have available:

   | Platform | Command |
   | --- | --- |
   | Linux, macOS | `./build.sh` |
   | Windows | `build.bat` |

4. Commit with a short message that describes the change.
5. Open a pull request.

No platform specific tooling is required. Building on either platform needs
only its own Gradle wrapper, which is committed.

Windows and Linux are both supported. Changes that would only work on one
platform are not acceptable.

## License

HELM is licensed under the GNU Lesser General Public License v3.0. See
[LICENSE](LICENSE) for the LGPL terms and [COPYING](COPYING) for the GNU General
Public License v3.0 that it incorporates.
