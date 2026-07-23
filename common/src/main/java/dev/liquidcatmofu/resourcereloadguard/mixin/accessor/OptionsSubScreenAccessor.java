package dev.liquidcatmofu.resourcereloadguard.mixin.accessor;

import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionsSubScreen.class)
public interface OptionsSubScreenAccessor {
    @Accessor("lastScreen") Screen resourceReloadGuard$getLastScreen();
    @Accessor("options") Options resourceReloadGuard$getOptions();
}
