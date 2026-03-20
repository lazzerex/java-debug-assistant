package parser;

import java.util.regex.*;
import model.ErrorReport;

public class StackTraceParser {

    private static final Pattern EXCEPTION_PATTERN =
        Pattern.compile("([a-zA-Z0-9_.]+Exception)");

    private static final Pattern LOCATION_PATTERN =
        Pattern.compile("\\(([A-Za-z0-9_.$]+):(\\d+)\\)");

    public ErrorReport parse(String log) {
        String exceptionType = extractException(log);
        String fileName = "Unknown";
        int lineNumber = -1;

        for (String line : log.split("\\n")) {
            Matcher locationMatcher = LOCATION_PATTERN.matcher(line);
            if (locationMatcher.find()) {
                fileName = locationMatcher.group(1);
                lineNumber = Integer.parseInt(locationMatcher.group(2));
                break; // first frame is usually the source of the error
            }
        }

        return new ErrorReport(exceptionType, fileName, lineNumber, log);
    }

    private String extractException(String log) {
        Matcher matcher = EXCEPTION_PATTERN.matcher(log);
        if (matcher.find()) {
            String fullName = matcher.group(1);
            int lastDot = fullName.lastIndexOf('.');
            return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
        }
        return "UnknownException";
    }
}
