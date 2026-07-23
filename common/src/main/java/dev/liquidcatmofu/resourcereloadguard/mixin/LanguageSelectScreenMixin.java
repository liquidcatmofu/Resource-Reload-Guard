package dev.liquidcatmofu.resourcereloadguard.mixin;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import dev.liquidcatmofu.resourcereloadguard.client.ReloadGuardClient;
import dev.liquidcatmofu.resourcereloadguard.client.LanguageEntryDuck;
import dev.liquidcatmofu.resourcereloadguard.mixin.accessor.LanguageSelectScreenAccessor;
import dev.liquidcatmofu.resourcereloadguard.mixin.accessor.OptionsSubScreenAccessor;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadDecision;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadPolicy;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadReason;
import dev.liquidcatmofu.resourcereloadguard.screen.ReloadGuardScreen;
import dev.liquidcatmofu.resourcereloadguard.state.ReloadGuardState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageSelectScreen.class)
abstract class LanguageSelectScreenMixin {
    @Unique private String resourceReloadGuard$pendingCode;

    @Inject(method = "init", at = @At("TAIL"))
    private void resourceReloadGuard$restoreSelection(CallbackInfo ci) {
        if (resourceReloadGuard$pendingCode == null) return;
        ObjectSelectionList list = resourceReloadGuard$list();
        if (list == null) return;
        for (Object entry : list.children()) {
            if (entry instanceof LanguageEntryDuck duck && resourceReloadGuard$pendingCode.equals(duck.resourceReloadGuard$getCode())) {
                list.setSelected((ObjectSelectionList.Entry)entry);
                return;
            }
        }
    }

    @Inject(method = "onDone", at = @At("HEAD"), cancellable = true)
    private void resourceReloadGuard$guardLanguage(CallbackInfo ci) {
        ReloadGuardState state = ReloadGuardState.INSTANCE;
        if (state.bypass()) return;
        LanguageSelectScreenAccessor accessor = (LanguageSelectScreenAccessor)this;
        ObjectSelectionList<?> list = resourceReloadGuard$list();
        Object selected = list == null ? null : list.getSelected();
        if (!(selected instanceof LanguageEntryDuck duck)) return;
        String selectedCode = duck.resourceReloadGuard$getCode();
        LanguageManager manager = accessor.resourceReloadGuard$getLanguageManager();
        OptionsSubScreenAccessor optionsScreen = (OptionsSubScreenAccessor)this;
        Screen parent = optionsScreen.resourceReloadGuard$getLastScreen();
        Options options = optionsScreen.resourceReloadGuard$getOptions();
        if (selectedCode.equals(manager.getSelected())) {
            Minecraft.getInstance().setScreen(parent); ci.cancel(); return;
        }
        resourceReloadGuard$pendingCode = selectedCode;
        if (ReloadGuardClient.isBusy()) { ReloadGuardClient.toast("resource_reload_guard.message.reload_in_progress"); ci.cancel(); return; }
        ReloadDecision decision = ReloadPolicy.decide(ResourceReloadGuard.config().languagePolicy,
            ResourceReloadGuard.config().allowDeferredLanguageApply);
        if (decision == ReloadDecision.ALLOW) { state.withBypass(accessor::resourceReloadGuard$invokeOnDone); ci.cancel(); return; }
        if (decision == ReloadDecision.BLOCK) { ReloadGuardClient.toast("resource_reload_guard.message.blocked"); ci.cancel(); return; }
        if (decision == ReloadDecision.DEFER) { resourceReloadGuard$defer(options, selectedCode, parent); ci.cancel(); return; }
        if (!state.tryOpenConfirmation(ReloadReason.LANGUAGE_CHANGE)) { ci.cancel(); return; }
        Component oldName = resourceReloadGuard$languageName(manager, manager.getSelected());
        Component newName = resourceReloadGuard$languageName(manager, selectedCode);
        Screen editing = (Screen)(Object)this;
        List<ReloadGuardScreen.Action> actions = new ArrayList<>();
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.language_back"), () -> ReloadGuardClient.closeTo(editing), false));
        if (ResourceReloadGuard.config().allowDeferredLanguageApply)
            actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.apply_next_launch"), () -> { state.closeConfirmation(); resourceReloadGuard$defer(options, selectedCode, parent); }, false));
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.apply_now"), () -> { state.closeConfirmation(); state.withBypass(accessor::resourceReloadGuard$invokeOnDone); }, true));
        Minecraft.getInstance().setScreen(new ReloadGuardScreen(Component.translatable("resource_reload_guard.screen.language.title"), editing,
            List.of(Component.translatable("resource_reload_guard.screen.language.summary", oldName, newName),
                    Component.translatable("resource_reload_guard.screen.language.message")),
            actions, ResourceReloadGuard.config().dangerousButtonDelayTicks, state::closeConfirmation));
        ci.cancel();
    }

    @Unique private void resourceReloadGuard$defer(Options options, String code, Screen parent) {
        options.languageCode = code;
        options.save();
        ReloadGuardState.INSTANCE.closeConfirmation();
        Minecraft.getInstance().setScreen(parent);
        ReloadGuardClient.toast("resource_reload_guard.message.deferred");
    }

    @Unique private Component resourceReloadGuard$languageName(LanguageManager manager, String code) {
        LanguageInfo info = manager.getLanguage(code);
        return info == null ? Component.literal(code) : info.toComponent();
    }

    @Unique private ObjectSelectionList<?> resourceReloadGuard$list() {
        for (Object child : ((Screen)(Object)this).children()) {
            if (child instanceof ObjectSelectionList<?> list) return list;
        }
        return null;
    }
}
