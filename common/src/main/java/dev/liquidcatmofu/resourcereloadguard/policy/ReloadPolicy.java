package dev.liquidcatmofu.resourcereloadguard.policy;

public final class ReloadPolicy {
    private ReloadPolicy() {}

    public static ReloadDecision decide(ReloadPolicyMode mode, boolean deferralAvailable) {
        return switch (mode) {
            case ALLOW -> ReloadDecision.ALLOW;
            case CONFIRM -> ReloadDecision.CONFIRM;
            case BLOCK -> ReloadDecision.BLOCK;
            case RESTART_ONLY -> deferralAvailable ? ReloadDecision.DEFER : ReloadDecision.BLOCK;
        };
    }
}
