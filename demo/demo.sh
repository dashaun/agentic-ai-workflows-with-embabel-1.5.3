#!/usr/bin/env bash
# The live demo, one beat per slide. server.js runs this in a PTY; the deck
# asks for beat N when the presenter reaches the slide with data-beat="N", and
# the server presses a key only while this script is waiting for that beat.
#
# Each beat: wait for the key, clear (the slide changes with it), type the
# command, run it through demo/format.py. The live-coding beats apply the
# patches in demo/patches/ and recompile with javac; they are reverted on exit.
set -u
cd "$(dirname "$0")/.."

GREEN=$'\e[32m'; CYAN=$'\e[36m'; DIM=$'\e[2m'; BOLD=$'\e[1m'; RED=$'\e[31m'; YELLOW=$'\e[33m'; RESET=$'\e[0m'
WORKER=src/main/java/com/embabel/demo/sre/SreIncidentWorker.java
CANARY=src/main/java/com/embabel/demo/domain/CanaryPlan.java
CANARY_CLASS=target/classes/com/embabel/demo/domain/CanaryPlan.class
PATCH1=demo/patches/01-require-canary-plan.patch
PATCH2=demo/patches/02-add-plan-canary-action.patch
STEP_LIMIT=${STEP_LIMIT:-90}   # seconds before a step gives up and points at the slide

ready()  { printf '\e]777;ready;%d\a' "$1"; read -rsn1 _; }
beat()   { ready "$1"; clear; printf '\e]777;beat;%d\a' "$1"; printf '%s# %s%s\n\n' "$DIM" "$2" "$RESET"; }
typed()  { # types a command at a prompt, like a person would
  printf '%s%s%s ' "$GREEN" "$1" "$RESET"
  local s=$2 i
  for (( i=0; i<${#s}; i++ )); do printf '%s' "${s:i:1}"; sleep 0.025; done
  printf '\n'
}
compile() { javac --release 21 -parameters -nowarn -d target/classes -cp "target/classes:$(cat target/demo.classpath)" "$@"; }

# Runs `embabel> <command>` with a time limit. A slow laptop model must never
# freeze the talk: past the limit the step stops and the slide carries on.
shell() {
  typed "embabel>" "$*"
  local pidfile; pidfile=$(mktemp)
  ( ( demo/embabel "$@" & echo $! >"$pidfile"; wait ) 2>&1 | DEMO_JAVA_PIDFILE="$pidfile" python3 demo/format.py ) &
  local pid=$! waited=0
  while kill -0 "$pid" 2>/dev/null; do
    sleep 1
    if (( ++waited >= STEP_LIMIT )); then
      # the JVM is a grandchild of $pid: stop it by its pid, or it keeps calling the model
      [[ -s "$pidfile" ]] && kill -TERM "$(cat "$pidfile")" 2>/dev/null
      pkill -TERM -P "$pid" 2>/dev/null; kill "$pid" 2>/dev/null
      # Ollama can keep generating for a client that is gone and starve the next step:
      # unloading the model stops it, then warm it again in the background.
      curl -s -m 10 "$OLLAMA_URL/api/generate" -d '{"model":"qwen2.5-coder:1.5b","keep_alive":0}' >/dev/null
      warm &
      printf '\e]777;result;{"status":"TIMEOUT"}\a'
      printf '\n  %sThis step ran past %ss on this laptop. The slide has the rehearsed result.%s\n' "$YELLOW" "$STEP_LIMIT" "$RESET"
      break
    fi
  done
  wait "$pid" 2>/dev/null; rm -f "$pidfile"
}

show_patch() { # the diff, colored, without git's header noise
  typed "\$" "git apply $1"
  git apply "$1"
  grep -v -E '^(diff --git|index |new file mode|--- |\+\+\+ )' "$1" | while IFS= read -r line; do
    case "$line" in
      +*) printf '  %s%s%s\n' "$GREEN" "$line" "$RESET" ;;
      -*) printf '  %s%s%s\n' "$RED" "$line" "$RESET" ;;
      @@*) ;;
      *) printf '  %s%s%s\n' "$DIM" "$line" "$RESET" ;;
    esac
  done
}

