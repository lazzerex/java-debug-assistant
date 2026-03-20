package model;

public class ErrorReport {
    private String exceptionType;
    private String fileName;
    private int lineNumber;
    private String rawMessage;

    public ErrorReport(String exceptionType, String fileName, int lineNumber, String rawMessage) {
        this.exceptionType = exceptionType;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.rawMessage = rawMessage;
    }

    public String getExceptionType() { return exceptionType; }
    public String getFileName() { return fileName; }
    public int getLineNumber() { return lineNumber; }
    public String getRawMessage() { return rawMessage; }
}
