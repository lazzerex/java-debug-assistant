package cli;

import analyzer.LogAnalyzer;
import explainer.ExceptionExplainer;
import explainer.ExplanationDetail;
import model.ErrorReport;
import util.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    private static final String VERSION = "0.1.0";
    private static final String COLOR_CYAN = "\u001B[36m";
    private static final String COLOR_YELLOW = "\u001B[33m";
    private static final String COLOR_GREEN = "\u001B[32m";
    private static final String COLOR_RESET = "\u001B[0m";

    private enum ColorMode { AUTO, ALWAYS, NEVER }

    private final LogAnalyzer analyzer = new LogAnalyzer();
    private final ExceptionExplainer explainer = new ExceptionExplainer();

    public int handle(String[] args) {
        CliOptions options;
        try {
            options = parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            return 1;
        }

        if (options.showHelp) {
            printUsage();
            return 0;
        }

        if (options.showVersion) {
            System.out.println("Debug Assistant version " + VERSION);
            return 0;
        }

        if (options.input == null && !options.promptMode) {
            System.err.println("Error: --input <file|-> is required unless --prompt is used");
            printUsage();
            return 1;
        }

        String content;
        try {
            content = options.promptMode ? readPrompt() : readInput(options.input);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }

        List<ErrorReport> errors = analyzer.analyze(content);
        if (options.limit >= 0 && errors.size() > options.limit) {
            errors = new ArrayList<>(errors.subList(0, options.limit));
        }

        if (errors.isEmpty()) {
            System.err.println("No errors found.");
            return 0;
        }

        boolean colorEnabled = shouldUseColor(options.colorMode);

        if ("json".equals(options.format)) {
            System.out.println(formatJson(errors));
        } else {
            formatText(errors, colorEnabled).forEach(System.out::println);
        }

        return 0;
    }

    private CliOptions parseArgs(String[] args) {
        CliOptions options = new CliOptions();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help":
                case "-h":
                    options.showHelp = true;
                    break;
                case "--version":
                    options.showVersion = true;
                    break;
                case "--format":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for --format");
                    }
                    options.format = args[++i].toLowerCase();
                    if (!options.format.equals("text") && !options.format.equals("json")) {
                        throw new IllegalArgumentException("Unsupported format: " + options.format);
                    }
                    break;
                case "--input":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for --input");
                    }
                    options.input = args[++i];
                    break;
                case "--limit":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for --limit");
                    }
                    try {
                        options.limit = Integer.parseInt(args[++i]);
                        if (options.limit < 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("--limit must be a non-negative integer");
                    }
                    break;
                case "--no-color":
                    options.colorMode = ColorMode.NEVER;
                    break;
                case "--color":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for --color");
                    }
                    String colorValue = args[++i].toLowerCase();
                    switch (colorValue) {
                        case "auto":
                            options.colorMode = ColorMode.AUTO;
                            break;
                        case "always":
                            options.colorMode = ColorMode.ALWAYS;
                            break;
                        case "never":
                            options.colorMode = ColorMode.NEVER;
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported color mode: " + colorValue);
                    }
                    break;
                case "--prompt":
                    options.promptMode = true;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        return options;
    }

    private String readInput(String input) throws IOException {
        if ("-".equals(input)) {
            return readStdin();
        }

        Path path = Paths.get(input);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + input);
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Not a file: " + input);
        }

        return FileUtils.readFile(input);
    }

    private String readStdin() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        return content.toString();
    }

    private String readPrompt() throws IOException {
        System.out.println("Paste stack trace (finish with an empty line):");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            content.append(line).append("\n");
        }
        return content.toString();
    }

    private List<String> formatText(List<ErrorReport> errors, boolean colorEnabled) {
        List<String> output = new ArrayList<>();

        for (ErrorReport error : errors) {
            ExplanationDetail detail = explainer.explain(error);
            StringBuilder builder = new StringBuilder();

            builder.append(colorize("Error", COLOR_CYAN, colorEnabled))
                   .append(": ").append(error.getExceptionType()).append("\n");

            builder.append(colorize("Location", COLOR_YELLOW, colorEnabled))
                   .append(": ").append(error.getFileName())
                   .append(":").append(error.getLineNumber()).append("\n\n");

            builder.append(colorize("Explanation", COLOR_GREEN, colorEnabled))
                   .append(":\n").append(detail.getExplanation()).append("\n\n");

            builder.append(colorize("Suggested fixes", COLOR_GREEN, colorEnabled))
                   .append(":\n");
            for (String fix : detail.getSuggestions()) {
                builder.append("- ").append(fix).append("\n");
            }

            output.add(builder.toString());
            output.add("--------------------------------");
        }

        return output;
    }

    private String formatJson(List<ErrorReport> errors) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < errors.size(); i++) {
            ErrorReport error = errors.get(i);
            ExplanationDetail detail = explainer.explain(error);

            builder.append("{");
            builder.append("\"error\":\"").append(escapeJson(error.getExceptionType())).append("\",");
            builder.append("\"file\":\"").append(escapeJson(error.getFileName())).append("\",");
            builder.append("\"line\":").append(error.getLineNumber()).append(",");
            builder.append("\"explanation\":\"").append(escapeJson(detail.getExplanation())).append("\",");
            builder.append("\"suggestedFixes\":[");
            for (int j = 0; j < detail.getSuggestions().size(); j++) {
                builder.append("\"").append(escapeJson(detail.getSuggestions().get(j))).append("\"");
                if (j < detail.getSuggestions().size() - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
            builder.append("}");

            if (i < errors.size() - 1) {
                builder.append(",");
            }
        }

        builder.append("]");
        return builder.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private String colorize(String value, String color, boolean enabled) {
        if (!enabled) {
            return value;
        }
        return color + value + COLOR_RESET;
    }

    private boolean shouldUseColor(ColorMode mode) {
        if (mode == ColorMode.ALWAYS) return true;
        if (mode == ColorMode.NEVER) return false;
        // AUTO
        String term = System.getenv("TERM");
        boolean hasConsole = System.console() != null;
        boolean termSupports = term != null && !term.equalsIgnoreCase("dumb");
        return hasConsole && termSupports;
    }

    private void printUsage() {
        System.out.println("Usage: java -cp out Main [options]\n" +
                "  --help, -h            Show help\n" +
                "  --version             Show version\n" +
                "  --input <file|->      Input file path or '-' for stdin (required unless --prompt)\n" +
                "  --prompt              Paste mode; read from stdin until an empty line\n" +
                "  --format <text|json>  Output format (default: text)\n" +
                "  --limit <n>           Limit number of errors reported\n" +
                "  --color <auto|always|never>  Color output mode (default: auto)\n" +
                "  --no-color            Disable ANSI colors (alias for --color never)");
    }

    private static class CliOptions {
        boolean showHelp;
        boolean showVersion;
        String format = "text";
        String input;
        int limit = -1;
        ColorMode colorMode = ColorMode.AUTO;
        boolean promptMode;
    }
}
