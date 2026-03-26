# Debug Assistant

A small CLI that parses stack traces and explains common Java exceptions. It can analyze a log file, read from stdin, or accept pasted stack traces interactively.

## Requirements
- JDK 11 or later
- No external libraries are required at runtime
- Build and run use `javac`/`java` only

## Build
Use `javac` directly.

```powershell
# Manual javac (Windows PowerShell)
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
```
```bash
# Manual javac (POSIX)
find src -name '*.java' -print0 | xargs -0 javac -d out
```

## Run
```powershell
# Show help
java -cp out Main --help

# Analyze a log file
java -cp out Main --input examples/sample-stacktrace.log

# Read from stdin (piping)
type examples/sample-stacktrace.log | java -cp out Main --input -

# Paste interactively (finish with an empty line)
java -cp out Main --prompt

# Emit JSON instead of text
java -cp out Main --input examples/sample-stacktrace.log --format json

# Limit number of reported errors
java -cp out Main --input examples/sample-stacktrace.log --limit 1
```

## Quick start with the bundled example
An example stack trace is provided at `examples/sample-stacktrace.log`.

```powershell
java -cp out Main --input examples/sample-stacktrace.log
```

Example output (text format):
```
Error: NullPointerException
Location: App.java:42

Explanation:
You are trying to use an object that has not been initialized.

Suggested fixes:
- Check if the object is null before using it
- Ensure proper initialization
- Trace where the null value comes from

--------------------------------
Error: IOException
Location: ConfigLoader.java:27

Explanation:
There was an input/output operation failure.

Suggested fixes:
- Check file paths
- Ensure file exists
- Handle exceptions properly
```

Example JSON output:
```powershell
java -cp out Main --input examples/sample-stacktrace.log --format json
```
```json
[
  {
    "error": "NullPointerException",
    "file": "App.java",
    "line": 42,
    "explanation": "You are trying to use an object that has not been initialized.",
    "suggestedFixes": [
      "Check if the object is null before using it",
      "Ensure proper initialization",
      "Trace where the null value comes from"
    ]
  },
  {
    "error": "IOException",
    "file": "ConfigLoader.java",
    "line": 27,
    "explanation": "There was an input/output operation failure.",
    "suggestedFixes": [
      "Check file paths",
      "Ensure file exists",
      "Handle exceptions properly"
    ]
  }
]
```

## Options
- `--help` / `-h`: Show usage
- `--version`: Show version
- `--input <file|->`: Input log file path or `-` for stdin (required unless `--prompt`)
- `--prompt`: Paste mode; reads from stdin until an empty line (for copy/paste scenarios)
- `--format <text|json>`: Output format (default: text)
- `--limit <n>`: Limit the number of errors reported
- `--color <auto|always|never>`: Color output mode (default: auto)
- `--no-color`: Disable ANSI colors in text output (alias for `--color never`)

## Exit codes
- `0`: Success (results printed or nothing found)
- `1`: Bad arguments
- `2`: I/O error (missing/unreadable file, etc.)

## Notes
- File reads default to UTF-8 and normalize newlines for cross-platform consistency (Windows/Linux/macOS/FreeBSD).
- Text output uses ANSI colors when a TTY/`TERM` is detected; override with `--color` / `--no-color` for CI or Windows consoles.
- A data file (`resources/exceptions.json`) powers the explanation catalog and can be extended.
- Forward-slash paths are used in docs/examples for portability.

## Project layout
- `src/` Java sources (analyzer, parser, explainer, CLI entry points)
- `resources/` Exception catalog JSON
- `examples/` Sample stack traces for quick testing
- `out/` Build output (created by `javac -d out ...`)
