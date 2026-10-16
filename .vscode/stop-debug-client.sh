#!/usr/bin/env bash
set -euo pipefail

# Match ForgeGradle's client entry point and this launch configuration's JDWP port.
# Leave Gradle daemons, language servers, and non-debug Minecraft clients running.
jps_command="${JAVA_HOME:?JAVA_HOME must point to a JDK}/bin/jps"
java_processes=$("$jps_command" -lv)

while read -r process_id main_class vm_args; do
    [[ "$process_id" =~ ^[0-9]+$ ]] || continue
    [[ "$main_class" == "GradleStart" ]] || continue
    [[ "$vm_args" =~ -agentlib:jdwp=[^[:space:]]*address=5005([,[:space:]]|$) ]] || continue

    echo "Stopping Minecraft debug client (PID $process_id)."
    case "${OSTYPE:-}" in
        msys*|cygwin*)
            MSYS_NO_PATHCONV=1 taskkill.exe /PID "$process_id" /T /F
            ;;
        *)
            kill "$process_id"
            ;;
    esac
done <<< "$java_processes"
