<!-- .slide: data-background-color="#6db33f" -->

# From Imperative to Goal-Oriented

Name the outcome. Let the planner find the path.

Notes:
- Learning two: from imperative coding to goal-oriented orchestration.
- Imperative: you write every step, in order, and every branch.
- Goal-oriented: you name the outcome. The planner writes the steps.

---

## Start With the Outcome

The shell command supplies an input and a goal type:

```java
@Command(description =
    "Resolve an OutOfMemory incident with the bounded SRE worker")
public PlanExecution<IncidentWorkflowReport> oomIncident(
        @Option(longName = "budget", defaultValue = "8") int budget) {
    return agentRunner.run(SreIncidentWorker.NAME,
        IncidentRequest.outOfMemory(),
        IncidentWorkflowReport.class, budget);
}
```

No steps, no routing: Embabel's GOAP planner finds the path from the actions that apply.

Notes:
- Here's the shell command for the OOM incident.
- Look at what it passes: the input, IncidentRequest.outOfMemory(), and the goal type, IncidentWorkflowReport. And a budget.
- That's it. No steps. No routing.
- (Condensed: the real one goes through a pursue helper.)

---

## Running an Agent on the AgentPlatform

```java
Agent agent = agentPlatform.agents().stream()
    .filter(candidate -> candidate.getName().equals(agentName))
    .findFirst()
    .orElseThrow();

// options carry the action budget: see "The Loop Has a Hard Stop"
AgentProcess process = agentPlatform.runAgentFrom(
    agent, options, Map.of(IoBinding.DEFAULT_BINDING, input));
// the process history becomes a PlanExecution (two slides on)
```

Spring Boot auto-configures the `AgentPlatform`. Every `@Agent` bean is deployed at startup.

Notes:
- Under the hood, a small runner. About fifty lines.
- Spring Boot auto-configures the AgentPlatform. We find the agent by name and run it with our input.
- At startup you'll see it in the log: "Deployed agent sre-incident-worker."
- All the planning is Embabel. We wrote none of it.

---

## The Blackboard Is Typed Working Memory

The shared state for one agent process. Two actions in, the blackboard holds:

<div class="chain" data-on="IncidentRequest ServiceObservation RunbookAssessment"></div>

Each completed action binds another Java object: **inspectable state made from your domain types**.

Notes:
- The planner needs memory. That's the blackboard.
- It's typed working memory for one run. It starts with the input.
- Every action drops another Java object on it. Two actions in, it holds the request, the observation, and the assessment. The green boxes.
- And because it's all your types, you can inspect any of it.

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

Notes:
- So how does it pick the next action?
- Embabel reads every @Action signature. Parameters become preconditions. The return type becomes the effect.
- Here's applyRunbook: it needs a request and an observation, and it produces an assessment.
- An action is eligible only when the blackboard holds every type it needs.
- That's your router. Gone. Question two: tools coordinate without if-else.

---

## Every Execution Explains Itself

```java
List<String> actions = process.getHistory().stream()
    .map(ActionInvocation::getActionName)
    .map(name -> name.substring(name.lastIndexOf('.') + 1))
    .toList();

boolean done = process.getStatus() == AgentProcessStatusCode.COMPLETED;
return new PlanExecution<>(agentName, goalType.getSimpleName(),
    process.getStatus().name(), maxActions, actions,
    done ? process.resultOfType(goalType) : null);
```

Running `oom-incident` returns the actual plan:

<p class="outcome">COMPLETED</p>
<p class="outcome-detail">5 of 8 actions: observeServices → applyRunbook → analyzeIncident → checkRecommendation → prepareReport</p>

The trace records executed Java methods while model reasoning stays private.

Notes:
- And the fourth question: why did it do that?
- Every run returns its own history: which actions ran, the status, the budget.
- Notice the last line: you only get a result if it COMPLETED. No half-finished reports.
- On the happy path: COMPLETED, five of eight actions. Let's run it and see.

---

<!-- .slide: data-beat="4" data-takeaway="Five actions, five plans. It replans after every step." -->
## Watch the Planner Think

