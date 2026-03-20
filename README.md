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
# Analyze a log file
java -cp out Main analyze <path-to-log>

# Explain mode (paste a stack trace, then press Enter on an empty line to finish)
java -cp out Main explain
```

## Quick start with the bundled example
An example stack trace is provided at `examples/sample-stacktrace.log`.

```powershell
java -cp out Main analyze examples/sample-stacktrace.log
```

Example output (truncated to the first two findings):
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

## Project layout
- `src/` Java sources (analyzer, parser, explainer, CLI entry points)
- `examples/` Sample stack traces for quick testing
- `out/` Build output (created by `javac -d out ...`)

