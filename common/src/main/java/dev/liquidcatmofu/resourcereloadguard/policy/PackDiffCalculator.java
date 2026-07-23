package dev.liquidcatmofu.resourcereloadguard.policy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PackDiffCalculator {
    private PackDiffCalculator() {}

    public static PackSelectionDiff diff(PackSelectionSnapshot before, PackSelectionSnapshot after) {
        List<String> oldIds = before.orderedPackIds();
        List<String> newIds = after.orderedPackIds();
        Set<String> oldSet = new HashSet<>(oldIds);
        Set<String> newSet = new HashSet<>(newIds);
        List<String> enabled = newIds.stream().filter(id -> !oldSet.contains(id)).toList();
        List<String> disabled = oldIds.stream().filter(id -> !newSet.contains(id)).toList();
        List<String> oldCommon = oldIds.stream().filter(newSet::contains).toList();
        List<String> newCommon = newIds.stream().filter(oldSet::contains).toList();
        List<String> reordered = new ArrayList<>();
        if (!oldCommon.equals(newCommon)) {
            for (int i = 0; i < newCommon.size(); i++) {
                String id = newCommon.get(i);
                if (i >= oldCommon.size() || !id.equals(oldCommon.get(i))) reordered.add(id);
            }
        }
        return new PackSelectionDiff(enabled, disabled, reordered);
    }
}
