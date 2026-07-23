# Changelog

All notable changes to Resource Reload Guard are documented in this file.

The project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.1.0] - 2026-07-23

### Added

- Initial release for Minecraft 1.20.1 on Forge and Fabric.
- Confirmation before applying resource-pack, language, and mipmap-level changes that trigger client resource reloads.
- Options to return to editing, discard supported changes, apply immediately, or defer supported changes until the next launch.
- Confirmation before an F3+T resource reload.
- Protection against duplicate confirmations and additional guarded requests while a reload is active.
- A configurable safety delay for destructive or reload-triggering actions.
- A commented TOML configuration file with per-action policies.
- English and Japanese translations.
