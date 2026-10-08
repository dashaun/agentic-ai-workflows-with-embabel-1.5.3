## Live Demo: One Compose Project, Two Workers

```text
docker-compose-status
```

Observe `ollama`, `redis`, and `grafana-lgtm` as typed JSON.

```text
oom-incident
```

Watch the SRE worker plan, act, and replan, then return an approval-required
`IncidentWorkflowReport`.

Notes:
- Start with dad-joke earlier to warm the model.
- Terminal font large enough for the back row before starting.
- If time is short, run oom-incident and the three live steps only.

---

## Watch the Planner Think

Read the log out loud before you look at the JSON:

```text
[vigorous_dubinsky] formulated plan:
[vigorous_dubinsky] executing action SreIncidentWorker.observeServices
[vigorous_dubinsky] object bound it:ServiceObservation
[vigorous_dubinsky] formulated plan:
[vigorous_dubinsky] executing action SreIncidentWorker.applyRunbook
[vigorous_dubinsky] object bound it:RunbookAssessment
[vigorous_dubinsky] executing action SreIncidentWorker.analyzeIncident
  using LLM qwen2.5-coder:1.5b, creating IncidentResponseReport
[vigorous_dubinsky] executing action SreIncidentWorker.prepareReport
[vigorous_dubinsky] completed
```

Observe, orient, decide, act: **the OODA loop, narrated by the framework**.

Notes:
- Point at "formulated plan" after each action: Embabel replans from the new world state every time.
- Only one of the four actions calls the model.

---

## Live: Bound the Worker

```text
oom-incident --budget 2
```

```text
early termination by MaxActionsEarlyTerminationPolicy(maxActions=2)
```

```json
{
  "status" : "TERMINATED",
  "actionBudget" : 2,
  "actions" : [ "observeServices", "applyRunbook" ]
}
```

The budget answers the first question from the abstract: **the worker cannot loop forever**.

Notes:
- The budget is enforced by Embabel, not by the shell command.
- Cost and token budgets work the same way.

---

## Live: Ask for the Impossible

Require a `CanaryPlan` before the report, but give the worker no way to make one:

```java
public record CanaryPlan(String change, int trafficPercent, boolean requiresApproval) {}

public IncidentWorkflowReport prepareReport(IncidentRequest request,
        ServiceObservation observation, RunbookAssessment assessment,
        IncidentResponseReport response,
        CanaryPlan canary) { ... }
```

```json
{ "status" : "STUCK", "actions" : [ ] }
```

Embabel plans **before** it acts: no path to the goal means zero actions and zero LLM calls.

Notes:
- Restart the shell after the edit (./mvnw spring-boot:run).
- The planner log names the unsatisfied condition: it:CanaryPlan.
- The imperative version of this bug is the infinite loop.

---

## Live: Add an Action, Change the Plan

```java
@Action(description = "Plan a canary to validate the remediation", readOnly = true)
public CanaryPlan planCanary(RunbookAssessment assessment) {
    return new CanaryPlan(assessment.recommendedAction(), 5, true);
}
```

```json
"actions" : [ "observeServices", "applyRunbook", "analyzeIncident",
              "planCanary", "prepareReport" ]
```

No router changed. **The new type created a new path to the goal.**

Notes:
- This is "types replace the if-else router" proven live.
- Full script with every snippet: DEMO.md in the repo.

---

## Switch Domains

```text
stored-analyses
safe-quick-win
```

The coding worker reads Redis and chooses a bounded remediation plan with the same runner.

---

## Read the Workflow Result

| OUTPUT | WHAT IT TELLS US |
|---|---|
| `actions` | type-driven tool coordination |
| `status` | COMPLETED, TERMINATED, or STUCK: never an endless loop |
| `actionBudget: 8` | execution stayed inside the action budget |
| `ServiceObservation` | the worker used live Docker state |
| `RunbookAssessment` | team policy remained deterministic Java |
| `IncidentResponseReport` | the model returned a domain object |
| `requiresApproval: true` | autonomy stayed inside a safety boundary |

The **traceable workflow** is the artifact to remember.

---

## The Same Pattern Crosses Domains

```java
// SRE: live state + runbook + model
PlanExecution<IncidentWorkflowReport> incident =
    agentRunner.run(SreIncidentWorker.NAME,
        IncidentRequest.outOfMemory(),
        IncidentWorkflowReport.class);

// Coding: Redis state + constraints
PlanExecution<ConstrainedRemediationPlan> quickWin =
    agentRunner.run(SafeRemediationAgent.NAME,
        new StoredCodeAnalyses(codeAnalysisRepository.findAll()),
        ConstrainedRemediationPlan.class);
```

| PATTERN | SRE WORKER | CODING WORKER |
|---|---|---|
| Typed input | `IncidentRequest` | `StoredCodeAnalyses` |
| Existing tool | Docker Compose | Redis repository |
| Domain rule | `SreRunbook` | quick-win comparator |
| Safe output | approval-required report | approval-required patch plan |

Spring Beans are the reusable action surface; domain types hold the workflow together.
