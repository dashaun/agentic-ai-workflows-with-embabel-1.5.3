#!/usr/bin/env bash
# Stand-in for demo/demo.sh in the smoke test: same ready/beat/result protocol, no Docker.
set -u
for n in 1 2 3; do
  printf '\e]777;ready;%d\a' "$n"; read -rsn1 _
  clear; printf '\e]777;beat;%d\a' "$n"
  echo "beat $n output"; sleep 0.2
  printf '\e]777;result;{"status":"COMPLETED","actions":%d,"budget":8}\a' "$n"
done
printf '\e]777;ready;0\a'; while true; do read -rsn1 _; done
