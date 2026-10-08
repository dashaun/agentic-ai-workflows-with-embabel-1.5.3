# Agentic AI Workflows with Embabel

## Uberconf 2026 Abstract

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

This talk explores how to move beyond chatbots and into disciplined agentic AI workflows with "digital workers":

- **The Challenge**: Why basic LLM chains fail in production (two domains: Coding & SRE)
- **OODA Loops & GOAP**: How Embabel structures agent reasoning
- **Spring Beans as Actions**: Reusing your existing tools and services
- **Digital Workers: Two Domains**
  - **Coding Assistants**: Code analysis, refactoring, quality improvement
  - **SRE Workers**: Production issue diagnosis, remediation, operational automation
- **Goal-Oriented Planning**: From imperative scripting to declarative goal-driven workflows
- **DICE**: Domain knowledge: code standards, operational procedures
- **Why the JVM**: Production reliability, strong typing, and existing investments

## Requirements

- JDK 21+
- Spring Boot 4.1.1
- Spring AI 2.0.1
- Embabel 1.5.3
- Docker Compose

## Running the Presentation

### Using JDK 21+ built-in server

```bash
jwebserver -d "$(pwd)/docs" -p 8000
```

Then open http://localhost:8000 in your browser.
## Embabel 1.5.3 Notes

This is the Embabel 1.5.3 version of the 1.0 demo. Embabel 1.5 is built on Spring Boot 4.1 and Spring AI 2.0, so the demo now uses `embabel-agent-starter-ollama` instead of only the `embabel-agent-api` annotations.

- At startup, Embabel deploys `safe-remediation-planner` and `sre-incident-worker` to its agent platform.
- Embabel finds the available models through the Ollama API. `embabel.models.default-llm` must exactly match a discovered model name, tag included (`qwen2.5-coder:1.5b`). If it doesn't match, or Ollama is not reachable, the app fails at startup.
- The compose `ollama` service publishes port 11434. Stop any native Ollama on that port before running the demo.
- Agents run on Embabel's `AgentPlatform` through `AgentRunner`, with an 8-action budget. In 1.5.3, `ProcessOptions.withBudget` alone does not change the enforced termination policy, so `AgentRunner` also adds the budget's policy.
