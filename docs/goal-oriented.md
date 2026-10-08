## Start With the Outcome

The shell command supplies an input and a goal type:

```java
@Command(description =
    "Resolve an OutOfMemory incident with the bounded SRE worker")
public PlanExecution<IncidentWorkflowReport> oomIncident() {
    return agentRunner.run(
        SreIncidentWorker.NAME,
        IncidentRequest.outOfMemory(),
        IncidentWorkflowReport.class);
}
```

The command supplies the input and desired output type. Embabel's GOAP planner discovers a
path from the actions that are currently applicable.

Notes:
- This is the imperative-to-goal-oriented shift from the abstract.
- The command names the desired output type.

---

## Running an Agent on the AgentPlatform

```java
Agent agent = agentPlatform.agents().stream()
    .filter(candidate -> candidate.getName().equals(agentName))
    .findFirst()
    .orElseThrow();

ProcessOptions options = ProcessOptions.DEFAULT
    .withBudget(budget)
    .withAdditionalEarlyTerminationPolicy(budget.earlyTerminationPolicy());

AgentProcess process = agentPlatform.runAgentFrom(
    agent, options, Map.of(IoBinding.DEFAULT_BINDING, input));

return process.resultOfType(goalType);
```

Spring Boot auto-configures the `AgentPlatform`. Every `@Agent` bean is deployed at startup.

Notes:
- The startup log shows "Deployed agent sre-incident-worker".
- AgentRunner is about 50 lines; the planning is all Embabel.

---

## The Blackboard Is Typed Working Memory

The blackboard is the shared state for one agent process. It starts with the input, then each
completed action binds another Java object:

```text
[vigorous_dubinsky] executing action SreIncidentWorker.observeServices
[vigorous_dubinsky] object bound it:ServiceObservation
[vigorous_dubinsky] executing action SreIncidentWorker.applyRunbook
[vigorous_dubinsky] object bound it:RunbookAssessment
```

```text
Start     IncidentRequest
Observe   + ServiceObservation
Orient    + RunbookAssessment
Decide    + IncidentResponseReport
Act       + IncidentWorkflowReport  ← goal achieved
```

It is inspectable application state made from your domain types.

Notes:
- These lines come straight from the Embabel log during the demo.
- Embabel replans after every action from the current world state.

---

## Types Replace the If-Else Router

Embabel reads each `@Action` signature and derives conditions from the types:

```text
action: SreIncidentWorker.applyRunbook
  preconditions:
    it:IncidentRequest: TRUE
    it:ServiceObservation: TRUE
    it:RunbookAssessment: FALSE
  postconditions:
    it:RunbookAssessment: TRUE
```

An action is eligible only when the blackboard holds objects matching all of its parameter
types. The planner searches for the cheapest path to the goal's preconditions.

```text
IncidentRequest → ServiceObservation → RunbookAssessment
                → IncidentResponseReport → IncidentWorkflowReport
```

Notes:
- No routing code: adding an action with new types changes the plans Embabel can find.
- Embabel uses actions, conditions, blackboard, goals, and replanning.

---

## The Loop Has a Hard Stop

```java
public static final int MAX_ACTIONS = 8;

Budget budget = Budget.DEFAULT.withActions(maxActions);
ProcessOptions options = ProcessOptions.DEFAULT
    .withBudget(budget)
    .withAdditionalEarlyTerminationPolicy(budget.earlyTerminationPolicy());
```

```java
@Agent(name = SreIncidentWorker.NAME,
       description = "...",
       actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
```

Guardrails that prevent runaway behavior:

- each action runs once unless its annotation enables reruns;
- the `Budget` caps the agent process at eight actions (cost and tokens are capped too);
- a process with no path to the goal ends as `STUCK`, not in a loop.

Notes:
- In Embabel 1.5.3, withBudget alone does not change the enforced policy; adding the budget's policy does.
- We prove both outcomes live in the demo: TERMINATED and STUCK.

---

## Every Execution Explains Itself

```java
List<String> actions = process.getHistory().stream()
    .map(ActionInvocation::getActionName)
    .toList();

return new PlanExecution<>(agentName, goalType.getSimpleName(),
    process.getStatus().name(), MAX_ACTIONS, actions,
    process.resultOfType(goalType));
```

Running `oom-incident` returns the actual plan:

```json
{
  "agent" : "sre-incident-worker",
  "goalType" : "IncidentWorkflowReport",
  "status" : "COMPLETED",
  "actionBudget" : 8,
  "actions" : [
    "observeServices", "applyRunbook",
    "analyzeIncident", "prepareReport"
  ]
}
```

The trace records executed Java methods while model reasoning stays private.

Notes:
- The result also contains the observation, runbook assessment, and typed response.
- This gives the operations team an audit trail built from the agent process history.
