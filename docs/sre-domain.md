<!-- .slide: data-background-color="#6db33f" -->

# An SRE Digital Worker

One OutOfMemory incident, five typed actions, one human approval.

Notes:
- Now let's build a real one. An SRE digital worker.
- A digital worker is software with a goal and a set of actions, and the judgment to pick the next one.
- Its job: one OutOfMemory incident. Five actions. And a human signs off before anything changes.

---

## Digital Workers: SRE Domain

The demo worker has one goal: **produce a safe incident response for human approval**.

<div class="chain" data-on="all"></div>

Each arrow is a typed Java action: **the OODA loop, written as types**.

Notes:
- Here's the worker as a chain of types.
- IncidentRequest is the alert. ServiceObservation is what's running. RunbookAssessment is what our team allows. Then the model's response, a check on that response, and the final report.
- Look at the labels on top: observe, orient, decide, act. That's the OODA loop, written as Java types.
- The types are the contract. The planner reads them.

---

## Observe and Orient Are Spring Actions

```java
@Action(description = "Observe every Docker Compose service",
        readOnly = true)
public ServiceObservation observeServices(IncidentRequest request) {
    List<DockerComposeStatus> services = dockerComposeService.statuses();
    List<String> unavailable = services.stream()
        .filter(status -> !isRunning(status.status()))
        .map(DockerComposeStatus::service)
        .toList();
    return new ServiceObservation(
        request.incidentType(), services, unavailable);
}
```

`applyRunbook(IncidentRequest, ServiceObservation)` comes next: the output type of one action
satisfies the input type of the next.

Notes:
- Real code from the repo.
- observeServices asks Docker Compose what's actually running right now. Not a guess. Live state.
- It returns a ServiceObservation.
- And applyRunbook takes a ServiceObservation. That's the wiring. There is no wiring. The types connect themselves.

---

<!-- .slide: data-beat="3" data-takeaway="Observe: the worker reads live state from Docker Compose." -->
## Live: What Is Actually Running?

```text
embabel> docker-compose-status
SERVICE         STATUS
grafana-lgtm    Up (healthy)
ollama          Up (healthy)
redis           Up (healthy)
```

This is what the worker **observes**: live state, as typed Java objects.

Notes:
- Remember observeServices? Here's what it sees, live.
- docker-compose-status: Ollama, Redis, Grafana. Typed objects, not a screenshot.
- That's step one of the loop: observe. The worker sees exactly this.
- (If the terminal isn't showing, the slide has the rehearsed result. Keep talking.)

---

## A Goal Is an Explicit Completion Condition

```java
@Action(
    description = "Create the approval-required incident report",
    readOnly = true, actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
@AchievesGoal(description =
    "A bounded incident response is ready for human approval")
public IncidentWorkflowReport prepareReport(
        IncidentRequest request, ServiceObservation observation,
        RunbookAssessment assessment, IncidentResponseReport response,
        RunbookCheck check) {
    return new IncidentWorkflowReport(
        "Produce a safe incident response",
        request.incidentType(), observation, assessment,
        response, check, assessment.requiresApproval());
}
```

Producing this goal type completes the worker's plan.

Notes:
- How does the worker know it's done? The goal is a type.
- @AchievesGoal on prepareReport. When this returns an IncidentWorkflowReport, the job is done.
- Look at its parameters: request, observation, assessment, response, and a RunbookCheck. It can't run until all five exist.
- And look at the last argument: requiresApproval. A human signs off. Always.

---

## A Second Model Checks the First

```java
@Action(description =
        "System One check: did the LLM stay inside the runbook?",
        readOnly = true)
public RunbookCheck checkRecommendation(
        RunbookAssessment assessment,
        IncidentResponseReport response) {
    return runbookChecker.check(assessment, response);
}
```

**Jev**, TypeSafe's hosted "System One" decision model, scores how likely the
recommendation stays inside the approved strategy.

<table class="fragment">
<thead><tr><th>JEV</th><th>RUNBOOK CHECK</th></tr></thead>
<tbody>
<tr><td>answers</td><td><code>escalate</code> when the score is below 0.7</td></tr>
<tr><td>unreachable</td><td>fails closed: <code>escalate: true</code></td></tr>
</tbody>
</table>

Notes:
- Trust, but verify. A second model checks the first.
- Jev, from TypeSafe, is a small decision model. One question: did the recommendation stay inside the approved runbook?
- Score below 0.7? Escalate to a human.
- Can't reach Jev at all? Escalate anyway. It fails closed. When in doubt, a human looks.
- And because prepareReport needs a RunbookCheck, the planner can't skip this step.
- (No key = "not configured". Key exported, network up.)
