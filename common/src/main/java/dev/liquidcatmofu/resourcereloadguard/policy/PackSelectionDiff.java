package dev.liquidcatmofu.resourcereloadguard.policy;

import java.util.List;

public record PackSelectionDiff(List<String> enabled, List<String> disabled, List<String> reordered) {
    public PackSelectionDiff {
        enabled = List.copyOf(enabled); disabled = List.copyOf(disabled); reordered = List.copyOf(reordered);
    }
    public boolean isEmpty() { return enabled.isEmpty() && disabled.isEmpty() && reordered.isEmpty(); }
}
