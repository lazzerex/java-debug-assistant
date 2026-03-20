# Debug Assistant

A small CLI that parses stack traces and explains common Java exceptions. It can analyze a log file or accept pasted stack traces interactively.

## Requirements
- JDK 11 or later
- Windows PowerShell commands below assume you run them from the project root (`debug-assistant`).

## Build
```powershell
# Compile all sources into the out/ directory
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
```

## Usage
```powershell
# Show help
java -cp out Main --help

# Analyze a log file
java -cp out Main --input examples/sample-stacktrace.log

# Read from stdin (e.g., piping a stack trace)
type examples\sample-stacktrace.log | java -cp out Main --input -

# Paste interactively (end with empty line)
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

Options:
- `--help` / `-h`: Show usage
- `--version`: Show version
- `--input <file|->`: Input log file path or `-` for stdin (required unless `--prompt`)
- `--prompt`: Paste mode; reads from stdin until an empty line
- `--format <text|json>`: Output format (default: text)
- `--limit <n>`: Limit the number of errors reported
- `--no-color`: Disable ANSI colors in text output

## Project layout
- `src/` Java sources (analyzer, parser, explainer, CLI entry points)
- `examples/` Sample stack traces for quick testing
- `out/` Build output (created by `javac -d out ...`)
