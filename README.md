<p align="center">
  <img src="docs/icon.svg" width="160" alt="Resource Reload Guard icon">
</p>

# Resource Reload Guard

[日本語](README_ja.md) · [Changelog](CHANGELOG.md)

Resource Reload Guard is a client-side mod for Minecraft Java Edition 1.20.1.

Reloading client resources can take several minutes in large modpacks and may cause long garbage-collection pauses, hangs, or crashes. This mod does not make resource loading faster. Instead, it guards known user actions so an expensive reload does not begin accidentally or unnecessarily.

## Supported environments

- Minecraft 1.20.1
- Forge 47.x
- Fabric Loader (Fabric API required)

Forge and Fabric use separate JARs. Install the JAR for your loader in the client `mods` directory. The Fabric build also requires Fabric API.

## Features

### Resource packs

- Detects enabled, disabled, and reordered packs.
- Skips applying a selection when the pack IDs and order are unchanged.
- Lets you return to editing without losing the current selection.
- Can discard the selection and restore the state from before the screen was opened.
- Can apply now or save the selection for the next launch.

### Language

- Skips the reload when the selected language is unchanged.
- Shows the current and selected languages before applying.
- Can apply now or save the language for the next launch.

### Mipmap levels

- Detects a mipmap-level change before leaving Video Settings.
- Can return to Video Settings, discard only the mipmap change, defer it until the next launch, or apply it now.
- Reloads client textures only when **Apply now** is selected.

### Safety measures

- Dangerous actions never receive initial focus.
- `Esc` always returns to a safe screen.
- Dangerous buttons remain disabled until both the configured number of client ticks and the equivalent monotonic time have elapsed.
- Initial loading, mod-initiated reloads, server resource packs, recovery reloads, and reloads of unknown origin are not globally cancelled.

### F3+T

- Stops the vanilla reload before it starts.
- Runs it only after explicit confirmation.
- Prevents duplicate prompts and reloads caused by key repeat.
- Rejects additional user requests while a reload is active.

## Configuration

A commented TOML file is generated on first launch:

```text
config/resource_reload_guard.toml
```

| Key | Default | Description |
| --- | --- | --- |
| `debugKeyPolicy` | `CONFIRM` | Policy for F3+T |
| `resourcePackPolicy` | `CONFIRM` | Policy for resource-pack changes |
| `languagePolicy` | `CONFIRM` | Policy for language changes |
| `mipmapPolicy` | `CONFIRM` | Policy for mipmap-level changes |
| `skipUnchangedResourcePackApply` | `true` | Skip unchanged pack selections |
| `allowDeferredResourcePackApply` | `true` | Allow pack changes to be deferred |
| `allowDeferredLanguageApply` | `true` | Allow language changes to be deferred |
| `allowDeferredMipmapApply` | `true` | Allow mipmap changes to be deferred |
| `blockUserRequestsWhileReloading` | `true` | Reject additional guarded requests during reloads |
| `dangerousButtonDelayTicks` | `20` | Delay before dangerous buttons become active |
| `showLoadedModCount` | `true` | Show the loaded mod count as context |
| `largeEnvironmentWarningThreshold` | `200` | Mod-count threshold for a non-blocking warning |

Available policy values are `ALLOW`, `CONFIRM`, `BLOCK`, and `RESTART_ONLY`. The loaded mod count is used only for an informational warning; it never automatically blocks a reload or estimates its duration.

## Building

Use JDK 21 and run from the repository root. The resulting mod still targets Java 17:

```bash
./gradlew clean build
```

On Windows:

```powershell
.\gradlew.bat clean build
```

Artifacts are generated in `forge/build/libs` and `fabric/build/libs`.

The Fabric `runClient` configuration includes Mod Menu and the full Fabric API as development-only runtime dependencies. They are used to inspect the mod metadata and icon, and are not bundled into the release JAR.

Release automation and required repository settings are documented in [Releasing](docs/releasing.md).

## Known limitations

- The mod does not predict reload duration or optimize the reload pipeline.
- Mods that substantially replace the Resource Packs, Language, or Video Settings screens—or inject into the same vanilla methods—may conflict with its Mixins.
- Server resource packs and reloads whose origin cannot be identified are outside the guard's scope.
- The mod does not restart the Minecraft process automatically.

## License and author

Copyright © 2026 [LiquidCatMofu](https://github.com/liquidcatmofu)

Licensed under the [MIT License](LICENSE).

Repository: [liquidcatmofu/Resource-Reload-Guard](https://github.com/liquidcatmofu/Resource-Reload-Guard)
