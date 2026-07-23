package dev.liquidcatmofu.resourcereloadguard.state;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ReloadCounterTest {
    @Test void overlappingReloadsReleaseIndependentlyAndNeverUnderflow() {
        ReloadCounter counter = new ReloadCounter();
        counter.begin(); counter.begin();
        assertTrue(counter.active());
        assertEquals(1, counter.end());
        assertEquals(0, counter.end());
        assertEquals(0, counter.end());
        assertFalse(counter.active());
    }
}
