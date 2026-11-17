package hk.edu.polyu.comp.comp2021.crobs.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogSafetyRegressionTest {
    @Test
    void existingFileIsNeverDeletedOrTruncatedWhenOtherPathIsInvalid() throws IOException {
        Path dir = Files.createTempDirectory("crobs-log-safety");
        Path html = dir.resolve("existing.html");
        Files.write(html, "keep this".getBytes(StandardCharsets.UTF_8));
        try {
            assertThrows(IOException.class, () -> CompositeOperationLog.open(
                    html.toString(), dir.resolve("missing/log.txt").toString()));
            assertEquals("keep this", new String(Files.readAllBytes(html), StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(html);
            Files.deleteIfExists(dir);
        }
    }
}
