package dev.liquidcatmofu.resourcereloadguard.client;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import dev.liquidcatmofu.resourcereloadguard.mixin.accessor.OptionsSubScreenAccessor;
import dev.liquidcatmofu.resourcereloadguard.mixin.accessor.VideoSettingsScreenAccessor;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadDecision;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadPolicy;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadReason;
import dev.liquidcatmofu.resourcereloadguard.screen.ReloadGuardScreen;
import dev.liquidcatmofu.resourcereloadguard.state.ReloadGuardState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;

public final class MipmapReloadGuard {
    private MipmapReloadGuard() {}

    public static boolean intercept(Minecraft minecraft, Screen current, Screen target) {
        if (!(current instanceof VideoSettingsScreen video)) return false;
        Options options = ((OptionsSubScreenAccessor)(Object)video).resourceReloadGuard$getOptions();
        int oldLevel = ((VideoSettingsScreenAccessor)(Object)video).resourceReloadGuard$getOldMipmaps();
        int selectedLevel = options.mipmapLevels().get();
        if (oldLevel == selectedLevel) return false;

        ReloadGuardState state = ReloadGuardState.INSTANCE;
        if (ReloadGuardClient.isBusy()) {
            options.mipmapLevels().set(oldLevel);
            ReloadGuardClient.toast("resource_reload_guard.message.reload_in_progress");
            return true;
        }

        ReloadDecision decision = ReloadPolicy.decide(ResourceReloadGuard.config().mipmapPolicy,
            ResourceReloadGuard.config().allowDeferredMipmapApply);
        if (decision == ReloadDecision.ALLOW) {
            options.mipmapLevels().set(oldLevel);
            minecraft.setScreen(target);
            applyNow(minecraft, options, selectedLevel, null);
            return true;
        }
        if (decision == ReloadDecision.BLOCK) {
            options.mipmapLevels().set(oldLevel);
            options.save();
            ReloadGuardClient.toast("resource_reload_guard.message.blocked");
            minecraft.setScreen(target);
            return true;
        }
        if (decision == ReloadDecision.DEFER) {
            options.mipmapLevels().set(oldLevel);
            minecraft.setScreen(target);
            defer(minecraft, options, selectedLevel, null);
            return true;
        }
        if (!state.tryOpenConfirmation(ReloadReason.MIPMAP_CHANGE)) return true;

        // Opening another screen invokes VideoSettingsScreen.removed(). Restore first so vanilla sees no change.
        options.mipmapLevels().set(oldLevel);
        List<ReloadGuardScreen.Action> actions = new ArrayList<>();
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.video_back"),
            () -> backToEditing(minecraft, state, options, selectedLevel, video), false));
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.discard_mipmap"),
            () -> discard(minecraft, state, options, oldLevel, target), false));
        if (ResourceReloadGuard.config().allowDeferredMipmapApply) {
            actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.apply_next_launch"),
                () -> { state.closeConfirmation(); defer(minecraft, options, selectedLevel, target); }, false));
        }
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.apply_now"),
            () -> { state.closeConfirmation(); applyNow(minecraft, options, selectedLevel, target); }, true));

        minecraft.setScreen(new ReloadGuardScreen(
            Component.translatable("resource_reload_guard.screen.mipmap.title"), video,
            List.of(
                Component.translatable("resource_reload_guard.screen.mipmap.summary", oldLevel, selectedLevel),
                Component.translatable("resource_reload_guard.screen.mipmap.message")
            ), actions, ResourceReloadGuard.config().dangerousButtonDelayTicks,
            () -> { options.mipmapLevels().set(selectedLevel); state.closeConfirmation(); }
        ));
        return true;
    }

    private static void backToEditing(Minecraft minecraft, ReloadGuardState state, Options options,
                                      int selectedLevel, Screen video) {
        options.mipmapLevels().set(selectedLevel);
        state.closeConfirmation();
        minecraft.setScreen(video);
    }

    private static void discard(Minecraft minecraft, ReloadGuardState state, Options options,
                                int oldLevel, Screen target) {
        options.mipmapLevels().set(oldLevel);
        options.save();
        state.closeConfirmation();
        if (target != null) minecraft.setScreen(target);
    }

    private static void defer(Minecraft minecraft, Options options, int selectedLevel, Screen target) {
        options.mipmapLevels().set(selectedLevel);
        options.save();
        if (target != null) minecraft.setScreen(target);
        ReloadGuardClient.toast("resource_reload_guard.message.deferred");
    }

    private static void applyNow(Minecraft minecraft, Options options, int selectedLevel, Screen target) {
        options.mipmapLevels().set(selectedLevel);
        options.save();
        minecraft.updateMaxMipLevel(selectedLevel);
        ReloadGuardState state = ReloadGuardState.INSTANCE;
        state.setGuardReloadInProgress(true);
        minecraft.delayTextureReload().whenComplete((ignored, error) ->
            minecraft.execute(() -> state.setGuardReloadInProgress(false)));
        if (target != null) minecraft.setScreen(target);
    }
}
