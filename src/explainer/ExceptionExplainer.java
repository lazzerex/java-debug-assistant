package explainer;

import java.util.*;
import model.ErrorReport;

public class ExceptionExplainer {

    private static final Map<String, String> explanations = new HashMap<>();
    private static final Map<String, List<String>> suggestions = new HashMap<>();

    static {
        explanations.put("NullPointerException",
            "You are trying to use an object that has not been initialized.");

        suggestions.put("NullPointerException", List.of(
            "Check if the object is null before using it",
            "Ensure proper initialization",
            "Trace where the null value comes from"
        ));

        explanations.put("IndexOutOfBoundsException",
            "You are accessing an index that does not exist.");

        suggestions.put("IndexOutOfBoundsException", List.of(
            "Check array or list size before access",
            "Validate index values",
            "Use safe iteration"
        ));

        explanations.put("IOException",
            "There was an input/output operation failure.");

        suggestions.put("IOException", List.of(
            "Check file paths",
            "Ensure file exists",
            "Handle exceptions properly"
        ));

        explanations.put("NoSuchFileException",
            "A referenced file path does not exist or is inaccessible.");

        suggestions.put("NoSuchFileException", List.of(
            "Verify the path and filename are correct",
            "Ensure the process has permission to read the file",
            "Create the file or adjust configuration to point to an existing one"
        ));

        explanations.put("translateToIOException",
            "The filesystem layer translated an internal Windows error to an IOException.");

        suggestions.put("translateToIOException", List.of(
            "Inspect preceding stack frames for the real cause",
            "Check disk/permission issues on the referenced path",
            "Retry after ensuring the target resource is available"
        ));

        explanations.put("rethrowAsIOException",
            "A low-level filesystem error was rethrown as an IOException.");

        suggestions.put("rethrowAsIOException", List.of(
            "Check the original exception in the stack trace",
            "Validate file paths and access rights",
            "Handle or log the underlying cause for diagnostics"
        ));
    }

    public String explain(ErrorReport error) {
        String type = error.getExceptionType();

        String explanation = explanations.getOrDefault(type, "No explanation available.");
        List<String> fixes = suggestions.getOrDefault(type, List.of("No suggestions available."));

        StringBuilder result = new StringBuilder();

        result.append("Error: ").append(type).append("\n");
        result.append("Location: ").append(error.getFileName())
              .append(":").append(error.getLineNumber()).append("\n\n");

        result.append("Explanation:\n").append(explanation).append("\n\n");

        result.append("Suggested fixes:\n");
        for (String fix : fixes) {
            result.append("- ").append(fix).append("\n");
        }

        return result.toString();
    }
}
