# Regular expression based string generator

## Disclaimer

> [!CAUTION]
> This project is in progress. Most of the features are not implemented yet.

## General info

This library is for generating strings that match a specific regular expression.
Unlike other string generators, it provides an alphabetically ordered virtual list of the generated strings.
This list is lazy, you can get the number of matching strings and the nth of them.

The main use of such sorted lists is the use of them as value sets of searchable data columns.
HoloDB provides regular expression-based searchable string columns using this.

This project is a standalone library,
its only dependency is the `miniconnect-api:lang` project.
The required large numbers are stored in the `LargeInteger` type from this dependency.

## Example of use:

```java
AstNode parsedRegex = new RegexParser().parse("\\d{4}(:([A-Z]{2}|[a-z]{3}))?");
InputGraph inputGraph = new InputGraphGenerator().generate(parsedRegex);
InputGraph sortedInputGraph = new InputGraphSorter().sort(inputGraph);
LargeInteger numberOfStrings = sortedInputGraph.size();
String someString = sortedInputGraph.get(LargeInteger.of(534));
```

## Supported regex constructs:

| Name | Example | Comments |
| ---- | ------- | -------- |
| Character literal | `a` | |
| Escaped literal | `\(` | For all special characters |
| Special literal | `\t`, `\n`, `\r`, `\f`, `\a`, `\e` | |
| Control escape sequence | `\cM` | |
| Octal escape sequence | `\043` | |
| Hexadecimal escape sequence | `\xF5`, `\x{1234}` | |
| Unicode escape sequence | `\u1234` | |
| Unicode/POSIX character classes | `\p{Letter}` | |
| Character class | `[a-z:=]` | |
| Line break | `\R` | Interpreted as `\n` |
| Built-in anchors | `\w`, `\W`, `^`, `$`, `\A`, `\z`, `\Z`, `\G` | Limited support |
| Capturing group | `(abc)` |  |
| Named group | `(?<name>abc)`, `(?P<name>abc)`, `(?'name'abc)` | |
| Non-capturing group | `(?:abc)` | Modifiers are not supported |
| Greedy quantifier | `?`, `*`, `+`, `{2,5}`, `{3,}` | Non-greedy quantifiers are not supported |
| Alternation | `(a|bc)` | |
