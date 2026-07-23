package dev.liquidcatmofu.resourcereloadguard.mixin;

import dev.liquidcatmofu.resourcereloadguard.client.ReloadGuardClient;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void resourceReloadGuard$interceptDebugReload(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_T) {
            ReloadGuardClient.requestDebugReload();
            cir.setReturnValue(true);
        }
    }
}
