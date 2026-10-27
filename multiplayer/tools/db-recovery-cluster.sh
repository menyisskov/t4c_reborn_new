#!/usr/bin/env bash
# T4C-0208: operate only a create-owned, isolated PostgreSQL 16 cluster.
set -euo pipefail
umask 077

action=${1:-}
id=${2:-}
[[ "$id" =~ ^t4c_db_0208_[0-9a-f]{12}$ ]] || { echo 'Invalid isolated run identifier' >&2; exit 2; }
[[ "$action" =~ ^(create|status|crash|start|stop|password)$ ]] || { echo 'Invalid cluster action' >&2; exit 2; }

base=/home/menyiss/.local/share/t4c-online-pg-0208
root=$base/$id
data=$root/data
socket=$root/socket
marker=$root/owner.marker
password=$root/password
bin=/home/menyiss/.local/share/t4c-online-pg/runtime/usr/lib/postgresql/16/bin
share=/home/menyiss/.local/share/t4c-online-pg/runtime/usr/share/postgresql/16
export LD_LIBRARY_PATH=/home/menyiss/.local/share/t4c-online-pg/runtime/usr/lib/x86_64-linux-gnu:/home/menyiss/.local/share/t4c-online-pg/runtime/usr/lib/postgresql/16/lib
port=55433

port_free() {
  ! ss -ltnH | awk '{print $4}' | grep -Eq '(^|:)55433$'
}
owned() {
  [[ -d "$base" && ! -L "$base" && $(realpath "$base") == "$base" && $(stat -c %u "$base") == "$(id -u)" ]] || { echo 'Isolated base directory ownership mismatch' >&2; exit 3; }
  [[ -d "$root" && ! -L "$root" && -d "$data" && ! -L "$data" && -d "$socket" && ! -L "$socket" ]] || { echo 'Owned cluster directories missing or linked' >&2; exit 3; }
  [[ -f "$marker" && ! -L "$marker" && $(cat "$marker") == "$id" ]] || { echo 'Owned cluster marker mismatch' >&2; exit 3; }
  [[ $(stat -c %u "$root") == "$(id -u)" && $(stat -c %u "$data") == "$(id -u)" ]] || { echo 'Cluster ownership mismatch' >&2; exit 3; }
  [[ $(realpath "$data") == "$data" && $(realpath "$root") == "$root" ]] || { echo 'Cluster path changed' >&2; exit 3; }
}
running_pid() {
  owned
  [[ -f "$data/postmaster.pid" && ! -L "$data/postmaster.pid" ]] || return 1
  local pid actual_data actual_port args
  pid=$(sed -n '1p' "$data/postmaster.pid")
  actual_data=$(sed -n '2p' "$data/postmaster.pid")
  actual_port=$(sed -n '4p' "$data/postmaster.pid")
  [[ "$pid" =~ ^[0-9]+$ && "$actual_data" == "$data" && "$actual_port" == "$port" ]] || { echo 'Postmaster PID file does not match owned cluster' >&2; exit 3; }
  kill -0 "$pid" 2>/dev/null || return 1
  [[ $(stat -c %u "/proc/$pid") == "$(id -u)" ]] || { echo 'Postmaster owner mismatch' >&2; exit 3; }
  args=$(tr '\0' ' ' < "/proc/$pid/cmdline")
  [[ "$args" == *postgres* && "$args" == *"$data"* ]] || { echo 'Postmaster command does not match owned cluster' >&2; exit 3; }
  printf '%s\n' "$pid"
}
start_postgres() {
  port_free || { echo 'Isolated port 55433 is occupied' >&2; exit 3; }
  "$bin/pg_ctl" -D "$data" -l "$root/postgres.log" -o "-h 127.0.0.1 -p $port -k $socket" -w -t 30 start >"$root/control.log" 2>&1
  running_pid >/dev/null
  "$bin/pg_isready" -h 127.0.0.1 -p "$port" -q
}
create_owned=0
create_success=0
create_cleanup() {
  local original_status=$1
  if [[ "$original_status" -ne 0 && "$create_owned" -eq 1 && "$create_success" -eq 0 ]]; then
    # Eligibility begins only after this invocation created the run directory and marker.
    # A marker/PID mismatch is a refusal, never permission to stop another process.
    if [[ -f "$marker" && ! -L "$marker" && $(cat "$marker") == "$id" && -f "$data/postmaster.pid" ]]; then
      if running_pid >/dev/null 2>&1; then
        if "$bin/pg_ctl" -D "$data" -m fast -w -t 30 stop >"$root/create-failure-stop.log" 2>&1; then
          echo 'CREATE_FAILURE_OWNED_STOP_OK' >&2
        else
          echo 'CREATE_FAILURE_OWNED_STOP_FAILED; inspect private cluster marker and logs' >&2
        fi
      else
        echo 'CREATE_FAILURE_PID_UNVERIFIED; no process was stopped' >&2
      fi
    fi
  fi
  exit "$original_status"
}

