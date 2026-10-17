<!-- .slide: data-background-color="#6db33f" -->

# Enter Embabel

Strong types, OODA loops, and goal-oriented planning on the JVM.

Notes:
- Enter Embabel. This is the framework that checks every box on that list.

---

## What Embabel Is

**Embabel** is a JVM-based framework from Rod Johnson (Spring creator) designed to bring **discipline** to agentic AI.

Core philosophy:

- **Strong typing**: domain models matter
- **OODA loops**: Observe, Orient, Decide, Act (John Boyd's decision cycle)
- **GOAP**: Goal-Oriented Action Planning (borrowed from game AI)
- **Composability**: Spring components become agent capabilities

Notes:
- Embabel comes from Rod Johnson. Yes, the Spring guy.
- Most agent frameworks are Python-first. Embabel is built for the JVM and for people like us: strongly typed, Spring-native.
- Four ideas, and they're the whole talk.
- Strong typing: your domain model is the contract.
- OODA: a decision loop. GOAP: a planner. Composability: Spring components become capabilities.
- I'll make the JVM case at the end, after you've seen it run.

---

## OODA Loops: The Decision Cycle


<svg style="max-width: 540px; margin: 0 auto; display: block;" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="OODA decision cycle">
  <defs>
    <marker id="ooda-arrowhead" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#6db33f"/>
    </marker>
  </defs>

  <!-- Four equal nodes, positioned at the cardinal points of the circle. -->
  <g fill="#6db33f">
    <rect x="180" y="20" width="140" height="140" rx="8"/>
    <rect x="340" y="180" width="140" height="140" rx="8"/>
    <rect x="180" y="340" width="140" height="140" rx="8"/>
    <rect x="20" y="180" width="140" height="140" rx="8"/>
  </g>
  <g fill="#111" font-family="'Work Sans', 'Open Sans', sans-serif" font-size="26" font-weight="700" text-anchor="middle" dominant-baseline="middle">
    <text x="250" y="90">OBSERVE</text>
    <text x="410" y="250">ORIENT</text>
    <text x="250" y="410">DECIDE</text>
    <text x="90" y="250">ACT</text>
  </g>

  <!-- Identical clockwise quarter-turns keep the cycle visually balanced. -->
  <g fill="none" stroke="#6db33f" stroke-width="3" marker-end="url(#ooda-arrowhead)">
    <path d="M 320 90 C 390 90, 410 120, 410 176"/>
    <path d="M 410 320 C 410 390, 380 410, 324 410"/>
    <path d="M 180 410 C 110 410, 90 380, 90 324"/>
    <path d="M 90 180 C 90 110, 120 90, 176 90"/>
  </g>
</svg>

The worker repeats **Observe → Orient → Decide → Act** until the goal is met.

Notes:
- OODA comes from John Boyd, a fighter pilot. Observe, orient, decide, act. Repeat.
- Why a loop and not a chain? A chain runs once and hopes. A loop looks at the world again after every move.
- For an agent: observe the system, orient with what you know, decide what to do, act. Then check again.
- It runs until the goal is met, or until something tells it to stop.

---

## GOAP: Work Backward from the Goal

Borrowed from game AI. For the OutOfMemory incident, the planner asks **"What do I need?"**

```text
Goal  IncidentWorkflowReport              ✗
        needs RunbookCheck                ✗
          needs IncidentResponseReport    ✗
            needs RunbookAssessment       ✗
              needs ServiceObservation    ✗
                needs IncidentRequest     ✓  ← the OOM alert
```

Then it runs the plan forward:

```text
observeServices → applyRunbook → analyzeIncident
                → checkRecommendation → prepareReport
```

Notes:
- GOAP: Goal-Oriented Action Planning. It comes from game AI; it's how game characters figure out what to do.
- Every action has preconditions, what it needs, and effects, what it produces.
- The planner starts at the goal and works backward. Look at the tree:
- The report needs a check. The check needs a response. The response needs an assessment. Down to the one thing we have: the OOM alert.
- Then it runs the plan forward, and replans after every step in case the world changed.
- We never wrote that order. The planner found it.