restore_code() { # leave the repo exactly as it was
  if git apply -R --check "$PATCH2" 2>/dev/null; then git apply -R "$PATCH2"; fi
  if git apply -R --check "$PATCH1" 2>/dev/null; then git apply -R "$PATCH1"; fi
  rm -f "$CANARY_CLASS"
  [[ -f target/demo.classpath ]] && compile "$WORKER" 2>/dev/null
}
trap restore_code EXIT

warm() { # load the model and keep it loaded for the talk
  curl -s -m 120 "$OLLAMA_URL/api/generate" \
    -d '{"model":"qwen2.5-coder:1.5b","prompt":"ok","stream":false,"keep_alive":"4h","options":{"num_predict":1}}' >/dev/null
}

hold() { printf '\n%s%s%s\n' "$YELLOW" "$1" "$RESET"; printf '\e]777;ready;0\a'; while true; do read -rsn1 _; done; }

# ---------------------------------------------------------------- preflight
clear
printf '%sPreparing the live demo…%s\n\n' "$BOLD" "$RESET"
restore_code
docker info >/dev/null 2>&1 || hold "Docker is not running. Start Docker Desktop, then click ↻ restart in the deck."
printf '  %s▸ compose stack%s\n' "$DIM" "$RESET"
docker compose up -d --wait >/dev/null 2>&1 || hold "docker compose up failed. Run it in a terminal to see why, then click ↻ restart."
# A native Ollama app can hold 127.0.0.1:11434 while Docker's serves [::1]:11434.
# Use whichever one actually has the demo model.
export OLLAMA_URL=""
for url in http://127.0.0.1:11434 "http://[::1]:11434"; do
  if curl -s -m 5 "$url/api/tags" | grep -q 'qwen2.5-coder:1.5b'; then OLLAMA_URL=$url; break; fi
done
[[ -n "$OLLAMA_URL" ]] || hold "No Ollama on port 11434 serves qwen2.5-coder:1.5b. Check docker compose ps, then click ↻ restart."
printf '  %s▸ compile and classpath%s\n' "$DIM" "$RESET"
./mvnw -q -DskipTests compile dependency:build-classpath \
  -Dmdep.outputFile=target/demo.classpath -Dmdep.includeScope=runtime >/dev/null 2>&1 \
  || hold "The build failed. Run ./mvnw compile in a terminal to see why, then click ↻ restart."
printf '  %s▸ warm the model%s\n' "$DIM" "$RESET"
warm \
  || printf '  %sOllama did not answer the warm-up; the first step may be slow.%s\n' "$YELLOW" "$RESET"
[[ -n "${TYPESAFE_API_KEY:-}" ]] || printf '  %sTYPESAFE_API_KEY is not set: the runbook check will report "not configured".%s\n' "$YELLOW" "$RESET"
printf '\n%sReady.%s Each demo step runs when you reach its slide.\n' "$GREEN" "$RESET"

# -------------------------------------------------------------------- beats
beat 1 "Hello world: prompt in, string out"
shell dad-joke --about java

beat 2 "Beans as actions: the coding worker"
shell stored-analyses
shell safe-quick-win

beat 3 "What is actually running?"
shell docker-compose-status

beat 4 "Plan, act, replan"
shell oom-incident

beat 5 "A budget of two actions"
shell oom-incident --budget 2

beat 6 "Poisoned logs"
shell injected-incident

beat 7 "Require a CanaryPlan nobody can make"
show_patch "$PATCH1"
typed "\$" "javac CanaryPlan.java SreIncidentWorker.java"
compile "$CANARY" "$WORKER"
shell oom-incident

beat 8 "Add one @Action"
show_patch "$PATCH2"
typed "\$" "javac SreIncidentWorker.java"
compile "$WORKER"
shell oom-incident

restore_code
printf '\n  %sDemo complete. The live-coding patches are reverted.%s\n' "$DIM" "$RESET"
hold ""
