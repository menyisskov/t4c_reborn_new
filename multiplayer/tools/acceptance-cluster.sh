#!/usr/bin/env bash
# Disposable, create-owned PostgreSQL 16 cluster for live acceptance runs.
# Adapted from db-recovery-cluster.sh (T4C-0208). Usage:
#   acceptance-cluster.sh create <new|t4c_acc_<12 hex>> <port>
#   acceptance-cluster.sh status|start|stop|crash|destroy|password <t4c_acc_<12 hex>>
# The password is generated into a mode-0600 file inside the cluster directory. Only the
# `password` subcommand prints it (for the PowerShell wrapper to capture into a process-scoped
# variable); no other subcommand, log line or argument ever carries it.
set -euo pipefail
umask 077

action=${1:-}
id=${2:-}
port_arg=${3:-}

[[ "$action" =~ ^(create|status|crash|start|stop|destroy|password)$ ]] || { echo 'Invalid cluster action' >&2; exit 2; }
if [[ "$action" == create && "$id" == new ]]; then
  id="t4c_acc_$(openssl rand -hex 6)"
fi
[[ "$id" =~ ^t4c_acc_[0-9a-f]{12}$ ]] || { echo 'Invalid acceptance run identifier' >&2; exit 2; }

base=$HOME/.local/share/t4c-online-pg-acceptance
root=$base/$id
data=$root/data
socket=$root/socket
marker=$root/owner.marker
meta=$root/cluster.meta
password=$root/password
runtime=$HOME/.local/share/t4c-online-pg/runtime
bin=$runtime/usr/lib/postgresql/16/bin
share=$runtime/usr/share/postgresql/16
export LD_LIBRARY_PATH=$runtime/usr/lib/x86_64-linux-gnu:$runtime/usr/lib/postgresql/16/lib
superuser=t4c_acc
protected_ports=(55432 55433 18765 18766 18770 18771)

