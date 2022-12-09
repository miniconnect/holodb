# Changelog

<table>
  <thead>
    <tr>
      <th>
        miniconnect-api
      </th>
      <th>
        <a href="https://github.com/miniconnect/miniconnect">miniconnect</a>
      </th>
      <th>
        minibase
      </th>
      <th>
        <a>holodb</a>
      </th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td rowspan="2">---</td>
      <td><a href="https://github.com/miniconnect/miniconnect/blob/master/CHANGELOG.md#version-030">0.3.0</a></td>
      <td rowspan="2">---</td>
      <td><a href="#version-031">0.3.1</a></td>
    </tr>
    <tr>
      <td rowspan="2">---</td>
      <td><a href="https://github.com/miniconnect/miniconnect/blob/master/CHANGELOG.md#version-020">0.2.0</a></td>
      <td rowspan="2">---</td>
      <td><a href="#version-020">0.2.0</a></td>
    </tr>
    <tr>
      <td><a href="https://github.com/miniconnect/miniconnect/blob/master/CHANGELOG.md#version-010">0.1.0</a></td>
      <td><a href="#version-010">0.1.0</a></td>
    </tr>
  </tbody>
</table>

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
