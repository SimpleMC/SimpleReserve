# Changelog

## [Unreleased]

## [2.0.0] - 2025-05-21
### Added
- MIT license

### Changed
- Rewrote plugin in Kotlin
- MC 1.21, Java 21, Kotlin 2.1
- **BREAKING**: New config format

### Removed
- Help command (duplicated built-in usage functionality)

### Fixed
- Close listeners on reload
- `KICK` method will now function for players with both kick and full permissions and `BOTH` reserve method

## [1.0.1] - 2020-06-28
### Changed
- Switch java target from Java 11 to 1.8
- Update gradle to 6.5

## [1.0.0] - 2020-02-13
### Added
- CHANGELOG
- Github Actions for build and automated releases

### Changed
- Adopt semver via axion-release plugin
- Target MC version 1.15

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

[Unreleased]: https://github.com/SimpleMC/SimpleReserve/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/SimpleMC/SimpleReserve/compare/v1.0.1...v2.0.0
[1.0.1]: https://github.com/SimpleMC/SimpleReserve/compare/release-1.0.0...release-1.0.1
[1.0.0]: https://github.com/SimpleMC/SimpleReserve/releases/tag/release-1.0.0
