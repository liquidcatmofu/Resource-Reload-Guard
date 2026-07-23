package dev.liquidcatmofu.resourcereloadguard.state;

import dev.liquidcatmofu.resourcereloadguard.policy.ReloadReason;
import net.minecraft.client.gui.screens.Screen;

public final class ReloadGuardState {
    public static final ReloadGuardState INSTANCE = new ReloadGuardState();
    private final ReloadCounter globalReloads = new ReloadCounter();
    private boolean confirmationOpen;
    private boolean guardReloadInProgress;
    private boolean bypass;
    private boolean deferredOptionsUpdate;
    private ReloadReason reason = ReloadReason.UNKNOWN;
    private Screen packParent;

    private ReloadGuardState() {}
    public synchronized boolean tryOpenConfirmation(ReloadReason newReason) {
        if (confirmationOpen) return false;
        confirmationOpen = true; reason = newReason; return true;
    }
    public synchronized void closeConfirmation() { confirmationOpen = false; reason = ReloadReason.UNKNOWN; }
    public synchronized boolean confirmationOpen() { return confirmationOpen; }
    public synchronized void withBypass(Runnable action) { bypass = true; try { action.run(); } finally { bypass = false; } }
    public synchronized boolean bypass() { return bypass; }
    public synchronized void withDeferredOptionsUpdate(Runnable action) { deferredOptionsUpdate = true; try { action.run(); } finally { deferredOptionsUpdate = false; } }
    public synchronized boolean deferredOptionsUpdate() { return deferredOptionsUpdate; }
    public int beginGlobalReload() { return globalReloads.begin(); }
    public void endGlobalReload() { globalReloads.end(); }
    public boolean globalReloadInProgress() { return globalReloads.active(); }
    public synchronized boolean guardReloadInProgress() { return guardReloadInProgress; }
    public synchronized void setGuardReloadInProgress(boolean value) { guardReloadInProgress = value; }
    public synchronized ReloadReason reason() { return reason; }
    public synchronized Screen packParent() { return packParent; }
    public synchronized void setPackParent(Screen screen) { packParent = screen; }
}
