package dev.liquidcatmofu.resourcereloadguard.mixin.accessor;

import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VideoSettingsScreen.class)
public interface VideoSettingsScreenAccessor {
    @Accessor("oldMipmaps") int resourceReloadGuard$getOldMipmaps();
}