<pre class="planner-log"><code class="nohighlight" data-noescape>[friendly_hodgkin] <mark>formulated plan:</mark>
[friendly_hodgkin] executing action SreIncidentWorker.observeServices
[friendly_hodgkin] object bound it:ServiceObservation
[friendly_hodgkin] <mark>formulated plan:</mark>
[friendly_hodgkin] executing action SreIncidentWorker.applyRunbook
[friendly_hodgkin] object bound it:RunbookAssessment
[friendly_hodgkin] <mark>formulated plan:</mark>
[friendly_hodgkin] executing action SreIncidentWorker.analyzeIncident
  using LLM qwen2.5-coder:1.5b, creating IncidentResponseReport
[friendly_hodgkin] object bound it:IncidentResponseReport
[friendly_hodgkin] <mark>formulated plan:</mark>
[friendly_hodgkin] executing action SreIncidentWorker.checkRecommendation
[friendly_hodgkin] object bound it:RunbookCheck
[friendly_hodgkin] <mark>formulated plan:</mark>
[friendly_hodgkin] executing action SreIncidentWorker.prepareReport
[friendly_hodgkin] object bound it:IncidentWorkflowReport
[friendly_hodgkin] <mark>completed in PT9.645811S</mark></code></pre>

Observe, orient, decide, act: **the OODA loop, narrated by the framework**.

Notes:
- Now the whole worker, live. Read it out loud with me. Look for "formulated plan."
- Five actions, five plans. It plans, acts, binds a typed object, and plans again. Every single step.
- That's the OODA loop, narrated by the framework.
- One action hits the local model: about nine seconds on my laptop CPU. Jev answers in under a second. Everything else is plain Java.
- The whole run: under ten seconds.
- (Real log from a 2026-10-08 run, timestamps and the com.embabel.demo.sre package trimmed. Jev returned withinRunbook 0.91, escalate false.)

---

## Read the Workflow Result

| OUTPUT | WHAT IT TELLS US |
|---|---|
| `actions` | type-driven tool coordination |
| `status` | COMPLETED, TERMINATED, or STUCK: never an endless loop |
| `actionBudget: 8` | execution stayed inside the action budget |
| `ServiceObservation` … `RunbookCheck` | live state, team policy, model output, and its check, all typed |
| `requiresApproval: true` | autonomy stayed inside a safety boundary |

The **traceable workflow** is the artifact to remember.

Notes:
- Let's read the result like an SRE would.
- actions: what it did. status: how it ended. actionBudget: its limit.
- The typed state: what it saw, what policy said, what the model said, and the check.
- requiresApproval: who decides. That's the artifact: a traceable workflow, not a chat log.

---

## The Planner Chooses the Path

