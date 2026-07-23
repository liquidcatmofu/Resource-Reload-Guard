package dev.liquidcatmofu.resourcereloadguard.mixin.accessor;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PackSelectionScreen.class)
public interface PackSelectionScreenAccessor {
    @Accessor("model") PackSelectionModel resourceReloadGuard$getModel();
    @Invoker("closeWatcher") void resourceReloadGuard$closeWatcher();
}
