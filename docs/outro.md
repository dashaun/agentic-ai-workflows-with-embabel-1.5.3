<!-- .slide: data-background-color="#6db33f" -->

# Disciplined Workflows

The worker can choose the path. Your code still defines the world.

Notes:
- Let's bring it home. We started with three questions.
- Loops? A budget, FIRE_ONCE, and STUCK.
- If-else? Types and a planner.
- Domain models? At the heart of the reasoning.

---

## Digital Workers Need Disciplined Workflows

```text
Goal
  → Embabel GOAP planner selects an applicable typed action
  → Spring Bean reads a real system
  → Java runbook supplies domain context
  → model returns a domain object; a second model checks it
  → bounded execution stops at the goal
```

The production advantage is **disciplined AI working through your existing Java system**.

Notes:
- Here's the whole thing in one block.
- A goal. A planner picks an action. Your beans touch real systems. Your runbook sets the rules. The model returns a type, and a second model checks it. And it stops.
- That's disciplined AI. Remember the word from the start? That's what it means.

---

## The Moment to Remember

<div class="before-after">
  <div class="state state-stuck">
    <p class="state-label">No action makes a <code>CanaryPlan</code></p>
    <p class="outcome">STUCK</p>
    <p class="outcome-detail">0 actions<br>0 LLM calls</p>
  </div>
  <div class="state-change">
    <code>+ @Action planCanary</code>
    <svg viewBox="0 0 120 40" aria-hidden="true"><path d="M4 20 H104" stroke="currentColor" stroke-width="5"/><path d="M96 8 L116 20 L96 32 z" fill="currentColor"/></svg>
  </div>
  <div class="state state-planned">
    <p class="state-label">One new typed action</p>
    <p class="outcome">New plan</p>
    <p class="outcome-detail">6 actions<br>0 router changes</p>
  </div>
</div>

Embabel plans **before** it acts, and replans when you add a capability.

Notes:
- If you remember one thing from tonight, make it this.
- No path: STUCK. Zero calls, zero cost.
- Add one @Action: new plan. No router.
- It plans before it acts. And your code defines the world it works in.

---

## Resources

- **Demo repo**: [github.com/dashaun/agentic-ai-workflows-with-embabel-1.5.3](https://github.com/dashaun/agentic-ai-workflows-with-embabel-1.5.3)
- **Embabel Agent**: [github.com/embabel/embabel-agent](https://github.com/embabel/embabel-agent)
- **Embabel documentation**: [docs.embabel.com](https://docs.embabel.com)
- **Stack**: Embabel 1.5.3, Spring Boot 4.1.1, Spring AI 2.0.1, Spring Shell 4.0.3
- **Runtime**: JDK 21+ (the repo pins 25.0.3), Ollama, Redis, Grafana LGTM

Clone it, run `./mvnw spring-boot:run`, then `oom-incident`. The unit tests cover the agent runner and the SRE runbook.

Notes:
- Everything I showed is in this repo.
- Clone it tonight. ./mvnw spring-boot:run, then oom-incident. Tests included.
- Break it. Add an action. Tell me what you find.

---

<!-- .slide: data-background-color="#6db33f" -->

# Questions?

> What goal could a typed worker safely pursue in your system tomorrow?

<p class="closing-url"><a href="https://dashaun.com">DaShaun.com</a></p>

Slides, code, events, and every social link live there.

**DaShaun Carter** · Spring Developer Advocate · [SpringOfficeHours.io](https://springofficehours.io) · [@coffeesoftware](https://youtube.com/@coffeesoftware)

Notes:
- This is where Java backend development is going: systems that don't just respond to requests. They work to achieve goals.
- And you can build them with the tools you already know.
- Thank you, STL JUG!
- So: what goal could your system hand a typed worker tomorrow? Let's hear it.
- (Everything's at DaShaun.com.)
