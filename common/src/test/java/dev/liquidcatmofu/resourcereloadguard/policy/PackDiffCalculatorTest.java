package dev.liquidcatmofu.resourcereloadguard.policy;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class PackDiffCalculatorTest {
    private PackSelectionDiff diff(List<String> before, List<String> after) {
        return PackDiffCalculator.diff(new PackSelectionSnapshot(before), new PackSelectionSnapshot(after));
    }
    @Test void identicalIsEmpty() { assertTrue(diff(List.of("a", "b"), List.of("a", "b")).isEmpty()); }
    @Test void detectsAddition() { assertEquals(List.of("c"), diff(List.of("a"), List.of("a", "c")).enabled()); }
    @Test void detectsRemoval() { assertEquals(List.of("b"), diff(List.of("a", "b"), List.of("a")).disabled()); }
    @Test void detectsReorder() { assertFalse(diff(List.of("a", "b"), List.of("b", "a")).reordered().isEmpty()); }
    @Test void additionAloneIsNotReorder() { assertTrue(diff(List.of("a", "b"), List.of("a", "c", "b")).reordered().isEmpty()); }
    @Test void detectsAdditionAndReorderTogether() {
        PackSelectionDiff diff = diff(List.of("a", "b"), List.of("b", "c", "a"));
        assertEquals(List.of("c"), diff.enabled());
        assertFalse(diff.reordered().isEmpty());
    }
}
