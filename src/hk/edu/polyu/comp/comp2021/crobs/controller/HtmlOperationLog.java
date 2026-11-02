package hk.edu.polyu.comp.comp2021.crobs.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes handled commands into an HTML table.
 * Each data row has the operation index and the command text.
 * Markup characters in the command are escaped so the browser shows them as text.
 */
public class HtmlOperationLog implements OperationLog {
    private static final int FIRST_INDEX = 1;
    private static final String DOCUMENT_START = joinLines(
            "<!DOCTYPE html>",
            "<html>",
            "<head>",
            "<meta charset=\"UTF-8\">",
            "<title>Crobs Operation Log</title>",
            "<style>",
            "table { border-collapse: collapse; }",
            "td { border: 1px solid black; padding: 4px; }",
            "</style>",
            "</head>",
            "<body>",
            "<table>");
    private static final String DOCUMENT_END = joinLines(
            "</table>",
            "</body>",
            "</html>");

    private final BufferedWriter writer;
    private int nextIndex = FIRST_INDEX;
    private boolean finished;

    /**
     * Writes the HTML table opening immediately.
     *
     * @param destination where the HTML log is written
     * @throws IOException if the table opening cannot be written
     */
    public HtmlOperationLog(Writer destination) throws IOException {
        this.writer = new BufferedWriter(destination);
        try {
            this.writer.write(DOCUMENT_START);
            this.writer.flush();
        } catch (IOException ex) {
            suppressClose(ex);
            throw ex;
        }
    }

    /**
     * @param command the trimmed command line
     * @throws IOException if the row cannot be written
     */
    @Override
    public void record(String command) throws IOException {
        writer.write("<tr><td>");
        writer.write(Integer.toString(nextIndex));
        writer.write("</td><td>");
        writer.write(escape(command));
        writer.write("</td></tr>");
        writer.newLine();
        writer.flush();
        nextIndex++;
    }

    /**
     * @throws IOException if the table cannot be finished or the log cannot be closed
     */
    @Override
    public void close() throws IOException {
        if (finished) {
            return;
        }
        finished = true;
        IOException failure = writeEnd();
        failure = combine(failure, closeWriter());
        if (failure != null) {
            throw failure;
        }
    }

    private IOException writeEnd() {
        try {
            writer.write(DOCUMENT_END);
            return null;
        } catch (IOException ex) {
            return ex;
        }
    }

    private IOException closeWriter() {
        try {
            writer.close();
            return null;
        } catch (IOException ex) {
            return ex;
        }
    }

    private void suppressClose(IOException primary) {
        try {
            writer.close();
        } catch (IOException closeFailure) {
            primary.addSuppressed(closeFailure);
        }
    }

    private static IOException combine(IOException first, IOException second) {
        if (first == null) {
            return second;
        }
        if (second != null) {
            first.addSuppressed(second);
        }
        return first;
    }

    private static String escape(String command) {
        StringBuilder builder = new StringBuilder(command.length());
        for (int index = 0; index < command.length(); index++) {
            char character = command.charAt(index);
            switch (character) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&#39;");
                    break;
                default:
                    builder.append(character);
                    break;
            }
        }
        return builder.toString();
    }

    private static String joinLines(String... lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
}
