# Releasing

Releases are created by `.github/workflows/release.yml` when a matching Git tag is pushed.

## Version scheme

- Mod metadata: `SemVer`, for example `0.1.0`
- Git tag and GitHub Release: `MinecraftVersion-SemVer`, for example `1.20.1-0.1.0`
- Public JAR: `ResourceReloadGuard-Loader-MinecraftVersion-SemVer.jar`, for example `ResourceReloadGuard-Fabric-1.20.1-0.1.0.jar`
- Modrinth/CurseForge version: `Loader-MinecraftVersion-SemVer`, for example `Fabric-1.20.1-0.1.0`

The workflow verifies that the tag matches `minecraft_version` and `mod_version` in `gradle.properties`. It also requires a matching section in `CHANGELOG.md`.

## Repository configuration

Add these GitHub Actions repository variables:

- `MODRINTH_PROJECT_ID`
- `CURSEFORGE_PROJECT_ID`

Add these GitHub Actions repository secrets:

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

The built-in `GITHUB_TOKEN` publishes the GitHub Release and needs no additional secret.

## Platform metadata

Resource Reload Guard is a client-only mod.

- Modrinth project type: `mod`
- Modrinth client side: `required`
- Modrinth server side: `unsupported`
- CurseForge project environment: client only

The release workflow refuses to publish to Modrinth unless the project-level side settings match the values above. It runs Gradle on JDK 21, while the compiled mod and published compatibility metadata continue to target Java 17. Every Modrinth and CurseForge upload also explicitly declares one loader, one Minecraft version, Java 17, and the required client environment. The Fabric upload declares Fabric API as a required dependency.

## Release procedure

1. Update `mod_version` and `minecraft_version` in `gradle.properties` as needed.
2. Add the matching version section to `CHANGELOG.md`.
3. Merge the release commit into the default branch and ensure CI passes.
4. Create and push the exact release tag, for example:

   ```bash
   git tag 1.20.1-0.1.0
   git push origin 1.20.1-0.1.0
   ```

The workflow builds once, attaches both loader JARs to one GitHub Release, and creates a separate one-file version for each loader on Modrinth and CurseForge.
