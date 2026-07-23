# Releasing

Releases are created by `.github/workflows/release.yml` when a matching Git tag is pushed.

## Version scheme

- Mod metadata: `SemVer`, for example `0.1.0`
- Git tag and GitHub Release: `MinecraftVersion-SemVer`, for example `1.20.1-0.1.0`
- Public JAR: `ResourceReloadGuard-Loader-MinecraftVersion-SemVer.jar`, for example `ResourceReloadGuard-Fabric-1.20.1-0.1.0.jar`
- Modrinth/CurseForge version: `Loader-MinecraftVersion-SemVer`, for example `Fabric-1.20.1-0.1.0`

The workflow verifies that the tag matches `minecraft_version` and `mod_version` in `gradle.properties`. It also requires a matching section in `CHANGELOG.md`.

## Release channels

The release channel is derived from `mod_version`, so there is no separate channel property that can drift out of sync:

| `mod_version` | Modrinth/CurseForge | GitHub Release |
| --- | --- | --- |
| `0.2.0` | Release | Normal release |
| `0.2.0-alpha.1` | Alpha | Pre-release |
| `0.2.0-beta.1` | Beta | Pre-release |
| `0.2.0-rc.1` | Beta | Pre-release |

GitHub does not distinguish Alpha from Beta, so the SemVer suffix carries that distinction. Unknown prerelease labels are rejected. Pre-releases are explicitly excluded from `Latest`.

The same SemVer, including its prerelease suffix, is expanded into the Fabric and Forge metadata inside each JAR. Minecraft compatibility remains separate metadata.

## Version source and increments

`gradle.properties` is the committed source of truth for `mod_version` and `minecraft_version`. Loader metadata files contain placeholders, so the JAR version is supplied by Gradle rather than duplicated manually.

Although GitHub Actions could override `mod_version` with `-Pmod_version=...`, making CI the only source would make local builds and release reproduction less predictable. The publishing workflow therefore validates and publishes the committed version; it does not increment versions or commit changes.

If version bump automation is added later, keep it separate from publishing: a preparation workflow should accept the intended SemVer/channel, update `gradle.properties` and `CHANGELOG.md`, and open a pull request. After that pull request is reviewed, merged, and passes CI, the existing tag-driven workflow can publish the exact committed state.

## Repository configuration

Add these GitHub Actions repository variables:

- `MODRINTH_PROJECT_ID`
- `CURSEFORGE_PROJECT_ID`

Add these GitHub Actions repository secrets:

- `MODRINTH_TOKEN` (a Modrinth personal access token with `VERSION_CREATE`)
- `CURSEFORGE_TOKEN`

The built-in `GITHUB_TOKEN` publishes the GitHub Release and needs no additional secret.

## Platform metadata

Resource Reload Guard is a client-only mod.

- Modrinth project type: `mod`
- Modrinth version environment: client only
- CurseForge file environment: client only

Modrinth environment metadata belongs to each uploaded version rather than the project as a whole. The release workflow runs Gradle on JDK 21, while the compiled mod and published compatibility metadata continue to target Java 17. Every Modrinth and CurseForge upload explicitly declares one loader, one Minecraft version, Java 17, and the client-only environment. The Fabric upload declares Fabric API as a required dependency.

## Release procedure

1. Update `mod_version` and `minecraft_version` in `gradle.properties` as needed. Use an `alpha`, `beta`, or `rc` SemVer suffix for a pre-release.
2. Add the matching version section to `CHANGELOG.md`.
3. Merge the release commit into the default branch and ensure CI passes.
4. Create and push the exact release tag, for example:

   ```bash
   git tag 1.20.1-0.1.0
   git push origin 1.20.1-0.1.0
   ```

The workflow builds once, attaches both loader JARs to one GitHub Release, and creates a separate one-file version for each loader on Modrinth and CurseForge.
