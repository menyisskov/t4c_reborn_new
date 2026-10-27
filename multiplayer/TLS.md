# Encrypted online transport

The online transport supports HTTPS with a PKCS12 server private key and certificate
chain. Single-player does not load this configuration. Local development can continue
using `http://127.0.0.1:18765`; remote plaintext endpoints are refused before a client
network worker can send credentials. HTTPS accepts an explicit port or the standard
443. Endpoints are origins only: no username, password, path, query or fragment.

## Server configuration

Set these variables in the server process environment:

| Variable | Meaning |
| --- | --- |
| `T4C_BIND_ADDRESS` | Address to bind; defaults to `127.0.0.1`. A non-loopback address requires TLS. |
| `T4C_PUBLIC_ORIGIN` | HTTPS origin clients use, e.g. `https://game.example:18443`. Required for non-loopback TLS. Its port must match the listener. |
| `T4C_TLS_KEYSTORE` | Path to PKCS12 containing the server private key and certificate chain. |
| `T4C_TLS_PASSWORD_FILE` | Path to the UTF-8 keystore password, optionally ending in a newline. |

Supply both TLS variables together. The key password must match the keystore
password. Missing keys, incorrect passwords and incomplete TLS settings fail startup;
there is no plaintext fallback. The listener permits TLS 1.2 and 1.3. Restart the
server after certificate renewal. Keep the keystore and password outside the checkout
and restrict both to the server account (owner-only POSIX permissions or Windows ACLs).
The loader does not itself change or validate platform ACLs. Trailing whitespace is
removed from the password file; do not use a password ending in whitespace.

Use a certificate whose Subject Alternative Name matches the exact hostname clients
use. A public service needs a certificate chain trusted by the client JVM. The
client uses standard JSSE trust managers and hostname validation, and follows no
HTTP redirects. For a private test CA, provide a dedicated truststore using the JVM
`javax.net.ssl.trustStore` and `javax.net.ssl.trustStoreType` properties before starting
the online client. Import the CA/public certificate only, never the server private key.
Do not disable hostname verification or install a trust-all manager.

## Verification and integration

`ServerTransportTest` generates a one-day, temporary self-signed certificate with
the installed JDK's `keytool`. It verifies successful trusted HTTPS, refusal of an
untrusted certificate, hostname mismatch refusal, and remote plaintext refusal.
The fixture never becomes a production certificate or bundled trust anchor.
`OnlineEndpointTest` verifies origin validation, remote plaintext refusal and
redirect refusal. The tests require JDK 21, already required by the build.

The native client and server entry points use these helpers. Existing preview
constructors explicitly retain local HTTP; environment configuration applies only to
an explicitly launched server. Host and POST Origin headers must match the configured
public origin. Administrative shutdown additionally requires a loopback network peer
and the control token; gameplay TLS does not enable remote administration.

For a foreground remote server, with the database environment already configured:

```powershell
$env:T4C_BIND_ADDRESS = '0.0.0.0'
$env:T4C_PUBLIC_ORIGIN = 'https://game.example:18443'
$env:T4C_TLS_KEYSTORE = 'C:\private\server.p12'
$env:T4C_TLS_PASSWORD_FILE = 'C:\private\server-password.txt'
java -Djava.awt.headless=true -jar multiplayer/server/target/t4c-server.jar --online assets/maps/worldmap/worldmap.colbin 18443
```

From another machine with the packaged game and its private account profile:

```powershell
./multiplayer/start-client.ps1 -Endpoint https://game.example:18443 -Profile C:\private\player.json
```

The local `start-preview.ps1` readiness/stop tools intentionally reject TLS or remote
bind settings and direct operators to the foreground command. Stop that foreground
server with Ctrl+C so it checkpoints and releases sessions. Existing `-Port` and
`-Profile` client invocations continue to target local HTTP. The `-Endpoint` parameter
takes precedence over `-Port`. Standard JVM truststore settings can be supplied through
`JAVA_TOOL_OPTIONS` for the online launch; do not put credentials in that variable.

Remote deployment still requires a hostname, trusted certificate, firewall/access
configuration and end-to-end native client verification over HTTPS. TLS does not
replace account revocation, rate limits or server authority. The automated TLS
fixture proves transport behavior; it does not constitute production deployment or
two native clients tested over a remote network.
