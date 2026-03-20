package analyzer;

import java.util.*;
import model.ErrorReport;
import parser.StackTraceParser;

public class LogAnalyzer {

    private final StackTraceParser parser = new StackTraceParser();

    public List<ErrorReport> analyze(String content) {
        List<ErrorReport> errors = new ArrayList<>();

        String[] lines = content.split("\n");

        StringBuilder currentError = new StringBuilder();

        for (String line : lines) {
            if (line.contains("Exception")) {
                if (!currentError.isEmpty()) {
                    errors.add(parser.parse(currentError.toString()));
                    currentError.setLength(0);
                }
            }
            currentError.append(line).append("\n");
        }

        if (!currentError.isEmpty()) {
            errors.add(parser.parse(currentError.toString()));
        }

        return errors;
    }
}
