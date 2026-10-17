<!-- .slide: data-background-color="#6db33f" -->

# Agentic AI Workflows with Embabel 1.5

## Building Disciplined Digital Workers on the JVM

DaShaun Carter | Spring Developer Advocate

[DaShaun.com](https://dashaun.com)

Notes:
- Good evening, STL JUG! I'm DaShaun Carter. I love my job, and I'm jazzed to be here.
- Quick question: who here has called an LLM from Java? Keep that hand handy.
- Tonight is about what happens after that first call works.
- We're building digital workers: software that takes a goal, makes a plan, uses your tools, and stops when it's done.
- Beyond basic RAG. Code-heavy. All on the JVM.
- You leave with four things: Spring Beans as AI actions. The shift from imperative to goal-oriented. DICE. And why the JVM is where these should run.

PRE-FLIGHT
- Docker up. `sdk env` for JDK 25.0.3. TYPESAFE_API_KEY in the environment.
- `./run.sh` opens this deck with the live terminal. Wait for "Ready." (press L on a demo slide to peek).
- Every demo step runs when you reach its slide. Just click. L toggles terminal and slide.
- Keys: S speaker view · B blackout · F fullscreen · Esc overview.

---

## DaShaun Carter

<ul class="bio">
<li><span id="the-number-of-days">DEFAULT</span> days as <strong>Spring Developer Advocate</strong>, VMware Tanzu</li>
<li class="fragment">411 days as Sr. Partner Solution Architect, Redis</li>
<li class="fragment">845 days as Sr. Platform Architect, Pivotal/VMware</li>
<li class="fragment">985 days as VP of Engineering, Mobile Capital Group/MedZero</li>
<li class="fragment">1995 days as Tech Lead Engineer, Garmin Connect</li>
</ul>

Notes:
- That first number is real: days as a Spring Developer Advocate.
- Click: Redis. Pivotal and VMware. MedZero. Garmin Connect.
- I've done some things. Production things. Pagers, outages, the 2 a.m. call.
- That's the lens tonight: not "can the model do it?" but "can I run it?"

---

<div class="promo-row">
  <figure>
    <a href="https://springofficehours.io"><img src="images/spring-office-hours-blank.png" alt="Spring Office Hours"></a>
    <figcaption><a href="https://springofficehours.io">SpringOfficeHours.io</a></figcaption>
  </figure>
  <figure>
    <a href="https://link.springer.com/book/9798868826399"><img src="images/book_cover.jpg" alt="Pro Spring Boot 4 book cover"></a>
    <figcaption>Pro Spring Boot 4</figcaption>
  </figure>
  <figure>
    <img src="images/aaif-ambassador-2026.png" alt="AAIF Ambassador 2026 badge">
    <figcaption>AAIF Ambassador 2026</figcaption>
  </figure>
</div>

Notes:
- A few places you can find me.
- Spring Office Hours: bring your questions, we'll dig in.
- Pro Spring Boot 4, with Felipe Gutierrez.
- And I'm an Agentic AI Foundation ambassador for 2026. This stuff is my day job.

---

<img class="promo-banner" src="images/coffeesoftware_banner.jpg" alt="Coffee Software">

[youtube.com/@coffeesoftware](https://youtube.com/@coffeesoftware)

## [DaShaun.com](https://dashaun.com)

Slides, code, events, and every social link live there.

Notes:
- Coffee Software on YouTube.
- If you remember one URL tonight: DaShaun.com. Slides, code, every link.

---

<img class="family" src="images/family.jpg" alt="Family dinner" width="800" height="600">

Notes:
- My crew. They're why I care about software that doesn't page me at dinner.
- OK. Let's build something.
