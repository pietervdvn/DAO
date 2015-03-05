package database.tools.logging;

import java.io.PrintStream;

public class PrintOut implements Out {

    private final PrintStream out;
    private final String message;

    private String buffer = "";

    public PrintOut(PrintStream out, String message) {
        this.out = out;
        this.message = "[" + message + "] ";
    }

    @Override
    public void print(String message) {
        buffer(message);
    }

    @Override
    public void println(Object message) {
        println(message.toString());
    }

    @Override
    public void println(String message) {
        buffer(message + "\n");
    }

    @Override
    public void println() {
        println("");
    }

    private void buffer(String message) {
        flush(this.buffer + message);
    }

    private void flush(String s) {
       if(s.contains("\r")){
           out.print(s);
           return;
       }
       if (s.contains("\n")) {
            out.print(message);
            out.println(s.substring(0, s.indexOf("\n")));
            flush(s.substring(s.indexOf("\n") + 1));
        } else {
            this.buffer = s;
        }
    }

}
