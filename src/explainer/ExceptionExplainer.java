package explainer;

import model.ErrorReport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionExplainer {

    private static final ExplanationDetail DEFAULT_DETAIL =
        new ExplanationDetail("No explanation available.", List.of("No suggestions available."));

    private final Map<String, ExplanationDetail> catalog;

    public ExceptionExplainer() {
        this(loadCatalog());
    }

    ExceptionExplainer(Map<String, ExplanationDetail> catalog) {
        this.catalog = catalog;
    }

    public ExplanationDetail explain(ErrorReport error) {
        return catalog.getOrDefault(error.getExceptionType(), DEFAULT_DETAIL);
    }

    private static Map<String, ExplanationDetail> loadCatalog() {
        try (InputStream in = ExceptionExplainer.class.getClassLoader().getResourceAsStream("exceptions.json")) {
            if (in == null) {
                return fallbackCatalog();
            }

            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, ExplanationDetail> loaded = parseSimpleJson(json);
            return loaded.isEmpty() ? fallbackCatalog() : loaded;
        } catch (IOException e) {
            return fallbackCatalog();
        }
    }

    private static Map<String, ExplanationDetail> parseSimpleJson(String json) {
        Map<String, ExplanationDetail> loaded = new HashMap<>();
        Pattern entryPattern = Pattern.compile(
            "\\\"([^\\\\\\\"]+)\\\"\\s*:\\s*\\{[^}]*?\\\"explanation\\\"\\s*:\\s*\\\"([^\\\\\\\"]*?)\\\"[^\\[]*?\\[([^]]*?)\\]",
            Pattern.DOTALL);
        Matcher m = entryPattern.matcher(json);
        while (m.find()) {
            String name = unescape(m.group(1));
            String explanation = unescape(m.group(2));
            String suggestionsRaw = m.group(3);

            List<String> suggestions = new ArrayList<>();
            Matcher sm = Pattern.compile("\\\"([^\\\\\\\"]*?)\\\"").matcher(suggestionsRaw);
            while (sm.find()) {
                suggestions.add(unescape(sm.group(1)));
            }

            if (explanation == null || explanation.isBlank()) {
                explanation = DEFAULT_DETAIL.getExplanation();
            }
            if (suggestions.isEmpty()) {
                suggestions = DEFAULT_DETAIL.getSuggestions();
            }

            loaded.put(name, new ExplanationDetail(explanation, Collections.unmodifiableList(suggestions)));
        }
        return loaded;
    }

    private static String unescape(String value) {
        if (value == null) return null;
        return value
            .replace("\\\\", "\\")
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t");
    }

    private static Map<String, ExplanationDetail> fallbackCatalog() {
        Map<String, ExplanationDetail> fallback = new HashMap<>();
        fallback.put("NullPointerException", new ExplanationDetail(
            "You are trying to use an object that has not been initialized.",
            List.of(
                "Check if the object is null before using it",
                "Ensure proper initialization",
                "Trace where the null value comes from"
            )));
        fallback.put("IndexOutOfBoundsException", new ExplanationDetail(
            "You are accessing an index that does not exist.",
            List.of(
                "Check array or list size before access",
                "Validate index values",
                "Use safe iteration"
            )));
        fallback.put("IOException", new ExplanationDetail(
            "There was an input/output operation failure.",
            List.of(
                "Check file paths",
                "Ensure file exists",
                "Handle exceptions properly"
            )));
        fallback.put("NoSuchFileException", new ExplanationDetail(
            "A referenced file path does not exist or is inaccessible.",
            List.of(
                "Verify the path and filename are correct",
                "Ensure the process has permission to read the file",
                "Create the file or adjust configuration to point to an existing one"
            )));
        return fallback;
    }

    private static class RawExplanation {
        public String explanation;
        public List<String> suggestions;
    }
}
