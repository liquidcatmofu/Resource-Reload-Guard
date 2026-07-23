package dev.liquidcatmofu.resourcereloadguard;

import dev.liquidcatmofu.resourcereloadguard.config.ReloadGuardConfig;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceReloadGuard {
    public static final String MOD_ID = "resource_reload_guard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ReloadGuardConfig config = ReloadGuardConfig.defaults();
    private static int loadedModCount = -1;

    private ResourceReloadGuard() {}

    public static void initialize(Path configDirectory, int modCount) {
        config = ReloadGuardConfig.load(configDirectory.resolve(MOD_ID + ".toml"));
        loadedModCount = modCount;
    }

    public static ReloadGuardConfig config() { return config; }
    public static int loadedModCount() { return loadedModCount; }
}
