# Agentic AI Workflows with Embabel 1.5.3: Development Guide

This file provides comprehensive guidance for working with code in this repository.

## Repository Purpose

50-minute workshop/conference presentation on **Agentic AI Workflows with Embabel 1.5.3**. Covers building disciplined AI agents on the JVM using goal-oriented planning (GOAP) and operational domain knowledge (DICE).

## Architecture

### Presentation Structure

- **docs/index.html**: Main entry point; orchestrates markdown files as RevealJS slides
- **docs/*.md**: Content organized as individual slide sections (not one monolithic file)
- **Slide ordering**: Sections load in index.html order; changing `<section>` tags reorders slides

### Two Domain Examples

Throughout the presentation, examples use two parallel domains to show pattern reusability:

1. **Coding Assistants**: Code analysis, linting, refactoring (team standards as domain rules)
2. **SRE Workers**: Production incident response, log analysis, canary deployments (runbooks as domain rules)

Both domains demonstrate the same Embabel patterns: Actions (Spring Beans), Goals, Constraints, OODA loops, and DICE.

## How to Edit

### RevealJS Markdown Format

- **Horizontal slide separator**: `^\f` (form feed character): new section with title slide
- **Vertical subsection separator**: `^---`: nested slide under a section
- **Speaker notes**: Start with `Notes:` on its own line; visible in presenter view (S key)

**Example structure**:
```markdown
<!-- .slide: data-background-color="#6db33f" -->
# Main Title

### Subtitle

Notes:
Speaker notes here...

---

## Content Slide Under This Section
```

### Adding a New Slide Section

1. Create a new `docs/section-name.md` file with RevealJS format
2. Add `<section>` entry to `docs/index.html` inside `<div class="slides">`
3. Use same data attributes as other sections for consistency

### Styling & Colors

- **Primary color**: `#6db33f` (Spring green, same as Spring Boot materials)
- **Title slides**: Use `data-background-color="#6db33f"` directive at top of .md
- **Code examples**: Keep concise (use short variable names, remove boilerplate) to fit on screen
- **Tables**: Prefer tables over bullet lists for information organization
- **Slide size**: Reveal lays out at 1200×800 (3:2). Content taller than that is clipped at the bottom; check tall code slides in the browser and condense the code rather than shrink the font

### Common Content Patterns

- **Code examples**: Concise and focused; show key lines only; 3-4 methods max
- **Goal/OODA examples**: Use SRE incident (OutOfMemory, canary validation) or coding task
- **Domain knowledge examples**: Show rules as plain Java (the `SreRunbook` switch, `RunbookAssessment`), not annotations

## Development

### Running Locally

```bash
./run.sh            # live deck: server.js serves docs/ and runs demo/demo.sh in a docked terminal
npm run smoke       # click-to-run protocol test against scripts/fake-demo.sh
./mvnw test         # unit tests: AgentRunner budget/STUCK, SreRunbook, RunbookChecker, DockerComposeService
jwebserver -d "$(pwd)/docs" -p 8000   # static deck only; add /?print-pdf to export (see PDF Export)
# Press S for speaker view with notes and timer
```

### Live Demo (don't break this)

- A slide with `data-beat="N"` (and `data-takeaway`) is a demo step. Arriving on it sends `goto:N`; `server.js` presses a key for `demo/demo.sh` only while the script waits for a beat at or below the highest one requested, so steps run once, in order.
- `demo.sh` reports progress with private OSC sequences (`ESC ] 777 ; ready|beat|result ; … BEL`). Keep `beat N` calls numbered to match the slides.
- Commands run from `target/classes` via `demo/embabel` so patches need only `javac`. `demo/format.py` stops a JVM that lingers after a model call, and every step has a time limit (`STEP_LIMIT`).
- Only the main window drives the demo; speaker-view iframes never send `goto`.
- On a time limit, `shell()` in `demo.sh` stops the JVM by its pid (it is a grandchild of the step's subshell), then unloads and re-warms the model so abandoned generation doesn't starve the next step.
- The step-to-slide map and the live-coding patches are documented in `DEMO.md` ("In the Deck"). Rehearsed results on the static slides must match what the live run prints (status, action count, action order).

### Demo App Notes

- `httpclient5` in `pom.xml` is load-bearing: Embabel's Ollama client uses a plain `RestClient.builder()`, and without HttpClient 5 it falls back to Reactor Netty's 10s read timeout, which breaks CPU inference and triggers retry pile-ups. `spring.http.clients.*` properties do not reach this client.
- Embabel also applies its own per-attempt LLM timeout (60s) with retries.
- Jev (Spring AI TypeSafe) needs `TYPESAFE_API_KEY` in the environment that starts the server. Without it the check reports "not configured"; a configured but failing Jev fails closed and escalates. Never print the key.

### Key Requirements

- JDK 21+, Docker, Node 18+ (live deck)
- RevealJS library in `docs/reveal.js/` (copied from spring-boot-41-sweet-n-sour)
- All markdown files UTF-8, RevealJS separators must be exact

### Files NOT to Edit

- `docs/reveal.js/`: Third-party library; already configured
- `compose.yml` and `.envrc`: the presenter maintains these
- `.claude/`, `.impeccable/`, and `PRODUCT.md` are local and git-ignored

## Content Guidelines

### Slide Count

Current structure: intro → warmer → challenge → framework → actions → SRE domain → orchestration → DICE → JVM → outro (10 sections). There is no demo section: each live step sits right after the slide that explains it (warmer: hello world; actions: coding worker; SRE domain: compose status; orchestration: planner run and budget; DICE: poisoned logs; JVM: the STUCK-to-new-plan live coding). "Why the JVM" is the closing argument.

### Domain Example Consistency

When adding slides:
- Use **both domains** in examples (Coding + SRE) to show pattern is general
- Coding examples: linters, tests, refactoring suggestions
- SRE examples: metrics queries, log analysis, canary deployments, rollbacks

### Abstract Alignment

Presentation must cover:
✓ "Hello World" vs production challenges  
✓ Infinite loops problem  
✓ Tool coordination without if-else  
✓ Java domain models  
✓ Rod Johnson/Spring creator mention  
✓ Strong typing, OODA loops, GOAP  
✓ Beyond RAG  
✓ Spring Beans as Actions  
✓ DICE for domain knowledge  
✓ Why JVM for production  
✓ Code-heavy examples  

## Visual Design

### SVG Diagrams (Professional, PDF-Ready)

- **OODA Loop** (enter-embabel.md): Circular cycle showing Observe → Orient → Decide → Act
- **Type chain** (`.chain` in sre-domain.md, goal-oriented.md): HTML rendered from one `SRE_CHAIN` definition in index.html; `data-on` picks the highlighted types
- **Two paths** (goal-oriented.md): SVG of the planner running or skipping `planCanary`
- **STUCK to new plan** (outro.md): before/after panels

All SVG diagrams:
- Scale perfectly for web and PDF
- Use Spring green (#6db33f) for consistency
- Include labels and clear visual hierarchy

### Typography & Spacing

- H1: 2.2em (2.6em on green title slides), H2: 1.5em, H3: 1.15em
- Body and lists 28px; tables 0.86em; code `pre` 0.8em (about 22px) in bundled JetBrains Mono; code lines up to 78 characters
- Section padding: 40px vertical, 60px horizontal (`box-sizing: border-box`)
- Text on `#6db33f` is dark `#111` (7.4:1); never white (2.6:1)
- Fonts live in `docs/fonts/`, and `docs/css/simple-offline.css` is the reveal theme without its Google Fonts imports, so the deck loads nothing from the network
- Line height: 1.6 (readable, print-friendly)
- Blockquotes: Italic, green border, no shadow

### PDF Export

- All graphics are SVG (vector-based, perfect for PDF)
- CSS includes @media print rules to optimize for PDFs
- Spacing and typography render cleanly in both web and print
- Export: serve `docs/` (`jwebserver -d "$(pwd)/docs" -p 8000`), open `http://localhost:8000/?print-pdf` in Chrome, Print → Save as PDF, margins None, background graphics on. One page per slide (`pdfSeparateFragments: false`); add `&showNotes=separate-page` for a notes page after each slide
- `html.reveal-print` moves the green top rule from `.slides` onto each `.pdf-page`; on `.slides` it pushes page 1 past the sheet and adds a blank page

## Important Notes

- **Color scheme**: Green (#6db33f), not purple. Changed to match Spring materials.
- **Professional styling**: Generous spacing, clear hierarchy, professional typography
- **SVG diagrams**: Used for all diagrams (scalable, PDF-ready); `docs/images/` holds only the intro photos and badges
- **Resources slide**: Embabel 1.5.3, Spring Boot 4.1.1, Spring AI 2.0.1, Spring Shell 4.0.3, JDK 21+
- **Code examples**: Condensed; prioritize readability
- **SRE incident**: Use OutOfMemory + canary validation pattern (not just metric-based scaling)
