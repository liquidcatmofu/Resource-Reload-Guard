package dev.liquidcatmofu.resourcereloadguard.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ReloadPolicyTest {
    @Test void allModes() {
        assertEquals(ReloadDecision.ALLOW, ReloadPolicy.decide(ReloadPolicyMode.ALLOW, true));
        assertEquals(ReloadDecision.CONFIRM, ReloadPolicy.decide(ReloadPolicyMode.CONFIRM, true));
        assertEquals(ReloadDecision.BLOCK, ReloadPolicy.decide(ReloadPolicyMode.BLOCK, true));
        assertEquals(ReloadDecision.DEFER, ReloadPolicy.decide(ReloadPolicyMode.RESTART_ONLY, true));
        assertEquals(ReloadDecision.BLOCK, ReloadPolicy.decide(ReloadPolicyMode.RESTART_ONLY, false));
    }
}
