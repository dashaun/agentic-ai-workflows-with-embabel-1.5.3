package com.embabel.demo.shell;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.core.ActionRetryPolicy;
import com.embabel.demo.domain.ConstrainedRemediationPlan;
import com.embabel.demo.domain.StoredCodeAnalyses;
import com.embabel.demo.domain.StoredCodeAnalysis;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@Agent(
        name = SafeRemediationAgent.NAME,
        description = "Find a low-risk code fix without changing source code",
        actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
public class SafeRemediationAgent {

    public static final String NAME = "safe-remediation-planner";

    @Action(
            description = "Recommend one low-risk fix from the stored code analyses",
            readOnly = true,
            canRerun = false,
            actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
    @AchievesGoal(description = "Produce a single approval-required, low-risk remediation plan")
    public ConstrainedRemediationPlan recommendQuickWin(StoredCodeAnalyses stored) {
        return stored.analyses().stream()
                .filter(entry -> !entry.analysis().issues().isEmpty())
                .min(Comparator
                        .comparingInt((StoredCodeAnalysis entry) -> entry.analysis().issues().size())
                        .thenComparing(entry -> entry.analysis().qualityScore(), Comparator.reverseOrder()))
                .map(entry -> new ConstrainedRemediationPlan(
                        entry.sourceId(),
                        entry.analysis().issues().getFirst(),
                        "Prepare a small patch and present it for human approval.",
                        true))
                .orElse(new ConstrainedRemediationPlan(
                        "none",
                        "No open issues are stored in Redis.",
                        "No change is recommended.",
                        true));
    }
}