<svg class="diagram" viewBox="0 0 1080 380" role="img" aria-label="Two goals, two plans. When the goal does not need a CanaryPlan, planCanary is available but not run. When it does, the planner routes through planCanary.">
  <defs>
    <marker id="pp-arrow" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto"><path d="M0 0 L10 5 L0 10 z" fill="#5c5c5c"/></marker>
    <marker id="pp-arrow-g" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto"><path d="M0 0 L10 5 L0 10 z" fill="#3a7d1e"/></marker>
  </defs>
  <g font-family="'Work Sans', 'Open Sans', sans-serif" font-weight="700" font-size="22" fill="#111">
    <text x="0" y="28">Goal needs a RunbookCheck</text>
    <text x="0" y="218">Goal also needs a CanaryPlan</text>
  </g>
  <g font-family="'JetBrains Mono', ui-monospace, monospace" font-size="19" text-anchor="middle">
    <!-- row 1: the five-action plan; planCanary available, not run -->
    <g fill="#6db33f"><rect x="0" y="46" width="186" height="48" rx="8"/><rect x="216" y="46" width="160" height="48" rx="8"/><rect x="406" y="46" width="186" height="48" rx="8"/><rect x="622" y="46" width="232" height="48" rx="8"/><rect x="884" y="46" width="196" height="48" rx="8"/></g>
    <g fill="#111"><text x="93" y="77">observeServices</text><text x="296" y="77">applyRunbook</text><text x="499" y="77">analyzeIncident</text><text x="738" y="77">checkRecommendation</text><text x="982" y="77">prepareReport</text></g>
    <g stroke="#5c5c5c" stroke-width="3" marker-end="url(#pp-arrow)"><path d="M186 70 H212"/><path d="M376 70 H402"/><path d="M592 70 H618"/><path d="M854 70 H880"/></g>
    <rect x="406" y="124" width="186" height="44" rx="8" fill="none" stroke="#5c5c5c" stroke-width="2" stroke-dasharray="8 6"/>
    <text x="499" y="153" fill="#5c5c5c">planCanary</text>
    <text x="622" y="153" fill="#4a4a4a" text-anchor="start" font-family="'Open Sans', sans-serif" font-size="20">available, not needed: not run</text>
    <!-- row 2: the planner routes through planCanary -->
    <g fill="#6db33f"><rect x="0" y="236" width="186" height="48" rx="8"/><rect x="216" y="236" width="160" height="48" rx="8"/><rect x="406" y="236" width="186" height="48" rx="8"/><rect x="622" y="236" width="232" height="48" rx="8"/><rect x="884" y="236" width="196" height="48" rx="8"/><rect x="406" y="314" width="186" height="48" rx="8"/></g>
    <g fill="#111"><text x="93" y="267">observeServices</text><text x="296" y="267">applyRunbook</text><text x="499" y="267">analyzeIncident</text><text x="738" y="267">checkRecommendation</text><text x="982" y="267">prepareReport</text><text x="499" y="345">planCanary</text></g>
    <g stroke="#5c5c5c" stroke-width="3" marker-end="url(#pp-arrow)"><path d="M186 260 H212"/><path d="M376 260 H402"/><path d="M592 260 H618"/><path d="M854 260 H880"/></g>
    <g fill="none" stroke="#3a7d1e" stroke-width="3" marker-end="url(#pp-arrow-g)"><path d="M296 284 V338 H402"/><path d="M592 338 H982 V288"/></g>
  </g>
</svg>

Same six actions available. **The goal decides which ones run**, and no router changes.

Notes:
- Now the question every senior dev asks me: "why not just call five methods in order?"
- This is why. Same six actions available, including planCanary.
- Top row: the goal doesn't need a CanaryPlan. planCanary is right there, and the planner skips it.
- Bottom row: the goal needs a CanaryPlan. The planner routes through planCanary.
- Same code. Different goal. Different plan. That's the difference between a script and a planner.
- (Verified 2026-10-16: with planCanary in the worker and no CanaryPlan in the goal, the run is still five actions.)

---

## The Loop Has a Hard Stop

```java
public static final int MAX_ACTIONS = 8;

Budget budget = Budget.DEFAULT.withActions(maxActions);
ProcessOptions options = ProcessOptions.DEFAULT
    .withBudget(budget)
    .withAdditionalEarlyTerminationPolicy(
        budget.earlyTerminationPolicy());
```

```java
@Agent(name = SreIncidentWorker.NAME,
       description = "...",
       actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
```

<ul>
<li class="fragment">each action runs once unless its annotation enables reruns</li>
<li class="fragment">the <code>Budget</code> caps the process at eight actions</li>
<li class="fragment">no path to the goal ends as <code>STUCK</code>, not in a loop</li>
</ul>

Notes:
- Question one: how do we stop it looping forever?
- Look at the first block: a Budget of eight actions. The budget caps cost and tokens too.
- Second block: FIRE_ONCE. Each action runs once.
- And if there's no path to the goal at all? The process ends STUCK. It doesn't spin.
- Three hard stops. The budget, live, right now. STUCK comes at the end.
- (1.5.3 gotcha: add the budget's termination policy too.)

---

<!-- .slide: data-beat="5" data-takeaway="A budget of two: TERMINATED. It cannot loop forever." -->
## Live: Bound the Worker

```text
oom-incident --budget 2
```

<p class="outcome">TERMINATED</p>
<p class="outcome-detail">after 2 actions: observeServices, applyRunbook</p>

```text
early termination by MaxActionsEarlyTerminationPolicy(maxActions=2)
```

The budget answers the first question from the abstract: **the worker cannot loop forever**.

Notes:
- Remember five of eight? Let's take the budget away. Budget of two.
- It runs observeServices, applyRunbook, and stops. TERMINATED.
- Embabel enforced that, not my shell code.
- Question one, live: it cannot loop forever.
- (If it fails: the slide shows the TERMINATED output.)
