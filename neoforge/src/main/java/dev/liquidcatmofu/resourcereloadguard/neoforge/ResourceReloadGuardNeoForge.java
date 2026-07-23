package dev.liquidcatmofu.resourcereloadguard.neoforge;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value = ResourceReloadGuard.MOD_ID, dist = Dist.CLIENT)
public final class ResourceReloadGuardNeoForge {
    public ResourceReloadGuardNeoForge() {
        int count;
        try {
            count = ModList.get().size();
        } catch (RuntimeException exception) {
            count = -1;
        }
        ResourceReloadGuard.initialize(FMLPaths.CONFIGDIR.get(), count);
    }
}
