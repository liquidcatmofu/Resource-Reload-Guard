package dev.liquidcatmofu.resourcereloadguard.mixin;

import dev.liquidcatmofu.resourcereloadguard.state.ReloadGuardState;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Options.class)
abstract class OptionsMixin {
    @Redirect(method = "updateResourcePacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> resourceReloadGuard$deferReload(Minecraft minecraft) {
        if (ReloadGuardState.INSTANCE.deferredOptionsUpdate()) return CompletableFuture.completedFuture(null);
        return minecraft.reloadResourcePacks();
    }
}
