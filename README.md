# HELM

Minecraft client automation mod for Fabric. Navigation, movement, world interaction, combat, inventory handling and schematic construction.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API for 26.2
- JDK 25

## Building

The build produces `build/libs/HELM<version>-<minecraft version>.jar`.

On Linux and macOS:

```
./gradlew build
```

On Windows:

```
gradlew.bat build
```

Both wrappers are committed, so no separate Gradle installation is needed. The
Gradle wrapper selects the required Gradle and JDK versions automatically.

To clear regenerable build output and then produce the jar:

| Platform | Command |
| --- | --- |
| Linux, macOS | `./build.sh` |
| Windows | `build.bat` |

Both scripts delegate to the `helmBuild` Gradle task, so the underlying work is
identical on every platform.

## Running the development client

| Platform | Command |
| --- | --- |
| Linux, macOS | `./gradlew runClient` |
| Windows | `gradlew.bat runClient` |

## Gradle tasks

| Task | Purpose |
| --- | --- |
| `build` | Compile, process resources and assemble the jar. |
| `helmBuild` | Remove regenerable build output, then run `build`. |
| `cleanCaches` | Remove build output, the run directory and stale toolchain locks. |
| `purgeLoomCache` | Remove the project toolchain cache. Run on its own. |
| `runClient` | Launch the development client. |

`cleanCaches` deliberately keeps the shared dependency and Minecraft caches,
because removing them only makes builds slower. `purgeLoomCache` is a separate
task because the project toolchain cache is populated during configuration and
cannot be removed while a build is in progress.

## Project layout

Source lives under `src/main/java/dev/helm`.

| Package | Responsibility |
| --- | --- |
| `client` | Client lifecycle and access to client state. |
| `command` | Command registration and dispatch. |
| `config` | User configuration. |
| `combat` | Targeting and combat behaviour. |
| `entity` | Entity queries and tracking. |
| `interaction` | Block and item interaction. |
| `inventory` | Inventory and item handling. |
| `movement` | Movement control. |
| `navigation` | Navigation goals and request handling. |
| `pathfinding` | Path search and path representation. |
| `rotation` | Player rotation control. |
| `schematic` | Schematic loading and placement. |
| `task` | Task execution. |
| `util` | Shared helpers. |
| `world` | World, block and collision queries. |

Minecraft-specific hooks are kept in `client` and in the mixin package declared
by `helm.mixins.json`.

## License

HELM is licensed under the GNU Lesser General Public License v3.0. See
[LICENSE](LICENSE) for the LGPL terms and [COPYING](COPYING) for the GNU General
Public License v3.0 that they incorporate.