port=
validate_port() {
  local p=$1 q
  [[ "$p" =~ ^[0-9]{4,5}$ ]] && (( 10#$p >= 1024 && 10#$p <= 65535 )) || { echo 'Port must be a number from 1024 to 65535' >&2; exit 2; }
  p=$((10#$p))
  for q in "${protected_ports[@]}"; do
    [[ "$p" != "$q" ]] || { echo "Port $p is protected and can never be used" >&2; exit 2; }
  done
  port=$p
}
port_free() {
  ! ss -ltnH | awk '{print $4}' | grep -Eq "(^|:)${port}\$"
}
load_meta() {
  # The recorded port is the single source of truth after create.
  [[ -f "$meta" && ! -L "$meta" ]] || { echo 'Cluster metadata missing' >&2; exit 3; }
  local meta_id meta_port
  meta_id=$(sed -n 's/^id=//p' "$meta")
  meta_port=$(sed -n 's/^port=//p' "$meta")
  [[ "$meta_id" == "$id" ]] || { echo 'Cluster metadata id mismatch' >&2; exit 3; }
  validate_port "$meta_port"
}
owned() {
  [[ -d "$base" && ! -L "$base" && $(realpath "$base") == "$base" && $(stat -c %u "$base") == "$(id -u)" ]] || { echo 'Acceptance base directory ownership mismatch' >&2; exit 3; }
  [[ -d "$root" && ! -L "$root" && -d "$data" && ! -L "$data" && -d "$socket" && ! -L "$socket" ]] || { echo 'Owned cluster directories missing or linked' >&2; exit 3; }
  [[ -f "$marker" && ! -L "$marker" && $(cat "$marker") == "$id" ]] || { echo 'Owned cluster marker mismatch' >&2; exit 3; }
  [[ $(stat -c %u "$root") == "$(id -u)" && $(stat -c %u "$data") == "$(id -u)" ]] || { echo 'Cluster ownership mismatch' >&2; exit 3; }
  [[ $(realpath "$data") == "$data" && $(realpath "$root") == "$root" ]] || { echo 'Cluster path changed' >&2; exit 3; }
  load_meta
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
  port_free || { echo "Acceptance port $port is occupied" >&2; exit 3; }
  "$bin/pg_ctl" -D "$data" -l "$root/postgres.log" \
    -o "-h 127.0.0.1 -p $port -k $socket -c listen_addresses=127.0.0.1 -c max_connections=40" \
    -w -t 30 start >"$root/control.log" 2>&1
  running_pid >/dev/null
  "$bin/pg_isready" -h 127.0.0.1 -p "$port" -q
}
create_owned=0
create_success=0
create_cleanup() {
  local original_status=$1
  if [[ "$original_status" -ne 0 && "$create_owned" -eq 1 && "$create_success" -eq 0 ]]; then
    # Only this invocation's own, verified postmaster may be stopped. Directory is kept for inspection.
    if [[ -f "$marker" && ! -L "$marker" && $(cat "$marker") == "$id" && -f "$data/postmaster.pid" ]]; then
      if running_pid >/dev/null 2>&1; then
        if "$bin/pg_ctl" -D "$data" -m fast -w -t 30 stop >"$root/create-failure-stop.log" 2>&1; then
          echo 'CREATE_FAILURE_OWNED_STOP_OK' >&2
        else
          echo 'CREATE_FAILURE_OWNED_STOP_FAILED; inspect the cluster logs' >&2
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
    [[ -n "$port_arg" ]] || { echo 'create needs a port argument' >&2; exit 2; }
    validate_port "$port_arg"
    [[ -x "$bin/initdb" && -x "$bin/pg_ctl" && -x "$bin/createdb" && -d "$share" ]] || { echo 'PostgreSQL 16 runtime missing' >&2; exit 3; }
    port_free || { echo "Acceptance port $port is occupied" >&2; exit 3; }
    mkdir -p -m 700 "$base"
    [[ ! -L "$base" && $(realpath "$base") == "$base" && $(stat -c %u "$base") == "$(id -u)" ]] || { echo 'Acceptance base directory ownership mismatch' >&2; exit 3; }
    [[ ! -e "$root" && ! -L "$root" ]] || { echo 'Run directory already exists' >&2; exit 3; }
    mkdir -m 700 "$root"
    printf '%s\n' "$id" > "$marker"
    printf 'id=%s\nport=%s\n' "$id" "$port" > "$meta"
    create_owned=1
    trap 'create_cleanup $?' EXIT
    mkdir -m 700 "$socket"
    ( umask 077; openssl rand -hex 32 > "$password" )
    chmod 600 "$password"
    "$bin/initdb" -D "$data" -L "$share" -U "$superuser" --auth-local=scram-sha-256 --auth-host=scram-sha-256 --pwfile="$password" --no-instructions >"$root/initdb.log" 2>&1
    owned
    start_postgres
    PGPASSWORD=$(cat "$password") "$bin/createdb" -h 127.0.0.1 -p "$port" -U "$superuser" "$id" >"$root/create-db.log" 2>&1
    echo "CREATE_OK id=$id port=$port database=$id user=$superuser"
    create_success=1
    ;;
  status)
    owned
    if [[ -f "$data/postmaster.pid" ]]; then
      pid=$(running_pid) || { echo 'DOWN (stale PID file)'; exit 0; }
      started=$(sed -n '3p' "$data/postmaster.pid")
      echo "UP id=$id port=$port pid=$pid started=$started"
    else
      echo "DOWN id=$id port=$port"
    fi
    ;;
  crash)
    owned
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
    [[ ! -e "$data/postmaster.pid" ]] || { echo 'Postmaster PID file exists; use status, or stop first' >&2; exit 3; }
    start_postgres
    pid=$(running_pid)
    started=$(sed -n '3p' "$data/postmaster.pid")
    if [[ -f "$root/crash.before" && ! -L "$root/crash.before" ]]; then
      read -r old_pid old_started < "$root/crash.before"
      [[ "$pid" != "$old_pid" && "$started" != "$old_started" ]] || { echo 'Postmaster PID/start time did not change' >&2; exit 3; }
      grep -Eq 'database system was interrupted|database system was not properly shut down' "$root/postgres.log" || { echo 'PostgreSQL crash-recovery log evidence missing' >&2; exit 3; }
      rm -f "$root/crash.before"
      echo "RECOVERY_OK pid=$pid started=$started"
    else
      echo "START_OK pid=$pid started=$started"
    fi
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
  destroy)
    owned
    if [[ -f "$data/postmaster.pid" ]]; then
      if running_pid >/dev/null 2>&1; then
        "$bin/pg_ctl" -D "$data" -m fast -w -t 30 stop >"$root/stop-control.log" 2>&1
      fi
      [[ ! -e "$data/postmaster.pid" ]] || { echo 'Refusing to delete: PID file remains and is not a verified stopped cluster' >&2; exit 3; }
    fi
    port_free || { echo "Refusing to delete: port $port is still listening" >&2; exit 3; }
    # Delete only the directory this tool created: exact base/id path, real directory, marker verified.
    [[ "$root" == "$base/$id" && "$root" == "$HOME"/.local/share/t4c-online-pg-acceptance/t4c_acc_* && "$(realpath "$root")" == "$root" ]] || { echo 'Refusing to delete: path prefix check failed' >&2; exit 3; }
    rm -rf --one-file-system -- "$root"
    echo "DESTROY_OK id=$id"
    ;;
  password)
    owned
    [[ -f "$password" && ! -L "$password" && $(stat -c %a "$password") == 600 ]] || { echo 'Private password file missing or not mode 0600' >&2; exit 3; }
    cat "$password"
    ;;
esac
