package dev.liquidcatmofu.resourcereloadguard.policy;

import java.util.List;

public record PackSelectionSnapshot(List<String> orderedPackIds) {
    public PackSelectionSnapshot { orderedPackIds = List.copyOf(orderedPackIds); }
}
