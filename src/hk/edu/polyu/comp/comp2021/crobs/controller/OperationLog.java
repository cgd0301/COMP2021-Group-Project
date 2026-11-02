package hk.edu.polyu.comp.comp2021.crobs.controller;

import java.io.IOException;

/**
 * Records commands handled during one Crobs session.
 * A command is one input line after trimming. Empty lines are not recorded.
 */
public interface OperationLog extends AutoCloseable {
    /**
     * Writes one handled command and flushes it.
     *
     * @param command the trimmed command line
     * @throws IOException if the command cannot be written
     */
    void record(String command) throws IOException;

    /**
     * Finishes and closes this log. A second call does nothing.
     *
     * @throws IOException if the log cannot be closed
     */
    @Override
    void close() throws IOException;
}
