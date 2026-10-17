<!-- .slide: data-background-color="#6db33f" -->

# Why the JVM

Everything you just saw was plain Spring and Java.

Notes:
- Learning four: why the JVM is the best place to run mission-critical agents.
- Hands up: who runs Spring in production? Keep them up. Look around.
- Everything you just saw was beans, records, tests, and a budget.
- Mission-critical means it runs at 2 a.m., under load, with an audit trail. That's the JVM's day job.

---

## Advantage 1: Types Are the Plan

```text
prepareReport(..., CanaryPlan canary)          →  STUCK
planCanary(RunbookAssessment) → CanaryPlan     →  new plan
```

The compiler checks every signature. The planner checks that a path exists, before it spends
a single LLM call.

Notes:
- First: types are the plan.
- javac checks the types. The planner checks the path.
- Two safety nets, and both run before you spend money.
- Let me prove it. I'll make the report need a CanaryPlan that nothing can produce. It will compile fine. Watch what Embabel does.

---

<!-- .slide: data-beat="7" data-takeaway="No path to the goal: zero actions, zero LLM calls. STUCK." -->
## Live: Ask for the Impossible

Require a `CanaryPlan` before the report, but give the worker no way to make one:

```java
public record CanaryPlan(
    String change, int trafficPercent, boolean requiresApproval) {}

public IncidentWorkflowReport prepareReport(IncidentRequest request,
        ServiceObservation observation, RunbookAssessment assessment,
        IncidentResponseReport response, RunbookCheck check,
        CanaryPlan canary) { ... }
```

<p class="outcome">STUCK</p>
<p class="outcome-detail">0 actions · 0 LLM calls</p>

Embabel plans **before** it acts: with no path to the goal, nothing runs.

Notes:
- Breaking it on purpose. Watch the diff: prepareReport now needs a CanaryPlan, and nothing can make one.
- Recompile, run it again. This is all one click.
- STUCK. Zero actions. Zero LLM calls.
- It planned before it spent anything. It saw there was no path and stopped.
- The imperative version of this bug? An infinite loop, and a cloud bill.
- (The click applies demo/patches/01 and runs javac. If it fails: press L, the slide shows STUCK.)

---

<!-- .slide: data-beat="8" data-takeaway="One new @Action, a new plan. No router." -->
## Live: Add an Action, Change the Plan

```java
@Action(description = "Plan a canary to validate the remediation",
        readOnly = true)
public CanaryPlan planCanary(RunbookAssessment assessment) {
    return new CanaryPlan(assessment.recommendedAction(), 5, true);
}
```

```json
"actions" : [ "observeServices", "applyRunbook", "analyzeIncident",
              "checkRecommendation", "planCanary", "prepareReport" ]
```

No router changed. **The new type created a new path to the goal.**

Notes:
- The fix is one new @Action: planCanary. Takes an assessment, returns a CanaryPlan. That's the green in the diff.
- Recompile, run it.
- New plan. Six actions. I didn't touch a router. There isn't one.
- Question two, live: a new tool joined the workflow without a single if-else.
- Read the plan lines: planCanary slots in after the check, because nothing before it needed one.
- (The click applies demo/patches/02. Both patches are reverted when the demo ends.)

---

## Advantage 2: Operations You Already Run

Nothing in the demo needed a new platform:

| YOU SAW | ON THE JVM |
|---|---|
| `java.lang.OutOfMemoryError`, `heap_usage`, `gc_pause` | JVM telemetry your SRE team already reads |
| `AgentRunnerTest` asserts TERMINATED and STUCK | plain JUnit, no model required |
| `grafana-lgtm` in `compose.yml` | Spring Boot's OpenTelemetry starter |
| `CodeAnalysisRepository` in Redis | Spring Data, unchanged |

Notes:
- Second: nothing new to run. Look at the left column, you saw all of it.
- OutOfMemoryError, heap usage, GC pause: JVM telemetry your SRE team already reads.
- AgentRunnerTest checks TERMINATED and STUCK with plain JUnit. No model needed.
- OpenTelemetry into Grafana. Spring Data into Redis.
- The Python route means a new runtime, new deploys, new monitoring. Here, you already operate all of it. Hands down.

---

## Advantage 3: Constraints That Matter

The annotations declare the action's bounds to Embabel:

```java
@Action(
    description =
        "Recommend one low-risk fix from the stored code analyses",
    readOnly = true,
    canRerun = false,
    actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
@AchievesGoal(description =
    "Produce a single approval-required, low-risk remediation plan")
public ConstrainedRemediationPlan recommendQuickWin(
        StoredCodeAnalyses stored) {
    // Select one issue; return a plan for human approval.
}
```

One attempt, no reruns. The human-approval flag is set in Java, not left to the model.

Notes:
- Third: constraints you can read in code review.
- One attempt. No reruns. And the approval flag is set in Java, never by the model.
- You saw it hold in safe-quick-win.
- Your security team can review this like any other pull request.

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

<table class="fragment">
<thead><tr><th>PATTERN</th><th>SRE WORKER</th><th>CODING WORKER</th></tr></thead>
<tbody>
<tr><td>Typed input</td><td><code>IncidentRequest</code></td><td><code>StoredCodeAnalyses</code></td></tr>
<tr><td>Existing tool</td><td>Docker Compose</td><td>Redis repository</td></tr>
<tr><td>Domain rule</td><td><code>SreRunbook</code></td><td>quick-win comparator</td></tr>
<tr><td>Safe output</td><td>approval-required report</td><td>approval-required patch plan</td></tr>
</tbody>
</table>

Notes:
- Two domains. One pattern.
- Look at the two calls: same agentRunner.run, different input and goal type.
- Typed input. An existing tool. A domain rule. A safe output.
- Beans are the actions. Types hold it together.
