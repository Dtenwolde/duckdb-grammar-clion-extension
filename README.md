# DuckDB Grammar Support for CLion

A CLion plugin that provides IDE support for [DuckDB](https://duckdb.org/)'s PEG grammar files (`.gram`).

## Features

- **Syntax highlighting** — distinct colours for rule names, separators (`<-`), literals, references, operators, choices, quantifiers, regex patterns, parameters, and comments
- **Go to Definition** — `Cmd+Click` or `F12` on any rule reference navigates to its definition, across files
- **Find Usages** — find all references to a grammar rule across the project
- **Grammar → Transformer navigation** — gutter icons on grammar rules that have a corresponding C++ transformer; clicking navigates directly to the `PEGTransformerFactory::Transform<Rule>` method in the relevant `transform_*.cpp` file
- **Transformer → Grammar navigation** — reverse gutter icons in C++ transformer files link back to the grammar rule
- **Structure view** — `Cmd+7` shows all grammar rules in the file structure panel, with alphabetic sorting available
- **Code folding** — multi-line rule bodies can be collapsed via the gutter fold indicator or `Cmd+.`; the placeholder shows the first line of the body
- **Commenter** — `Cmd+/` toggles `#` line comments

## Installation

Install from the [JetBrains Marketplace](https://plugins.jetbrains.com/) by searching for **DuckDB Grammar Support**, or go to **Settings → Plugins → Marketplace**.

## Building from Source

Requirements: JDK 21, internet access (Gradle downloads dependencies on first build).

```bash
git clone --recurse-submodules https://github.com/duckdb/duckdb-grammar-clion-extension
cd duckdb-grammar-clion-extension
./gradlew buildPlugin
```

The plugin ZIP is written to `build/distributions/`.

To launch a sandboxed CLion instance with the plugin loaded:

```bash
./gradlew runIde
```

## Running Tests

```bash
./gradlew test
```

Tests use IntelliJ's `LexerTestCase` and cover the three-state lexer (rule name → separator → rule body), multi-line continuation via both trailing and leading `/`, quantifiers, regex patterns, comments, escaped literals, and lexer restart correctness.

## Publishing

Set your JetBrains Marketplace token, then run the publish script:

```bash
export JETBRAINS_MARKETPLACE_TOKEN=<your_token>

# Keep current version
./publish.sh

# Or bump the version first
./publish.sh --version 1.1.0
```

The script runs `buildPlugin`, `verifyPlugin`, and `publishPlugin` in sequence.

## Grammar File Format

DuckDB uses a [PEG](https://en.wikipedia.org/wiki/Parsing_expression_grammar) grammar with the following syntax:

```
# Comment
ruleName <- alternative1 / alternative2
ruleName <- termA termB+
ruleName <- 'literal' / [regex] / otherRule?

# Parameterised rule
ruleName(param) <- 'text'

# Multi-line alternatives (leading '/' style)
ruleName <- firstAlternative
    / secondAlternative
    / thirdAlternative
```

| Token | Meaning |
|---|---|
| `ruleName <- …` | Rule definition |
| `'text'` | Literal string |
| `[a-z]` / `<name>` | Character class / named regex |
| `ref` | Reference to another rule |
| `/` | Ordered choice (PEG "or") |
| `?` `*` `+` | Optional, zero-or-more, one-or-more |
| `(…)` | Grouping |
| `!` | Negative lookahead |
| `# …` | Line comment |

## Project Structure

```
src/main/kotlin/com/duckdb/gram/
├── lexer/          # Hand-written stateful lexer (three-state machine)
├── parser/         # Light PSI parser
├── psi/            # PSI element types (GramRule, GramRuleRef, …)
├── highlighting/   # Syntax highlighter, colour settings page, and commenter
├── folding/        # Code folding for multi-line rule bodies
├── structure/      # Structure view (file outline of grammar rules)
├── reference/      # Go-to-Definition via file-based index
├── index/          # GramRuleNameIndex and TransformerMethodIndex
├── usage/          # Find Usages provider
├── navigation/     # Gutter icon providers (grammar ↔ C++ transformer)
└── icons/          # SVG icons
```

## License

[MIT](LICENSE)