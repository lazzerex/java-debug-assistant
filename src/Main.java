import cli.CommandHandler;

public class Main {
    public static void main(String[] args) throws Exception {
        CommandHandler handler = new CommandHandler();
        handler.handle(args);
    }
}
