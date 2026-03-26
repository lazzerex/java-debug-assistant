package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileUtils {
    public static String readFile(String path) throws IOException {
        // Normalize newlines to keep parsing consistent across platforms
        return normalizeNewlines(Files.readString(Paths.get(path), StandardCharsets.UTF_8));
    }

    public static String normalizeNewlines(String content) {
        return content.replace("\r\n", "\n").replace("\r", "\n");
    }
}
