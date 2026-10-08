package com.embabel.demo.agent;

import com.embabel.agent.core.ActionInvocation;
import com.embabel.agent.core.Agent;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.core.Budget;
import com.embabel.agent.core.IoBinding;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.demo.domain.PlanExecution;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Runs a deployed Embabel agent on the AgentPlatform and reports the plan it executed. */
@Component
public class AgentRunner {

    public static final int MAX_ACTIONS = 8;

    private final AgentPlatform agentPlatform;

    public AgentRunner(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    public <T> PlanExecution<T> run(String agentName, Object input, Class<T> goalType) {
        return run(agentName, input, goalType, MAX_ACTIONS);
    }

    public <T> PlanExecution<T> run(String agentName, Object input, Class<T> goalType, int maxActions) {
        Agent agent = agentPlatform.agents().stream()
                .filter(candidate -> candidate.getName().equals(agentName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No deployed agent named " + agentName));

        // The budget caps the whole process: the planner may not run more than maxActions actions.
        // In Embabel 1.5.3, withBudget alone does not change the enforced policy, so apply it explicitly.
        Budget budget = Budget.DEFAULT.withActions(maxActions);
        ProcessOptions options = ProcessOptions.DEFAULT
                .withBudget(budget)
                .withAdditionalEarlyTerminationPolicy(budget.earlyTerminationPolicy());

        AgentProcess process = agentPlatform.runAgentFrom(
                agent, options, Map.of(IoBinding.DEFAULT_BINDING, input));

        List<String> actions = process.getHistory().stream()
                .map(ActionInvocation::getActionName)
                .map(name -> name.substring(name.lastIndexOf('.') + 1))
                .toList();

        return new PlanExecution<>(
                agentName,
                goalType.getSimpleName(),
                process.getStatus().name(),
                maxActions,
                actions,
                process.getStatus() == AgentProcessStatusCode.COMPLETED ? process.resultOfType(goalType) : null);
    }
}
