package cli;

import java.util.*;
import java.io.*;
import analyzer.LogAnalyzer;
import explainer.ExceptionExplainer;
import model.ErrorReport;
import util.FileUtils;

public class CommandHandler {

    private LogAnalyzer analyzer = new LogAnalyzer();
    private ExceptionExplainer explainer = new ExceptionExplainer();

    public void handle(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: analyze <file> | explain");
            return;
        }

        if (args[0].equals("analyze")) {
            String content = FileUtils.readFile(args[1]);
            process(content);
        } else if (args[0].equals("explain")) {
            System.out.println("Paste stack trace (end with empty line):");
            Scanner sc = new Scanner(System.in);
            StringBuilder input = new StringBuilder();

            while (true) {
                String line = sc.nextLine();
                if (line.isEmpty()) break;
                input.append(line).append("\n");
            }

            process(input.toString());
        }
    }

    private void process(String content) {
        List<ErrorReport> errors = analyzer.analyze(content);

        for (ErrorReport error : errors) {
            System.out.println(explainer.explain(error));
            System.out.println("--------------------------------");
        }
    }
}
