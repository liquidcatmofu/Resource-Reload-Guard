package dev.liquidcatmofu.resourcereloadguard.state;

import java.util.concurrent.atomic.AtomicInteger;

public final class ReloadCounter {
    private final AtomicInteger count = new AtomicInteger();
    public int begin() { return count.incrementAndGet(); }
    public int end() { return count.updateAndGet(value -> Math.max(0, value - 1)); }
    public int count() { return count.get(); }
    public boolean active() { return count() > 0; }
}
