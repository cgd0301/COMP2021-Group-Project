package hk.edu.polyu.comp.comp2021.crobs.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes each handled command as one plain-text line, in execution order.
 */
public class TextOperationLog implements OperationLog {
    private final BufferedWriter writer;
    private boolean finished;

    /**
     * @param destination where the text log is written
     */
    public TextOperationLog(Writer destination) {
        this.writer = new BufferedWriter(destination);
    }

    /**
     * @param command the trimmed command line
     * @throws IOException if the command cannot be written
     */
    @Override
    public void record(String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    /**
     * @throws IOException if the log cannot be closed
     */
    @Override
    public void close() throws IOException {
        if (finished) {
            return;
        }
        finished = true;
        writer.close();
    }
}
