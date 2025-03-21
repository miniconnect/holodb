# Changelog

## Version 0.6.0

Released on *2025-03-15*

### Added:

- Added a new regex based value generator (`regex` subproject)
- Added the `--watch` option for live reloading
- Added `BitShufflePermutation` and `BitXorPermutation`
- Added the separate `bootstrap` project
- Added basic GraalVM support

### Improved:

- Improve logging
- Improve performance of permutations and other computations
- Changed `ValueExtractor` to be fully value agnostic

## Development:

- Upgrade to gradle 8
- Improved the config JSON schema generator task
- Extended and improved benchmarks


## Version 0.5.0

Released on *2023-12-16*

### Fixed:

- Set explicit encoding to byte extraction in `TreeRandom::sub(String)`
- Fixed unsigned arithmetics in `FeistelNetworkPermutation` and `HasherTreeRandom`

### Added:

- New column settings in configuration: `defaultValue`, `distributionQuality`, `shuffleQuality`, `sourceFactory`, `sourceFactoryData`
- More and improved permutation types
- Support for default config values at all level for all sublevels
- JSON Schema generator task: `config:generateSchema`

### Improved:

- Made `values` config nullable like others
- Improved key hasher initialization in `FastHasher`
- Made nullability more clear for config constructors
- Extracted `config` project
- Extracted `jpa-annotations` from `jpa` to its own lightweight subproject
- Added module definition and utf-8 encoding explicitly

## Version 0.4.1

Released on *2023-01-12*

### Added:

- MySQL scanner script (see the `tools` directory)
- Integrate `LargeInteger`
- `LargeInteger` and `ImmutableList` in config classes

### Changed:

- `DirtyFpePermutation` was replaced by `ModuloPermutation` for fast initialization

### Improved:

- Improved gradle build

### Removed:

- Version table moved to the new `general-docs` repo
- `examples` directory moved to the new `general-docs` repo

## Version 0.4.0

Released on *2023-01-11*

Please don't use this version except for testing purposes. It depends on a SNAPSHOT version of miniconnect.

## Version 0.3.1

Released on *2022-12-04*

**MiniConnect version**: [0.3.0](https://github.com/miniconnect/miniconnect/blob/master/CHANGELOG.md#version-030)

**Added:**

- Setup holodb from JPA entities
- Setup embedded holodb from configuration file
- `ENUM` column mode
- New config possibilities: `valuesBundle`, `valuesEnum`, and `valuesForeignColumn`

**Improved:**

- Handling of `valuesPattern`
- Improved gradle build
- Type guessing

**Fixed:**

- Ensure full java8 compatibility
- Many more minor fixes

## Version 0.3.0

Released on *2022-12-04*

Please don't use this version except for testing purposes. It depends on a SNAPSHOT version of miniconnect.

## Version 0.2.0

Released on *2022-08-10*

**MiniConnect version**: [0.2.0](https://github.com/miniconnect/miniconnect/blob/master/CHANGELOG.md#version-020)

**Added:**

- new column configurations: `valuesRange`, `valuesResource`, `valuesPattern`, `valuesDynamicPattern`
- Support for database sequences
- Proper support for NULL values
- `possibleValues()` method in `Source`

**Improved:**

- Examples

**Fixed:**

- Malfunction of `PermutatedSelection` and permutated sources
- Inappropriate content of `surnames.txt`
- Problems with NULL matching

## Version 0.1.0

Released on *2022-04-19*

**MiniConnect version**: [0.1.0](https://github.com/miniconnect/miniconnect/blob/master/CHANGELOG.md#version-010)

**Added:**

- Data source framework
- YAML configuration support
- Docker integration
- and more &hellip;
