package util;

import java.io.IOException;
import java.nio.file.*;

public class FileUtils {
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
