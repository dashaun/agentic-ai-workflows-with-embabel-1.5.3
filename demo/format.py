#!/usr/bin/env python3
"""Turns one demo command's raw output into something the back row can read.

Keeps Embabel's planner narration (plans, actions, bound objects, outcome),
drops Spring startup noise, and summarizes the JSON result. Nothing is
invented: every line printed comes from the command's own output.
It also emits a private OSC marker with the outcome so the deck can headline it.
"""
import json
import re
import sys
import textwrap

DIM, BOLD, RESET = "\x1b[2m", "\x1b[1m", "\x1b[0m"
GREEN, CYAN, YELLOW, RED, WHITE = "\x1b[32m", "\x1b[36m", "\x1b[33m", "\x1b[31m", "\x1b[97m"

LOG = re.compile(r"^\d\d:\d\d:\d\d\.\d+ \[[^\]]+\] (\w+)\s+(\S+) - (.*)$")
PKG = re.compile(r"\bcom\.embabel\.demo\.(?:sre|shell|domain)\.")
STATUS_COLOR = {"COMPLETED": GREEN, "TERMINATED": RED, "STUCK": YELLOW}
WIDTH = 96


def short(text):
    return PKG.sub("", text)


def method(name):
    return short(name).split(".")[-1]


def say(line=""):
    sys.stdout.write(line + "\n")
    sys.stdout.flush()


def osc(kind, payload):
    sys.stdout.write(f"\x1b]777;{kind};{payload}\x07")
    sys.stdout.flush()


def wrap(label, text, color=WHITE):
    lines = textwrap.wrap(str(text), WIDTH - len(label) - 4) or [""]
    say(f"  {DIM}{label}{RESET} {color}{lines[0]}{RESET}")
    for more in lines[1:]:
        say(f"  {' ' * len(label)} {color}{more}{RESET}")


def narrate(level, logger, msg, pending_plan):
    """Prints one planner line. Returns True when the next line is a plan."""
    tag = re.match(r"^\[([^\]]+)\] (.*)$", msg)
    body = tag.group(2) if tag else msg
    if logger == "Embabel":
        if body.startswith("formulated plan"):
            return True
        if body.startswith("executing action"):
            say(f"  {WHITE}▸ {method(body.split()[-1])}{RESET}")
        elif body.startswith("object bound"):
            say(f"    {CYAN}+ {short(body.split()[-1]).removeprefix('it:')}{RESET}")
        elif "using LLM" in body:
            m = re.search(r"using LLM (\S+), creating (\w+)", body)
            if m:
                say(f"    {YELLOW}… model {m.group(1)} creating {m.group(2)}{RESET}")
        elif body.startswith("early termination"):
            m = re.search(r"maxActions=(\d+)", body)
            say(f"  {RED}■ budget reached: max {m.group(1) if m else '?'} actions{RESET}")
        elif body.startswith("completed in"):
            secs = re.search(r"PT([\d.]+)S", body)
            say(f"  {GREEN}✓ completed in {float(secs.group(1)):.1f}s{RESET}" if secs else f"  {GREEN}✓ {body}{RESET}")
        elif body.startswith("stuck"):
            say(f"  {YELLOW}■ stuck: no plan reaches the goal{RESET}")
    elif logger == "AStarGoapPlanner" and "not satisfied" in body:
        say(f"  {YELLOW}✗ planner: no sequence of actions reaches the goal{RESET}")
    elif level in ("WARN", "ERROR") and logger in ("RunbookChecker", "ChatClientLlmOperations"):
        say(f"  {YELLOW}! {short(body)[:WIDTH]}{RESET}")
    return False


