# Agentic AI Workflows with Embabel 1.5.3: Development Guide

This file provides comprehensive guidance for working with code in this repository.

## Repository Purpose

50-minute workshop/conference presentation on **Agentic AI Workflows with Embabel 1.5.3**. Covers building disciplined AI agents on the JVM using goal-oriented planning (GOAP) and operational domain knowledge (DICE).

## Architecture

### Presentation Structure

- **index.html**: Main entry point; orchestrates markdown files as RevealJS slides
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
2. Add `<section>` entry to `index.html` inside `<div class="slides">`
3. Use same data attributes as other sections for consistency

### Styling & Colors

- **Primary color**: `#6db33f` (Spring green, same as Spring Boot materials)
- **Title slides**: Use `data-background-color="#6db33f"` directive at top of .md
- **Code examples**: Keep concise (use short variable names, remove boilerplate) to fit on screen
- **Tables**: Prefer tables over bullet lists for information organization

### Common Content Patterns

- **Code examples**: Concise and focused; show key lines only; 3-4 methods max
- **Goal/OODA examples**: Use SRE incident (OutOfMemory, canary validation) or coding task
- **Domain knowledge examples**: Show rules as methods with @DomainRule annotation

## Development

### Running Locally

```bash
cd docs
jwebserver -p 8000
# Open http://localhost:8000
# Press S for speaker view with notes and timer
```

### Key Requirements

- JDK 21+
- RevealJS library in `docs/reveal.js/` (copied from spring-boot-41-sweet-n-sour)
- All markdown files UTF-8, RevealJS separators must be exact

### Files NOT to Edit

- `docs/reveal.js/`: Third-party library; already configured

## Content Guidelines

### Slide Count

Current structure: intro → warmer → challenge → framework → actions → SRE domain → orchestration → DICE → demo → JVM → outro (11 sections). "Why the JVM" is the closing argument, after the demo.

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
- **Canary Deployment** (sre-domain.md): Four-stage progression showing validation workflow
- **DICE** (dice.md): Venn diagram showing LLM reasoning ∩ Domain knowledge

All SVG diagrams:
- Scale perfectly for web and PDF
- Use Spring green (#6db33f) for consistency
- Include labels and clear visual hierarchy

### Typography & Spacing

- H1: 2.8em, H2: 2em, H3: 1.3em (generous sizing for readability)
- Section padding: 60px horizontal (professional spacing)
- Line height: 1.6 (readable, print-friendly)
- Blockquotes: Italic, green border, subtle shadow

### PDF Export

- All graphics are SVG (vector-based, perfect for PDF)
- CSS includes @media print rules to optimize for PDFs
- Spacing and typography render cleanly in both web and print
- Use browser print-to-PDF or reveal.js export

## Important Notes

- **Color scheme**: Green (#6db33f), not purple. Changed to match Spring materials.
- **Professional styling**: Generous spacing, clear hierarchy, professional typography
- **SVG diagrams**: Used for all graphics (scalable, PDF-ready, no image files needed)
- **Resources slide**: Spring Boot 4.0+, Spring AI 2.0+, JDK 21+
- **Code examples**: Condensed; prioritize readability
- **SRE incident**: Use OutOfMemory + canary validation pattern (not just metric-based scaling)
