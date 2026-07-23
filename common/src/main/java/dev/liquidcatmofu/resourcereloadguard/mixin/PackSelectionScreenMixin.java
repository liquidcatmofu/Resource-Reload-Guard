package dev.liquidcatmofu.resourcereloadguard.mixin;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import dev.liquidcatmofu.resourcereloadguard.client.ReloadGuardClient;
import dev.liquidcatmofu.resourcereloadguard.mixin.accessor.PackSelectionModelAccessor;
import dev.liquidcatmofu.resourcereloadguard.mixin.accessor.PackSelectionScreenAccessor;
import dev.liquidcatmofu.resourcereloadguard.policy.PackDiffCalculator;
import dev.liquidcatmofu.resourcereloadguard.policy.PackSelectionDiff;
import dev.liquidcatmofu.resourcereloadguard.policy.PackSelectionSnapshot;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadDecision;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadPolicy;
import dev.liquidcatmofu.resourcereloadguard.policy.ReloadReason;
import dev.liquidcatmofu.resourcereloadguard.screen.ReloadGuardScreen;
import dev.liquidcatmofu.resourcereloadguard.state.ReloadGuardState;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackSelectionScreen.class)
abstract class PackSelectionScreenMixin {
    @Unique private PackSelectionSnapshot resourceReloadGuard$initial;
    @Unique private Screen resourceReloadGuard$parent;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void resourceReloadGuard$capture(PackRepository repository, Consumer<PackRepository> output, Path path,
                                             Component title, CallbackInfo ci) {
        resourceReloadGuard$parent = Minecraft.getInstance().screen;
        ReloadGuardState.INSTANCE.setPackParent(resourceReloadGuard$parent);
        resourceReloadGuard$initial = resourceReloadGuard$snapshot();
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    private void resourceReloadGuard$guardApply(CallbackInfo ci) {
        ReloadGuardState state = ReloadGuardState.INSTANCE;
        if (state.bypass()) return;
        PackSelectionSnapshot current = resourceReloadGuard$snapshot();
        PackSelectionDiff diff = PackDiffCalculator.diff(resourceReloadGuard$initial, current);
        if (ResourceReloadGuard.config().skipUnchangedResourcePackApply && diff.isEmpty()) {
            resourceReloadGuard$closeToParent(); ci.cancel(); return;
        }
        if (ReloadGuardClient.isBusy()) { ReloadGuardClient.toast("resource_reload_guard.message.reload_in_progress"); ci.cancel(); return; }
        ReloadDecision decision = ReloadPolicy.decide(ResourceReloadGuard.config().resourcePackPolicy,
            ResourceReloadGuard.config().allowDeferredResourcePackApply);
        if (decision == ReloadDecision.ALLOW) { state.withBypass(() -> ((PackSelectionScreen)(Object)this).onClose()); ci.cancel(); return; }
        if (decision == ReloadDecision.BLOCK) { ReloadGuardClient.toast("resource_reload_guard.message.blocked"); ci.cancel(); return; }
        if (decision == ReloadDecision.DEFER) { resourceReloadGuard$defer(current); ci.cancel(); return; }
        if (!state.tryOpenConfirmation(ReloadReason.RESOURCE_PACK_CHANGE)) { ci.cancel(); return; }
        List<Component> lines = List.of(
            Component.translatable("resource_reload_guard.screen.pack.summary.enabled", diff.enabled().size()),
            Component.translatable("resource_reload_guard.screen.pack.summary.disabled", diff.disabled().size()),
            Component.translatable("resource_reload_guard.screen.pack.summary.reordered", diff.reordered().size()),
            Component.translatable("resource_reload_guard.screen.pack.message")
        );
        Screen editing = (Screen)(Object)this;
        List<ReloadGuardScreen.Action> actions = new ArrayList<>();
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.back"), () -> ReloadGuardClient.closeTo(editing), false));
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.discard"), this::resourceReloadGuard$discardAndClose, false));
        if (ResourceReloadGuard.config().allowDeferredResourcePackApply)
            actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.apply_next_launch"), () -> { state.closeConfirmation(); resourceReloadGuard$defer(current); }, false));
        actions.add(new ReloadGuardScreen.Action(Component.translatable("resource_reload_guard.button.apply_now"), () -> { state.closeConfirmation(); state.withBypass(() -> ((PackSelectionScreen)(Object)this).onClose()); }, true));
        Minecraft.getInstance().setScreen(new ReloadGuardScreen(Component.translatable("resource_reload_guard.screen.pack.title"), editing,
            lines, actions, ResourceReloadGuard.config().dangerousButtonDelayTicks, state::closeConfirmation));
        ci.cancel();
    }

    @Unique private PackSelectionSnapshot resourceReloadGuard$snapshot() {
        PackSelectionModel model = ((PackSelectionScreenAccessor)this).resourceReloadGuard$getModel();
        return new PackSelectionSnapshot(model.getSelected().map(PackSelectionModel.Entry::getId).toList());
    }

    @Unique private void resourceReloadGuard$defer(PackSelectionSnapshot desired) {
        Minecraft minecraft = Minecraft.getInstance();
        PackSelectionModel model = ((PackSelectionScreenAccessor)this).resourceReloadGuard$getModel();
        PackRepository repository = ((PackSelectionModelAccessor)(Object)model).resourceReloadGuard$getRepository();
        List<String> sessionIds = repository.getSelectedPacks().stream().map(pack -> pack.getId()).toList();
        List<String> desiredRepositoryOrder = new ArrayList<>(desired.orderedPackIds());
        Collections.reverse(desiredRepositoryOrder);
        try {
            repository.setSelected(desiredRepositoryOrder);
            ReloadGuardState.INSTANCE.withDeferredOptionsUpdate(() -> minecraft.options.updateResourcePacks(repository));
        } finally {
            repository.setSelected(sessionIds);
        }
        resourceReloadGuard$closeToParent();
        ReloadGuardClient.toast("resource_reload_guard.message.deferred");
    }

    @Unique private void resourceReloadGuard$closeToParent() {
        ((PackSelectionScreenAccessor)this).resourceReloadGuard$closeWatcher();
        ReloadGuardState.INSTANCE.closeConfirmation();
        Minecraft.getInstance().setScreen(resourceReloadGuard$parent);
    }

    @Unique private void resourceReloadGuard$discardAndClose() {
        PackSelectionModel model = ((PackSelectionScreenAccessor)this).resourceReloadGuard$getModel();
        PackRepository repository = ((PackSelectionModelAccessor)(Object)model).resourceReloadGuard$getRepository();
        List<String> initialRepositoryOrder = new ArrayList<>(resourceReloadGuard$initial.orderedPackIds());
        Collections.reverse(initialRepositoryOrder);
        repository.setSelected(initialRepositoryOrder);
        resourceReloadGuard$closeToParent();
    }
}
