package hk.edu.polyu.comp.comp2021.crobs.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Records each command in every nested log.
 * A Crobs session uses one HTML log and one plain-text log.
 */
public class CompositeOperationLog implements OperationLog {
    private final OperationLog htmlLog;
    private final OperationLog textLog;

    /**
     * @param htmlLog HTML table log
     * @param textLog plain-text log
     */
    public CompositeOperationLog(OperationLog htmlLog, OperationLog textLog) {
        this.htmlLog = htmlLog;
        this.textLog = textLog;
    }

    /**
     * Opens both session logs. The files are replaced, not appended.
     * Nothing is left behind when either path cannot be opened.
     *
     * @param htmlPath HTML log path
     * @param txtPath plain-text log path
     * @return a log that writes both files
     * @throws IOException if the paths are the same file or either file cannot be opened
     */
    public static OperationLog open(String htmlPath, String txtPath) throws IOException {
        rejectSamePath(htmlPath, txtPath);
        Writer htmlWriter = newUtf8Writer(htmlPath);
        Writer txtWriter;
        try {
            txtWriter = newUtf8Writer(txtPath);
        } catch (IOException ex) {
            suppressClose(htmlWriter, ex);
            deleteQuietly(htmlPath, ex);
            throw ex;
        }
        try {
            return new CompositeOperationLog(
                    new HtmlOperationLog(htmlWriter),
                    new TextOperationLog(txtWriter));
        } catch (IOException ex) {
            suppressClose(txtWriter, ex);
            suppressClose(htmlWriter, ex);
            deleteQuietly(txtPath, ex);
            deleteQuietly(htmlPath, ex);
            throw ex;
        }
    }

    /**
     * @param command the trimmed command line
     * @throws IOException if any nested log cannot write the command
     */
    @Override
    public void record(String command) throws IOException {
        htmlLog.record(command);
        textLog.record(command);
    }

    /**
     * @throws IOException if any nested log cannot be closed
     */
    @Override
    public void close() throws IOException {
        IOException failure = finish(htmlLog);
        failure = combine(failure, finish(textLog));
        if (failure != null) {
            throw failure;
        }
    }

    private static void rejectSamePath(String htmlPath, String txtPath) throws IOException {
        String htmlCanonical = new File(htmlPath).getCanonicalPath();
        String txtCanonical = new File(txtPath).getCanonicalPath();
        if (htmlCanonical.equals(txtCanonical)) {
            throw new IOException("HTML and TXT log paths must be different");
        }
    }

    private static Writer newUtf8Writer(String path) throws IOException {
        try {
            return new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IOException("Cannot open log file: " + path + ". " + ex.getMessage(), ex);
        }
    }

    private static void suppressClose(Writer writer, IOException primary) {
        try {
            writer.close();
        } catch (IOException closeFailure) {
            primary.addSuppressed(closeFailure);
        }
    }

    private static void deleteQuietly(String path, IOException primary) {
        if (!new File(path).delete() && new File(path).exists()) {
            primary.addSuppressed(new IOException("Cannot delete incomplete log file: " + path));
        }
    }

    private static IOException finish(OperationLog log) {
        try {
            log.close();
            return null;
        } catch (IOException ex) {
            return ex;
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
}