case "$action" in
  create)
    [[ -x "$bin/initdb" && -x "$bin/pg_ctl" && -d "$share" ]] || { echo 'PostgreSQL 16 runtime missing' >&2; exit 3; }
    port_free || { echo 'Isolated port 55433 is occupied' >&2; exit 3; }
    mkdir -p -m 700 "$base"
    [[ ! -L "$base" && $(realpath "$base") == "$base" && $(stat -c %u "$base") == "$(id -u)" ]] || { echo 'Isolated base directory ownership mismatch' >&2; exit 3; }
    [[ ! -e "$root" && ! -L "$root" ]] || { echo 'Run directory already exists' >&2; exit 3; }
    mkdir -m 700 "$root"
    printf '%s\n' "$id" > "$marker"
    create_owned=1
    trap 'create_cleanup $?' EXIT
    mkdir -m 700 "$socket"
    openssl rand -hex 32 > "$password"
    "$bin/initdb" -D "$data" -L "$share" -U t4c_recovery --auth-local=scram-sha-256 --auth-host=scram-sha-256 --pwfile="$password" --no-instructions >"$root/initdb.log" 2>&1
    owned
    start_postgres
    if [[ ${T4C_0208_TEST_FAIL_AFTER_START:-0} == 1 ]]; then
      echo 'Controlled post-start failure fixture' >&2
      exit 77
    fi
    PGPASSWORD=$(cat "$password") "$bin/createdb" -h 127.0.0.1 -p "$port" -U t4c_recovery "$id" >"$root/create-db.log" 2>&1
    echo 'CREATE_OK'
    create_success=1
    ;;
  status)
    owned
    if [[ -f "$data/postmaster.pid" ]]; then
      pid=$(running_pid) || exit 3
      started=$(sed -n '3p' "$data/postmaster.pid")
      echo "UP pid=$pid started=$started"
    else
      echo 'DOWN'
    fi
    ;;
  crash)
    pid=$(running_pid) || { echo 'Owned postmaster is not running' >&2; exit 3; }
    started=$(sed -n '3p' "$data/postmaster.pid")
    printf '%s %s\n' "$pid" "$started" > "$root/crash.before"
    "$bin/pg_ctl" -D "$data" -m immediate -w -t 30 stop >"$root/crash-control.log" 2>&1
    if running_pid >/dev/null 2>&1 || ! port_free || "$bin/pg_isready" -h 127.0.0.1 -p "$port" -q; then
      echo 'Owned database outage was not proven' >&2; exit 3
    fi
    echo 'CRASH_OUTAGE_OK'
    ;;
  start)
    owned
    [[ -f "$root/crash.before" && ! -L "$root/crash.before" ]] || { echo 'No owned crash record' >&2; exit 3; }
    [[ ! -e "$data/postmaster.pid" ]] || { echo 'Postmaster PID file still exists' >&2; exit 3; }
    start_postgres
    pid=$(running_pid)
    started=$(sed -n '3p' "$data/postmaster.pid")
    read -r old_pid old_started < "$root/crash.before"
    [[ "$pid" != "$old_pid" && "$started" != "$old_started" ]] || { echo 'Postmaster PID/start time did not change' >&2; exit 3; }
    grep -Eq 'database system was interrupted|database system was not properly shut down' "$root/postgres.log" || { echo 'PostgreSQL crash-recovery log evidence missing' >&2; exit 3; }
    echo "RECOVERY_OK pid=$pid started=$started"
    ;;
  stop)
    owned
    if [[ -f "$data/postmaster.pid" ]]; then
      running_pid >/dev/null || exit 3
      "$bin/pg_ctl" -D "$data" -m fast -w -t 30 stop >"$root/stop-control.log" 2>&1
      [[ ! -e "$data/postmaster.pid" ]] || { echo 'Owned postmaster PID file remains' >&2; exit 3; }
    fi
    echo 'STOP_OK'
    ;;
  password)
    owned
    [[ -f "$password" && ! -L "$password" ]] || { echo 'Private password file missing' >&2; exit 3; }
    cat "$password"
    ;;
esac
