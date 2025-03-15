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

## Supported constructs:

| Name | Example | Comments |
| ---- | ------- | -------- |
| Character literal | `a` | |
| Escaped literal | `\(` | For anything else than ASCII letters and numbers |
| Special literal | `\t`, `\n`, `\r`, `\f`, `\a`, `\e` | |
| Control escape sequence | `\cM` | |
| Octal escape sequence | `\043` | |
| Hexadecimal escape sequence | `\xF5`, `\x{123}` | |
| Unicode escape sequence | `\u00F5`, `\u{123}` | |
| POSIX character classes | `\p{Alnum}` | |
| Unicode property character class | `\p{IsDigit}`, `\p{IsJoin_Control}` | Binary only, limited support |
| Bracketed character class | `[0-9]`, `[^a-z\d]` | |
| Nested character class | `[a-z[A-Z[^FH]]\p{Digit}]` | |
| Line break | `\R` | Interpreted as `\n` |
| Built-in anchors | `\w`, `\W`, `^`, `$`, `\A`, `\z`, `\Z`, `\G` | Limited support |
| Capturing group | `(abc)` |  |
| Named group | `(?<name>abc)`, `(?P<name>abc)`, `(?'name'abc)` | |
| Non-capturing group | `(?:abc)` | Modifiers are not supported |
| Greedy quantifier | `?`, `*`, `+`, `{2,5}`, `{3,}` | Non-greedy quantifiers are not supported |
| Alternation | `(a|bc)` | |
| Numbered backreference | `\1` | Limited support |
| Named backreference | `\k<name>` | Limited support |
| Quoted fixed string | `\Qa.b?c\E` | Can't be used inside character classes |

## Unsupported constructs:

Here are some features that aren't supported:

- Modifiers
- Character class intersection and subtraction
- Non-binary character properties
- Unicode scripts, blocks, categories
- Non-greedy quantifiers
- Lookahead and lookbehind
- Recursion
- Atomic and other special groups
- Backtracking control verbs

In the future, some of these may be implemented.

## Normalizations

Upper bound of unlimited quantifiers is 3 for groups and 12 for anything other:

- `x*` &rarr; `x{0,12}`
- `x+` &rarr; `x{1,12}`
- `x{<n>,}` &rarr; `x{<n>,<max(12,n+5)>}` where `<n>` is any natural number
- `(xyz)*` &rarr; `(xyz){0,3}`
- `(xyz)+` &rarr; `(xyz){1,3}`
- `(xyz){<n>,}` &rarr; `(xyz){<n>,<max(3,n+1)>}` where `<n>` is any natural number

Unicode property character classes are narrowed, if not empty, to their ASCII subset.

By default, negative character classes are narrowed to the ASCII printable set.
If the remaining set would be empty, the first few allowed printable codepoint will be included.
