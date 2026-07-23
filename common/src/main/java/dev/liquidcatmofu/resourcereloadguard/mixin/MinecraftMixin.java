package dev.liquidcatmofu.resourcereloadguard.mixin;

import dev.liquidcatmofu.resourcereloadguard.client.MipmapReloadGuard;
import dev.liquidcatmofu.resourcereloadguard.state.ReloadGuardState;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void resourceReloadGuard$guardMipmapExit(Screen target, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft)(Object)this;
        if (MipmapReloadGuard.intercept(minecraft, minecraft.screen, target)) ci.cancel();
    }

    @Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), require = 0)
    private void resourceReloadGuard$observeStart(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ReloadGuardState.INSTANCE.beginGlobalReload();
    }

    @Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"), require = 0)
    private void resourceReloadGuard$observeEnd(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        CompletableFuture<Void> future = cir.getReturnValue();
        if (future == null) ReloadGuardState.INSTANCE.endGlobalReload();
        else future.whenComplete((ignored, error) -> ReloadGuardState.INSTANCE.endGlobalReload());
    }
}
