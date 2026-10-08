## Enter Embabel

**Embabel** is a JVM-based framework from Rod Johnson (Spring creator) designed to bring **discipline** to agentic AI.

Embabel gives **digital workers** a way to plan and act through Java domain types.

Core philosophy:

- **Strong typing**: domain models matter
- **OODA loops**: Observe, Orient, Decide, Act (John Boyd's decision cycle)
- **GOAP**: Goal-Oriented Action Planning (borrowed from game AI)
- **Composability**: Spring components become agent capabilities

> The JVM brings reliability, observability, and familiar production operations.

---

## OODA Loops: The Decision Cycle


<svg style="max-width: 400px; margin: 20px auto; display: block;" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="OODA decision cycle">
  <defs>
    <marker id="ooda-arrowhead" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#6db33f"/>
    </marker>
  </defs>

  <!-- Four equal nodes, positioned at the cardinal points of the circle. -->
  <g fill="#6db33f">
    <rect x="190" y="30" width="120" height="120" rx="8"/>
    <rect x="350" y="190" width="120" height="120" rx="8"/>
    <rect x="190" y="350" width="120" height="120" rx="8"/>
    <rect x="30" y="190" width="120" height="120" rx="8"/>
  </g>
  <g fill="white" font-size="17" font-weight="bold" text-anchor="middle" dominant-baseline="middle">
    <text x="250" y="90">OBSERVE</text>
    <text x="410" y="250">ORIENT</text>
    <text x="250" y="410">DECIDE</text>
    <text x="90" y="250">ACT</text>
  </g>

  <!-- Identical clockwise quarter-turns keep the cycle visually balanced. -->
  <g fill="none" stroke="#6db33f" stroke-width="3" marker-end="url(#ooda-arrowhead)">
    <path d="M 310 90 C 390 90, 410 140, 410 190"/>
    <path d="M 410 310 C 410 390, 360 410, 310 410"/>
    <path d="M 190 410 C 110 410, 90 360, 90 310"/>
    <path d="M 90 190 C 90 110, 140 90, 190 90"/>
  </g>
</svg>

Agent cycles through **Observe → Orient → Decide → Act** repeatedly until goal met.

The cycle turns `prompt → response` into **continuous reasoning with domain knowledge**.

---

## GOAP: Goal-Oriented Action Planning

Borrowed from game AI, GOAP agents work backward from goals:

### Production Example: SRE Incident Response

```
Alert: java.lang.OutOfMemoryError: Java heap space
  heap_usage=95%, gc_pause=5000ms
  ↓
Goal: "An incident report ready for human approval"
  ↓ What does the report require?
  - ServiceObservation      ✗ ← observe the live services first
  - RunbookAssessment       ✗ ← needs the observation
  - IncidentResponseReport  ✗ ← needs the assessment
  ↓ After observing:
  - Found: which services are unavailable right now
  ↓ Still required:
  - Apply the OOM runbook   ✗ ← do this next
  - Ask the model to reason inside the runbook ✗
```

The worker works backward from the goal: **"What do I need to reach it?"**

Embabel tracks subgoals, dependencies, and execution order automatically.

This requires **investigation, metrics, and domain knowledge**.

The plan can adapt as each action adds new information.
