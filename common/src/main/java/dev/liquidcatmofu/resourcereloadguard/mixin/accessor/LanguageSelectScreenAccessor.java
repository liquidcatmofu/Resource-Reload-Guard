package dev.liquidcatmofu.resourcereloadguard.mixin.accessor;

import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.resources.language.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LanguageSelectScreen.class)
public interface LanguageSelectScreenAccessor {
    @Accessor("languageManager") LanguageManager resourceReloadGuard$getLanguageManager();
    @Invoker("onDone") void resourceReloadGuard$invokeOnDone();
}
