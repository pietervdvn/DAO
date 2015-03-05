package database.tools.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;

public class UserDateLogger implements Out {

    private final int user;
    private final Writer out;

    private final static SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss");

    public UserDateLogger(int u, OutputStream out) {
        this.user = u;
        this.out = new OutputStreamWriter(out);
    }

    public void print(String message) {
        println(message);
    }

    public void println(Object message) {
        println(message.toString());
    }

    @Override
    public void println(String message) {
        try {
            println1(message);
        } catch (IOException e) {
            Logger.err.println("LOGGIN FAILED!");
        }
    }

    public void println1(String message) throws IOException {
        out.append(format.format(new java.util.Date()));
        out.append(" ");
        out.append("user " + user);
        out.append(" ");
        out.append(message);
        out.append('\n');
        out.flush();
    }

    public void println() {
        println("");
    }

}