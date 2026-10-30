#!/usr/bin/env bash
# Backup half of the restored-gameplay check (T4C-0350): runs multiplayer/tools/verify_backup.py against a disposable
# acceptance cluster, restoring into a NEW t4c_restore_* database inside the same cluster.
#   restore-check-backup.sh <t4c_acc_<12 hex>> <t4c_restore_<12 hex>> <verify_backup.py path (WSL)> <report path (WSL, new file)>
# The cluster password is read from the cluster's own mode-0600 file into a mode-0600 PGPASSFILE that exists only for the
# duration of this script; it is never printed, never in an argument and never logged. The archive stays inside the
# cluster directory (it holds account verifiers) and disappears with the cluster. Only the JSON report (counts and
# hashes, no credentials) is copied to the requested path.
set -euo pipefail
umask 077
id=${1:-}; restore=${2:-}; verifier=${3:-}; report=${4:-}
[[ "$id" =~ ^t4c_acc_[0-9a-f]{12}$ ]] || { echo 'Invalid acceptance run identifier' >&2; exit 2; }
[[ "$restore" =~ ^t4c_restore_[0-9a-f]{12}$ ]] || { echo 'Invalid restore database name' >&2; exit 2; }
[[ -f "$verifier" && ! -e "$report" ]] || { echo 'Verifier missing or report path already exists' >&2; exit 2; }
base=$HOME/.local/share/t4c-online-pg-acceptance
root=$base/$id
[[ -d "$root" && ! -L "$root" && "$(cat "$root/owner.marker" 2>/dev/null)" == "$id" ]] || { echo 'Cluster ownership check failed' >&2; exit 3; }
port=$(sed -n 's/^port=//p' "$root/cluster.meta" | head -1)
[[ "$port" =~ ^[0-9]{4,5}$ ]] || { echo 'Cluster port unreadable' >&2; exit 3; }
case "$port" in 55432|55433|18765|18766|18770|18771) echo 'Protected port' >&2; exit 3;; esac
runtime=$HOME/.local/share/t4c-online-pg/runtime
export LD_LIBRARY_PATH=$runtime/usr/lib/x86_64-linux-gnu:$runtime/usr/lib/postgresql/16/lib
pgpass=$(mktemp "$root/pgpass.XXXXXX")
trap 'rm -f "$pgpass"' EXIT
printf '127.0.0.1:%s:*:t4c_acc:%s\n' "$port" "$(cat "$root/password")" > "$pgpass"
chmod 600 "$pgpass"
out=$root/restore-check-$restore
export PGHOST=127.0.0.1 PGPORT=$port PGUSER=t4c_acc PGPASSFILE=$pgpass PGDATABASE=$id
python3 "$verifier" --source "$id" --restore "$restore" --output-dir "$out" --pg-bin "$runtime/usr/lib/postgresql/16/bin"
cp "$out"/*.json "$report"
echo "RESTORE_OK $restore"
