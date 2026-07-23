package dev.liquidcatmofu.resourcereloadguard.client;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadDecision;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadPolicy;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadReason;
import dev.liquidcatmofu.resourcereloadguard.screen.ReloadGuardScreen;
import dev.liquidcatmofu.resourcereloadguard.state.ReloadGuardState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ReloadGuardClient {
    private ReloadGuardClient() {}

    public static void requestDebugReload() {
        Minecraft minecraft = Minecraft.getInstance();
        ReloadGuardState state = ReloadGuardState.INSTANCE;
        if (isBusy()) { toast("resource_reload_guard.message.reload_in_progress"); return; }
        ReloadDecision decision = ReloadPolicy.decide(ResourceReloadGuard.config().debugKeyPolicy, false);
        if (decision == ReloadDecision.ALLOW) { startGuardReload(minecraft); return; }
        if (decision == ReloadDecision.BLOCK || decision == ReloadDecision.DEFER) {
            toast("resource_reload_guard.message.blocked"); return;
        }
        Screen parent = minecraft.screen;
        if (!state.tryOpenConfirmation(ReloadReason.DEBUG_KEY)) return;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("resource_reload_guard.screen.debug.message.1"));
        lines.add(Component.translatable("resource_reload_guard.screen.debug.message.2"));
        lines.add(Component.translatable("resource_reload_guard.screen.debug.message.3"));
        int mods = ResourceReloadGuard.loadedModCount();
        if (ResourceReloadGuard.config().showLoadedModCount && mods >= 0)
            lines.add(Component.translatable("resource_reload_guard.screen.loaded_mods", mods));
        if (mods >= ResourceReloadGuard.config().largeEnvironmentWarningThreshold)
            lines.add(Component.translatable("resource_reload_guard.warning.large_environment"));
        ReloadGuardScreen screen = new ReloadGuardScreen(
            Component.translatable("resource_reload_guard.screen.debug.title"), parent, lines,
            List.of(
                new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.cancel"), () -> closeTo(parent), false),
                new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.reload"), () -> {
                    closeTo(parent);
                    startGuardReload(minecraft);
                }, true)
            ), ResourceReloadGuard.config().dangerousButtonDelayTicks, state::closeConfirmation);
        minecraft.setScreen(screen);
    }

    public static boolean isBusy() {
        ReloadGuardState state = ReloadGuardState.INSTANCE;
        return ResourceReloadGuard.config().blockUserRequestsWhileReloading
            && (state.globalReloadInProgress() || state.guardReloadInProgress());
    }

    public static void startGuardReload(Minecraft minecraft) {
        ReloadGuardState state = ReloadGuardState.INSTANCE;
        state.setGuardReloadInProgress(true);
        minecraft.reloadResourcePacks().whenComplete((ignored, error) -> minecraft.execute(() -> state.setGuardReloadInProgress(false)));
    }

    public static void closeTo(Screen screen) {
        ReloadGuardState.INSTANCE.closeConfirmation();
        Minecraft.getInstance().setScreen(screen);
    }

    public static void toast(String key) {
        Minecraft minecraft = Minecraft.getInstance();
        SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
            Component.translatable("resource_reload_guard.toast.title"), Component.translatable(key));
    }
}
