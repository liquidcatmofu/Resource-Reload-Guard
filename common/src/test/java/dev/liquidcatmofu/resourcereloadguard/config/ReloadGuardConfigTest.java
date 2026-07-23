package dev.liquidcatmofu.resourcereloadguard.config;

import static org.junit.jupiter.api.Assertions.*;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadPolicyMode;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReloadGuardConfigTest {
    @TempDir Path directory;

    @Test void fillsMissingFields() throws Exception {
        Path path = directory.resolve("config.toml");
        Files.writeString(path, "debugKeyPolicy = \"ALLOW\" # inline comment\n");
        ReloadGuardConfig config = ReloadGuardConfig.load(path);
        assertEquals(ReloadPolicyMode.ALLOW, config.debugKeyPolicy);
        assertEquals(ReloadPolicyMode.CONFIRM, config.resourcePackPolicy);
        assertEquals(ReloadPolicyMode.CONFIRM, config.mipmapPolicy);
        assertTrue(config.skipUnchangedResourcePackApply);
        assertTrue(config.allowDeferredMipmapApply);
    }

    @Test void brokenTomlFallsBackToSafeDefaults() throws Exception {
        Path path = directory.resolve("config.toml");
        Files.writeString(path, "debugKeyPolicy = definitely-not-valid");
        ReloadGuardConfig config = ReloadGuardConfig.load(path);
        assertEquals(ReloadPolicyMode.CONFIRM, config.debugKeyPolicy);
        assertEquals(ReloadPolicyMode.CONFIRM, config.resourcePackPolicy);
        assertEquals(ReloadPolicyMode.CONFIRM, config.languagePolicy);
        assertEquals(ReloadPolicyMode.CONFIRM, config.mipmapPolicy);
    }

    @Test void unknownEnumFallsBackToSafeDefaults() throws Exception {
        Path path = directory.resolve("config.toml");
        Files.writeString(path, "debugKeyPolicy = \"SURPRISE\"");
        assertEquals(ReloadPolicyMode.CONFIRM, ReloadGuardConfig.load(path).debugKeyPolicy);
    }

    @Test void generatedTomlContainsCommentsAndRoundTrips() throws Exception {
        Path path = directory.resolve("config.toml");
        ReloadGuardConfig original = ReloadGuardConfig.defaults();
        original.dangerousButtonDelayTicks = 35;
        original.save(path);
        String text = Files.readString(path);
        assertTrue(text.contains("# Client ticks"));
        assertTrue(text.contains("mipmapPolicy = \"CONFIRM\""));
        assertEquals(35, ReloadGuardConfig.load(path).dangerousButtonDelayTicks);
    }

}
