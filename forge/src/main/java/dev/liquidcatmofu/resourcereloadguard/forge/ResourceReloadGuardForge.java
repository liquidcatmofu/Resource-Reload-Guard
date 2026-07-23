package dev.liquidcatmofu.resourcereloadguard.forge;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(ResourceReloadGuard.MOD_ID)
public final class ResourceReloadGuardForge {
    public ResourceReloadGuardForge() {
        int count;
        try { count = ModList.get().size(); } catch (RuntimeException exception) { count = -1; }
        ResourceReloadGuard.initialize(FMLPaths.CONFIGDIR.get(), count);
    }
}
