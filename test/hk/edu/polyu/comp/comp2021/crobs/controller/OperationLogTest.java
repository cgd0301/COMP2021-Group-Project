package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.Application;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationLogTest {
    @Test
    void textRecordsOneCommandPerLine() throws IOException {
        StringWriter destination = new StringWriter();
        TextOperationLog log = new TextOperationLog(destination);
        log.record("login S000000");
        log.record("quit");
        log.close();
        log.close();

        String newline = System.lineSeparator();
        assertEquals("login S000000" + newline + "quit" + newline, destination.toString());
    }

    @Test
    void htmlTableEscapesCommandText() throws IOException {
        StringWriter destination = new StringWriter();
        HtmlOperationLog log = new HtmlOperationLog(destination);
        log.record("book R001 2026-10-01 10:00 11:00 <b>a & b</b>");
        log.record("say \"it's\"");
        log.close();

        String html = destination.toString();
        String newline = System.lineSeparator();
        assertTrue(html.contains("<tr><td>1</td><td>book R001 2026-10-01 10:00 11:00 &lt;b&gt;a &amp; b&lt;/b&gt;</td></tr>" + newline));
        assertTrue(html.contains("<td>2</td><td>say &quot;it&#39;s&quot;</td>"));
        assertTrue(html.contains("</table>"));
        assertFalse(html.contains("<b>"));
        assertFalse(html.contains("a & b"));
    }

    @Test
    void compositeWritesBothDestinations() throws IOException {
        StringWriter htmlDestination = new StringWriter();
        StringWriter textDestination = new StringWriter();
        OperationLog log = new CompositeOperationLog(
                new HtmlOperationLog(htmlDestination),
                new TextOperationLog(textDestination));
        log.record("listAllRooms");
        log.close();

        assertTrue(htmlDestination.toString().contains("<td>1</td><td>listAllRooms</td>"));
        assertEquals("listAllRooms" + System.lineSeparator(), textDestination.toString());
    }

    @Test
    void openRejectsOneFileForBothLogs() throws IOException {
        Path dir = Files.createTempDirectory("crobs-m9-same");
        Path html = dir.resolve("log.html");
        try {
            IOException ex = assertThrows(IOException.class,
                    () -> CompositeOperationLog.open(html.toString(), html.toString()));
            assertEquals("HTML and TXT log paths must be different", ex.getMessage());
            assertFalse(Files.exists(html));
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void openRejectsMissingDirectoryAndRemovesPartialFile() throws IOException {
        Path dir = Files.createTempDirectory("crobs-m9-missing");
        Path html = dir.resolve("log.html");
        Path txt = dir.resolve("missing").resolve("log.txt");
        try {
            IOException ex = assertThrows(IOException.class,
                    () -> CompositeOperationLog.open(html.toString(), txt.toString()));
            assertTrue(ex.getMessage().startsWith("Cannot open log file:"));
            assertFalse(Files.exists(html));
            assertFalse(Files.exists(txt));
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void applicationRecordsHandledLines() throws IOException {
        Path dir = Files.createTempDirectory("crobs-m9-app");
        Path html = dir.resolve("log.html");
        Path txt = dir.resolve("log.txt");
        String input = "\n"
                + "login S000000\n"
                + "book R001 2026-10-01 10:00 11:00 <b>a & b</b>\n"
                + "nope\n"
                + "quit\n";
        try {
            String output = capture(() -> withInput(input, () -> Application.main(new String[]{
                    "-txt", txt.toString(),
                    "-html", html.toString()
            })));
            assertTrue(output.contains("Successfully logged in"));
            assertTrue(output.contains("Input target room ID not found"));
            assertTrue(output.contains("No such command"));
            assertTrue(output.contains("System quit"));

            String newline = System.lineSeparator();
            String text = new String(Files.readAllBytes(txt), StandardCharsets.UTF_8);
            assertEquals("login S000000" + newline
                    + "book R001 2026-10-01 10:00 11:00 <b>a & b</b>" + newline
                    + "nope" + newline
                    + "quit" + newline, text);

            String htmlText = new String(Files.readAllBytes(html), StandardCharsets.UTF_8);
            assertTrue(htmlText.contains("<td>1</td><td>login S000000</td>"));
            assertTrue(htmlText.contains("<td>2</td><td>book R001 2026-10-01 10:00 11:00 &lt;b&gt;a &amp; b&lt;/b&gt;</td>"));
            assertTrue(htmlText.contains("<td>3</td><td>nope</td>"));
            assertTrue(htmlText.contains("<td>4</td><td>quit</td>"));
            assertTrue(htmlText.endsWith("</html>" + newline));
        } finally {
            Files.deleteIfExists(txt);
            Files.deleteIfExists(html);
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void applicationRejectsMissingArguments() {
        String output = capture(() -> Application.main(new String[0]));
        assertTrue(output.contains("Incorrect number of arguments"));
    }

    @Test
    void cliClosesLogWhenInputEndsWithoutQuit() throws IOException {
        StringWriter htmlDestination = new StringWriter();
        StringWriter textDestination = new StringWriter();
        OperationLog log = new CompositeOperationLog(
                new HtmlOperationLog(htmlDestination),
                new TextOperationLog(textDestination));
        capture(() -> withInput("login S000000\n", () -> new Cli().run(new CROBS(), log)));

        assertTrue(htmlDestination.toString().contains("</html>"));
        assertEquals("login S000000" + System.lineSeparator(), textDestination.toString());
    }

    private String capture(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(buffer, true);
        System.setOut(captured);
        try {
            action.run();
        } finally {
            captured.flush();
            System.setOut(original);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private void withInput(String input, Runnable action) {
        InputStream original = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        try {
            action.run();
        } finally {
            System.setIn(original);
        }
    }
}
