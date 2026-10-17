# Agentic AI Workflows with Embabel 1.5

## STL JUG October 2026 Abstract

We have all seen the “Hello, World” of Spring AI: sending a prompt and getting a response. But as we move toward production, the real challenge is not the LLM call; it is the workflow. How do you ensure an agent does not loop infinitely? How do you coordinate multiple tools without a mess of “if-else” blocks? And how do we keep our Java-centric domain models at the heart of the AI’s reasoning?

Enter Embabel, a new JVM-based framework from Rod Johnson (creator of Spring) designed to bring discipline to agentic AI. Unlike Python-centric alternatives, Embabel is built on the philosophy of strong typing, OODA loops (Observe, Orient, Decide, Act), and Goal-Oriented Action Planning (GOAP).

In this session, we will go beyond basic RAG and explore how to build “digital workers” that can actually plan. You will learn: How to turn your existing Spring Beans into AI Actions.

The shift from imperative coding to Goal-Oriented orchestration.

How Embabel uses DICE (Domain-Integrated Context Engineering) to give agents true domain knowledge.

Why the JVM is actually the best place to run mission-critical AI agents.

Join us for a code-heavy look at the future of Java backend development. We are moving to a world where our systems do not just respond to requests, but actively work to achieve goals.

## Quick Start: Run the Demo

```bash
# Build and run the Spring Shell CLI
./mvnw spring-boot:run

# Then try commands like:
embabel> docker-compose-status
embabel> oom-incident
embabel> oom-incident --budget 2
embabel> stored-analyses
embabel> safe-quick-win
```

See [DEMO.md](DEMO.md) for the full command reference and the live-coding script.

## Presentation Overview

Ten sections, about 50 minutes. Each live demo step sits right after the slide that explains it:

| SECTION | COVERS | LIVE STEP |
|---|---|---|
| Intro | Speaker, links | |
| From Hello World to Production | One `ChatClient` call vs. a workflow | 1: `dad-joke` |
| The Challenge | Loops, tool spam, lost context in two domains (coding, SRE) | |
| Enter Embabel | Rod Johnson, strong typing, OODA, GOAP | |
| Spring Beans as Actions | `@Agent`, `@Action`, records as the contract | 2: coding worker (`safe-quick-win`) |
| An SRE Digital Worker | Typed actions, the goal, Jev checking the model | 3: `docker-compose-status` |
| From Imperative to Goal-Oriented | Blackboard, types instead of a router, budget | 4: `oom-incident`, 5: `--budget 2` |
| DICE | Runbook as Java, live state as context, beyond RAG | 6: poisoned logs |
| Why the JVM | Types are the plan, operations you already run | 7: STUCK, 8: add an `@Action` |
| Disciplined Workflows | Recap, resources, questions | |

## Requirements

- JDK 21+ (`.sdkmanrc` pins `25.0.3-librca`; run `sdk env`)
- Docker running, and nothing else on port 11434 (quit a native Ollama)
- Node 18+ for the live deck (`./run.sh`)
- Optional: `TYPESAFE_API_KEY` for Jev, the runbook check. Without it the check reports "not configured"; if Jev is configured but unreachable, the incident escalates
- Stack: Spring Boot 4.1.1, Spring AI 2.0.1, Embabel 1.5.3, Spring Shell 4.0.3. `compose.yml` runs Ollama (`qwen2.5-coder:1.5b` baked in), Redis Stack, and Grafana LGTM

## Running the Presentation

### Live deck (recommended for the talk)

```bash
./run.sh
```

This starts `server.js` (Node 18+), opens the deck at http://localhost:4173, and prepares the live demo in the background: Compose stack, compile, model warm-up. Then just click through the deck. Each demo slide (`data-beat="N"` in the markdown) runs its step when you arrive: the slide's title and takeaway sit above a live terminal, and the outcome (COMPLETED, TERMINATED, STUCK, ESCALATED) is headlined.

- **Click ahead and the steps queue.** Each one runs to completion, in order.
- **Go back and nothing re-runs.** A step you left shows its normal slide again.
- **L** toggles between the terminal and the static slide, which carries the rehearsed result. Use it if the demo misbehaves; **↻ restart demo** starts it over.
- The speaker view (S) shows a small demo-status pill that the audience never sees.
- The live-coding steps apply `demo/patches/*.patch` and recompile with `javac`; both are reverted when the demo ends or restarts.
- `npm run smoke` checks the click-to-run protocol against a fake demo, with no Docker needed.
- Start `./run.sh` from a shell where `TYPESAFE_API_KEY` is already exported; the preflight warns if it isn't.
- Keep the laptop awake (`caffeinate -dimsu ./run.sh`): sleep stalls a running step.
- Each step has a time limit (`STEP_LIMIT`, default 90s). Past it, the step stops its JVM, unloads and re-warms the model, and points at the slide's rehearsed result. A model call on a laptop CPU takes from a few seconds to about 45.

The demo script is `demo/demo.sh`; `demo/format.py` trims each command's output to the planner narration and a result summary.

### Static deck only

```bash
jwebserver -d "$(pwd)/docs" -p 8000
```

Then open http://localhost:8000. The demo slides show their rehearsed results.

### PDF

With the static deck running, open http://localhost:8000/?print-pdf in Chrome and choose Print → Save as PDF, with margins None and background graphics on. You get one 3:2 page per slide with every fragment shown. Add `&showNotes=separate-page` to the URL for a speaker-notes page after each slide.

## Embabel 1.5.3 Notes

This is the Embabel 1.5.3 version of the 1.0 demo. Embabel 1.5 is built on Spring Boot 4.1 and Spring AI 2.0, so the demo now uses `embabel-agent-starter-ollama` instead of only the `embabel-agent-api` annotations.

- At startup, Embabel deploys `safe-remediation-planner` and `sre-incident-worker` to its agent platform.
- Embabel finds the available models through the Ollama API. `embabel.models.default-llm` must exactly match a discovered model name, tag included (`qwen2.5-coder:1.5b`). If it doesn't match, or Ollama is not reachable, the app fails at startup.
- The compose `ollama` service publishes port 11434. Stop any native Ollama on that port before running the demo.
- Agents run on Embabel's `AgentPlatform` through `AgentRunner`, with an 8-action budget. In 1.5.3, `ProcessOptions.withBudget` alone does not change the enforced termination policy, so `AgentRunner` also adds the budget's policy.
- Embabel builds its Ollama client with a plain `RestClient.builder()`, so Spring Boot's HTTP client settings don't reach it. Without `httpclient5` on the classpath that builder picks Reactor Netty, whose 10s read timeout cuts off slow CPU inference, and Spring AI's retries then pile abandoned requests onto Ollama. The `pom.xml` adds `httpclient5` so the builder uses Apache HttpClient 5 instead.
