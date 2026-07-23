package dev.liquidcatmofu.resourcereloadguard.mixin;

import dev.liquidcatmofu.resourcereloadguard.client.LanguageEntryDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.options.LanguageSelectScreen$LanguageSelectionList$Entry")
abstract class LanguageEntryMixin implements LanguageEntryDuck {
    @Shadow @Final String code;
    @Override public String resourceReloadGuard$getCode() { return code; }
}