def summarize(data):
    if isinstance(data, dict) and "status" in data and "agent" in data:
        status = data["status"]
        actions = data.get("actions") or []
        budget = data.get("actionBudget")
        check = (data.get("result") or {}).get("runbookCheck") or {}
        osc("result", json.dumps({"status": status, "actions": len(actions), "budget": budget,
                                  "escalate": bool(check.get("escalate")), "withinRunbook": check.get("withinRunbook")}))
        say()
        say(f"  {BOLD}{STATUS_COLOR.get(status, WHITE)}{status}{RESET}  {DIM}{len(actions)} of {budget} actions{RESET}")
        if actions:
            wrap("actions", " → ".join(actions))
        result = data.get("result") or {}
        response = result.get("response") or {}
        if response.get("recommendation"):
            wrap("model  ", response["recommendation"])
        check = result.get("runbookCheck")
        if check:
            score = check.get("withinRunbook")
            verdict = f"{check.get('judge')}  withinRunbook {score if score is not None else '—'}  escalate {str(check.get('escalate')).lower()}"
            wrap("check  ", verdict, RED if check.get("escalate") else GREEN)
        if "sourceId" in result:
            wrap("pick   ", f"{result['sourceId']}: {result.get('issue')}")
            wrap("plan   ", result.get("recommendation"))
        if "requiresApproval" in result:
            wrap("human  ", "approval required" if result["requiresApproval"] else "no approval", GREEN)
    elif isinstance(data, list) and data and isinstance(data[0], dict) and "SERVICE" in data[0]:
        say(f"  {DIM}{'SERVICE':<16}STATUS{RESET}")
        for s in data:
            color = GREEN if s.get("STATUS", "").startswith("Up") else RED
            say(f"  {WHITE}{s.get('SERVICE', ''):<16}{color}{s.get('STATUS', '')}{RESET}")
    elif isinstance(data, list) and data and isinstance(data[0], dict) and "qualityScore" in data[0]:
        say(f"  {DIM}{'SCORE':<7}{'ISSUES':<8}SUMMARY{RESET}")
        for a in data:
            say(f"  {WHITE}{a.get('qualityScore', ''):<7}{len(a.get('issues') or []):<8}{RESET}{a.get('summary', '')[:WIDTH - 15]}")
    else:
        say(json.dumps(data, indent=2)[:4000])


def lines_until_done(state):
    """Yields input lines. After a model call the JVM can idle for a minute
    before exiting; once the command's output is complete and the stream has
    been quiet for a moment, stop the JVM so the next step can start."""
    import os, select, signal
    fd = sys.stdin.fileno()
    buf = b""
    while True:
        ready, _, _ = select.select([fd], [], [], 2.5)
        if not ready:
            pidfile = os.environ.get("DEMO_JAVA_PIDFILE")
            if state["done"] and pidfile:
                try:
                    os.kill(int(open(pidfile).read().strip()), signal.SIGTERM)
                except (OSError, ValueError):
                    pass
                state["done"] = False  # signal once; keep draining until EOF
            continue
        chunk = os.read(fd, 65536)
        if not chunk:
            if buf:
                yield buf.decode("utf-8", "replace")
            return
        buf += chunk
        *complete, buf = buf.split(b"\n")
        for line in complete:
            yield line.decode("utf-8", "replace")


def main():
    pending_plan = False
    started = False  # plain output only counts once the app is up
    json_lines = None
    state = {"done": False}
    for line in lines_until_done(state):
        if json_lines is not None:
            json_lines.append(line)
            if line.rstrip() in ("}", "} ]", "]"):
                try:
                    summarize(json.loads("\n".join(json_lines)))
                    json_lines = None
                    state["done"] = True
                except json.JSONDecodeError:
                    pass  # a nested object closed; keep reading
            continue
        if started and (line == "{" or line.startswith("[ {") or line == "["):
            json_lines = [line]
            continue
        if pending_plan:
            pending_plan = False
            if line.startswith("  ") and not line.strip().startswith(("from:", "{")):
                say(f"  {GREEN}{BOLD}plan{RESET} {GREEN}{' → '.join(method(a.strip()) for a in line.split('->'))}{RESET}")
                continue
        m = LOG.match(line)
        if m and ("Deployed agent sre-incident-worker" in m.group(3) or "Started EmbabelShellDemoApplication" in m.group(3)):
            started = True
        if m:
            pending_plan = narrate(m.group(1), m.group(2), m.group(3), pending_plan)
            continue
        if not started or not line.strip() or line.startswith(("\t", "  ", "NOTE: Picked up", "Caused by", "java.", "org.", "io.", "[")):
            continue
        say(f"  {WHITE}{line}{RESET}")  # plain command output, e.g. the dad joke
        state["done"] = True


if __name__ == "__main__":
    main()
