package com.perso.T4C.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/** Online listener configuration. Plaintext is restricted to a resolved loopback address. */
public final class ServerTransport {
  private final InetAddress address;
  private final SSLContext tls;
  private java.net.URI publicOrigin;

  private ServerTransport(InetAddress address, SSLContext tls) {
    if (tls == null && !address.isLoopbackAddress())
      throw new IllegalArgumentException("Non-loopback listeners require TLS");
    this.address = address;
    this.tls = tls;
  }

  public static ServerTransport local() throws IOException {
    return new ServerTransport(InetAddress.getByName("127.0.0.1"), null);
  }

  public static ServerTransport fromEnvironment(Map<String, String> environment)
      throws IOException, GeneralSecurityException {
    var address = InetAddress.getByName(environment.getOrDefault("T4C_BIND_ADDRESS", "127.0.0.1"));
    String keyFile = environment.get("T4C_TLS_KEYSTORE");
    String passwordFile = environment.get("T4C_TLS_PASSWORD_FILE");
    if (keyFile == null && passwordFile == null) {
      if (environment.containsKey("T4C_PUBLIC_ORIGIN")) throw new IllegalArgumentException("Public origin requires TLS");
      return new ServerTransport(address, null);
    }
    if (keyFile == null || passwordFile == null)
      throw new IllegalArgumentException("TLS requires both keystore and password file");
    char[] password = Files.readString(Path.of(passwordFile)).stripTrailing().toCharArray();
    try {
      var keys = KeyStore.getInstance("PKCS12");
      try (var input = Files.newInputStream(Path.of(keyFile))) { keys.load(input, password); }
      boolean privateKey = false;
      for (var aliases = keys.aliases(); aliases.hasMoreElements();) {
        if (keys.entryInstanceOf(aliases.nextElement(), KeyStore.PrivateKeyEntry.class)) privateKey = true;
      }
      if (!privateKey) throw new GeneralSecurityException("TLS keystore has no private key");
      var managers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      managers.init(keys, password);
      var context = SSLContext.getInstance("TLS");
      context.init(managers.getKeyManagers(), null, null);
      var transport = new ServerTransport(address, context);
      String origin = environment.get("T4C_PUBLIC_ORIGIN");
      if (origin == null && !address.isLoopbackAddress())
        throw new IllegalArgumentException("Remote TLS listeners require T4C_PUBLIC_ORIGIN");
      if (origin != null) transport.publicOrigin = parseOrigin(origin, "https");
      return transport;
    } finally { Arrays.fill(password, '\0'); }
  }

  public boolean secure() { return tls != null; }

  public String origin(int port) {
    if (publicOrigin != null) return publicOrigin.toString();
    String host = address.getHostAddress();
    if (host.contains(":")) host = "[" + host + "]";
    return (secure() ? "https" : "http") + "://" + host + ":" + port;
  }

  public boolean acceptsOrigin(String candidate, int port) {
    try {
      var expected = parseOrigin(origin(port), secure() ? "https" : "http");
      var actual = parseOrigin(candidate, secure() ? "https" : "http");
      return expected.getHost().equalsIgnoreCase(actual.getHost()) && effectivePort(expected) == effectivePort(actual);
    } catch (IllegalArgumentException invalid) { return false; }
  }

  public boolean acceptsHost(String host, int port) {
    return host != null && acceptsOrigin((secure() ? "https" : "http") + "://" + host, port);
  }

  private static java.net.URI parseOrigin(String value, String scheme) {
    if (value == null) throw new IllegalArgumentException("Missing origin");
    var uri = java.net.URI.create(value);
    if (!scheme.equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
        || uri.getRawQuery() != null || uri.getRawFragment() != null || !uri.getRawPath().isEmpty()
        || uri.getPort() == 0 || uri.getPort() > 65535)
      throw new IllegalArgumentException("Expected a server origin without path or credentials");
    return uri;
  }

  private static int effectivePort(java.net.URI uri) {
    return uri.getPort() < 0 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
  }

  /** Binds the persistent native session socket with the same TLS identity as the HTTPS API. */
  public ServerSocket createGameSocket(int port) throws IOException {
    ServerSocket socket = tls == null ? new ServerSocket() : tls.getServerSocketFactory().createServerSocket();
    try {
      socket.setReuseAddress(false);
      socket.bind(new InetSocketAddress(address, port), 32);
      if (socket instanceof SSLServerSocket secure)
        secure.setEnabledProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
      return socket;
    } catch (IOException | RuntimeException failure) {
      try { socket.close(); } catch (IOException ignored) { }
      throw failure;
    }
  }
  public HttpServer create(int port) throws IOException {
    if (publicOrigin != null && effectivePort(publicOrigin) != port)
      throw new IllegalArgumentException("Public origin port must match listener port");
    var bind = new InetSocketAddress(address, port);
    if (tls == null) return HttpServer.create(bind, 32);
    var server = HttpsServer.create(bind, 32);
    server.setHttpsConfigurator(new HttpsConfigurator(tls) {
      @Override public void configure(HttpsParameters parameters) {
        var options = getSSLContext().getDefaultSSLParameters();
        options.setProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
        parameters.setSSLParameters(options);
      }
    });
    return server;
  }
}
