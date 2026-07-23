package dev.liquidcatmofu.resourcereloadguard.fabric;

import dev.liquidcatmofu.resourcereloadguard.ResourceReloadGuard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class ResourceReloadGuardFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader loader = FabricLoader.getInstance();
        ResourceReloadGuard.initialize(loader.getConfigDir(), loader.getAllMods().size());
    }
}
