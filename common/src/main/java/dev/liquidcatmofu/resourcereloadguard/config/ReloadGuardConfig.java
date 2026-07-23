package dev.liquidcatmofu.resourcereloadguard.config;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadPolicyMode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReloadGuardConfig {
    public ReloadPolicyMode debugKeyPolicy = ReloadPolicyMode.CONFIRM;
    public ReloadPolicyMode resourcePackPolicy = ReloadPolicyMode.CONFIRM;
    public ReloadPolicyMode languagePolicy = ReloadPolicyMode.CONFIRM;
    public ReloadPolicyMode mipmapPolicy = ReloadPolicyMode.CONFIRM;
    public boolean skipUnchangedResourcePackApply = true;
    public boolean allowDeferredResourcePackApply = true;
    public boolean allowDeferredLanguageApply = true;
    public boolean allowDeferredMipmapApply = true;
    public boolean blockUserRequestsWhileReloading = true;
    public int dangerousButtonDelayTicks = 20;
    public boolean showLoadedModCount = true;
    public int largeEnvironmentWarningThreshold = 200;
    public boolean logReloadRequests = true;

    public static ReloadGuardConfig defaults() { return new ReloadGuardConfig(); }

    public void normalize() {
        if (debugKeyPolicy == null) debugKeyPolicy = ReloadPolicyMode.CONFIRM;
        if (resourcePackPolicy == null) resourcePackPolicy = ReloadPolicyMode.CONFIRM;
        if (languagePolicy == null) languagePolicy = ReloadPolicyMode.CONFIRM;
        if (mipmapPolicy == null) mipmapPolicy = ReloadPolicyMode.CONFIRM;
        dangerousButtonDelayTicks = Math.max(0, Math.min(200, dangerousButtonDelayTicks));
        largeEnvironmentWarningThreshold = Math.max(0, largeEnvironmentWarningThreshold);
    }

    public static ReloadGuardConfig load(Path path) {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                ReloadGuardConfig defaults = defaults();
                defaults.save(path);
                return defaults;
            }
            ReloadGuardConfig result = parseToml(Files.readString(path, StandardCharsets.UTF_8));
            result.normalize();
            return result;
        } catch (Exception exception) {
            ResourceReloadGuard.LOGGER.warn("Could not read {}; using safe defaults", path, exception);
            return defaults();
        }
    }

    private static ReloadGuardConfig parseToml(String text) {
        ReloadGuardConfig result = defaults();
        int lineNumber = 0;
        for (String rawLine : text.split("\\R", -1)) {
            lineNumber++;
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) continue;
            int separator = line.indexOf('=');
            if (separator <= 0) throw new IllegalArgumentException("Invalid TOML at line " + lineNumber);
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            switch (key) {
                case "debugKeyPolicy" -> result.debugKeyPolicy = parsePolicy(value);
                case "resourcePackPolicy" -> result.resourcePackPolicy = parsePolicy(value);
                case "languagePolicy" -> result.languagePolicy = parsePolicy(value);
                case "mipmapPolicy" -> result.mipmapPolicy = parsePolicy(value);
                case "skipUnchangedResourcePackApply" -> result.skipUnchangedResourcePackApply = parseBoolean(value);
                case "allowDeferredResourcePackApply" -> result.allowDeferredResourcePackApply = parseBoolean(value);
                case "allowDeferredLanguageApply" -> result.allowDeferredLanguageApply = parseBoolean(value);
                case "allowDeferredMipmapApply" -> result.allowDeferredMipmapApply = parseBoolean(value);
                case "blockUserRequestsWhileReloading" -> result.blockUserRequestsWhileReloading = parseBoolean(value);
                case "dangerousButtonDelayTicks" -> result.dangerousButtonDelayTicks = Integer.parseInt(value);
                case "showLoadedModCount" -> result.showLoadedModCount = parseBoolean(value);
                case "largeEnvironmentWarningThreshold" -> result.largeEnvironmentWarningThreshold = Integer.parseInt(value);
                case "logReloadRequests" -> result.logReloadRequests = parseBoolean(value);
                default -> { /* Forward-compatible: ignore fields from newer versions. */ }
            }
        }
        return result;
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"' && (i == 0 || line.charAt(i - 1) != '\\')) quoted = !quoted;
            if (character == '#' && !quoted) return line.substring(0, i);
        }
        return line;
    }

    private static ReloadPolicyMode parsePolicy(String value) {
        if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"')
            throw new IllegalArgumentException("Policy values must be quoted");
        return ReloadPolicyMode.valueOf(value.substring(1, value.length() - 1));
    }

    private static boolean parseBoolean(String value) {
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;
        throw new IllegalArgumentException("Invalid boolean: " + value);
    }

    public void save(Path path) throws IOException {
        String toml = """
            # Resource Reload Guard configuration
            # Policy values: \"ALLOW\", \"CONFIRM\", \"BLOCK\", or \"RESTART_ONLY\".

            # F3+T policy. RESTART_ONLY blocks this action because it cannot be deferred.
            debugKeyPolicy = \"%s\"

            # Policies for changes made in the resource-pack, language, and video screens.
            resourcePackPolicy = \"%s\"
            languagePolicy = \"%s\"
            mipmapPolicy = \"%s\"

            # Skip applying a resource-pack selection when its IDs and order are unchanged.
            skipUnchangedResourcePackApply = %s

            # Show the \"apply on next launch\" option where supported.
            allowDeferredResourcePackApply = %s
            allowDeferredLanguageApply = %s
            allowDeferredMipmapApply = %s

            # Reject additional user requests while any client resource reload is active.
            blockUserRequestsWhileReloading = %s

            # Client ticks before dangerous confirmation buttons become active (0-200).
            # 20 ticks are approximately one second under normal conditions.
            dangerousButtonDelayTicks = %d

            # Display the loaded mod count as informational context only.
            showLoadedModCount = %s

            # Show a non-blocking warning at or above this loaded-mod count.
            largeEnvironmentWarningThreshold = %d

            # Log intercepted reload requests.
            logReloadRequests = %s
            """.formatted(
                debugKeyPolicy, resourcePackPolicy, languagePolicy, mipmapPolicy,
                skipUnchangedResourcePackApply, allowDeferredResourcePackApply, allowDeferredLanguageApply, allowDeferredMipmapApply,
                blockUserRequestsWhileReloading, dangerousButtonDelayTicks, showLoadedModCount,
                largeEnvironmentWarningThreshold, logReloadRequests);
        Files.writeString(path, toml, StandardCharsets.UTF_8);
    }
}
