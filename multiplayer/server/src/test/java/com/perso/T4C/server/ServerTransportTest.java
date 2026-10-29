package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerTransportTest {
  @TempDir Path directory;

  @Test void rejectsRemotePlaintextAndIncompleteTlsConfiguration() throws Exception {
    assertFalse(ServerTransport.fromEnvironment(Map.of()).secure());
    assertThrows(IllegalArgumentException.class,
        () -> ServerTransport.fromEnvironment(Map.of("T4C_BIND_ADDRESS", "0.0.0.0")));
    assertThrows(IllegalArgumentException.class,
        () -> ServerTransport.fromEnvironment(Map.of("T4C_TLS_KEYSTORE", "missing")));
  }

  @Test void tlsRequiresTrustedCertificateAndMatchingHostname() throws Exception {
    var keystore = directory.resolve("server.p12");
    var password = directory.resolve("password");
    Files.writeString(password, "test-fixture-only\n");
    String suffix = System.getProperty("os.name").startsWith("Windows") ? ".exe" : "";
    var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "keytool" + suffix).toString(),
        "-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048", "-validity", "1",
        "-dname", "CN=localhost", "-ext", "SAN=dns:localhost", "-storetype", "PKCS12",
        "-keystore", keystore.toString(), "-storepass:file", password.toString(), "-noprompt")
        .redirectErrorStream(true).redirectOutput(directory.resolve("keytool.log").toFile()).start();
    if (!process.waitFor(30, TimeUnit.SECONDS)) { process.destroyForcibly(); fail("keytool timed out"); }
    assertEquals(0, process.exitValue(), "keytool fixture creation failed");
    var transport = ServerTransport.fromEnvironment(Map.of("T4C_TLS_KEYSTORE", keystore.toString(),
        "T4C_TLS_PASSWORD_FILE", password.toString()));
    var server = transport.create(0);
    server.createContext("/", exchange -> {
      byte[] response = "secure".getBytes(java.nio.charset.StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      try (var output = exchange.getResponseBody()) { output.write(response); }
    });
    server.start();
    try {
      var keys = KeyStore.getInstance("PKCS12");
      try (var input = Files.newInputStream(keystore)) { keys.load(input, "test-fixture-only".toCharArray()); }
      var trusted = KeyStore.getInstance("PKCS12");
      trusted.load(null, null);
      trusted.setCertificateEntry("server", keys.getCertificate("server"));
      var managers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      managers.init(trusted);
      var context = SSLContext.getInstance("TLS");
      context.init(null, managers.getTrustManagers(), null);
      int port = server.getAddress().getPort();
      var request = HttpRequest.newBuilder(URI.create("https://localhost:" + port))
          .timeout(Duration.ofSeconds(5)).build();
      try (var client = HttpClient.newBuilder().sslContext(context).build()) {
        assertEquals("secure", client.send(request, HttpResponse.BodyHandlers.ofString()).body());
        var wrongHost = HttpRequest.newBuilder(URI.create("https://127.0.0.1:" + port))
            .timeout(Duration.ofSeconds(5)).build();
        assertThrows(SSLHandshakeException.class,
            () -> client.send(wrongHost, HttpResponse.BodyHandlers.ofString()));
      }
      try (var untrusted = HttpClient.newHttpClient()) {
        assertThrows(SSLHandshakeException.class,
            () -> untrusted.send(request, HttpResponse.BodyHandlers.ofString()));
      }
      server.stop(0);
      String origin="https://localhost:"+port;
      var configured=ServerTransport.fromEnvironment(Map.of("T4C_TLS_KEYSTORE",keystore.toString(),
          "T4C_TLS_PASSWORD_FILE",password.toString(),"T4C_PUBLIC_ORIGIN",origin));
      assertFalse(configured.acceptsHost("attacker.example:"+port,port));
      try (var preview=new PreviewServer(new PreviewMap(new boolean[8][8],0,0,3,3),port,null,null,configured);
           var client=HttpClient.newBuilder().sslContext(context).build()) {
        preview.start(); assertEquals(origin,preview.url());
        var mapReply=client.send(HttpRequest.newBuilder(URI.create(origin+"/map")).timeout(Duration.ofSeconds(5)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(200,mapReply.statusCode());
        int gameSocketPort=com.google.gson.JsonParser.parseString(mapReply.body()).getAsJsonObject()
            .get("gameSocketPort").getAsInt();
        try(var gameSocket=(javax.net.ssl.SSLSocket)context.getSocketFactory().createSocket("localhost",gameSocketPort)) {
          gameSocket.setSoTimeout(5000);
          var parameters=gameSocket.getSSLParameters();parameters.setEndpointIdentificationAlgorithm("HTTPS");
          parameters.setProtocols(new String[] {"TLSv1.3","TLSv1.2"});gameSocket.setSSLParameters(parameters);
          gameSocket.startHandshake();
          var input=new java.io.DataInputStream(gameSocket.getInputStream());
          var output=new java.io.DataOutputStream(gameSocket.getOutputStream());
          com.perso.T4C.online.model.GameSocketProtocol.writeFrame(output,
              new com.google.gson.Gson().toJson(new com.perso.T4C.online.model.GameSocketProtocol.Hello(
                  com.perso.T4C.online.model.OnlineProtocol.VERSION)));
          var hello=com.google.gson.JsonParser.parseString(
              com.perso.T4C.online.model.GameSocketProtocol.readFrame(input)).getAsJsonObject();
          assertTrue(hello.get("accepted").getAsBoolean());
        }
        assertThrows(SSLHandshakeException.class,()->{
          try(var wrongHost=(javax.net.ssl.SSLSocket)context.getSocketFactory().createSocket("127.0.0.1",gameSocketPort)) {
            wrongHost.setSSLParameters(parametersForHttps());wrongHost.startHandshake();
          }
        });
        var join=HttpRequest.newBuilder(URI.create(origin+"/join"))
            .header("Origin",origin).header("Content-Type","text/plain")
            .header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER,
                Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION))
            .POST(HttpRequest.BodyPublishers.noBody()).timeout(Duration.ofSeconds(5)).build();
        assertEquals(200,client.send(join,HttpResponse.BodyHandlers.ofString()).statusCode());
        var crossOrigin=HttpRequest.newBuilder(URI.create(origin+"/join"))
            .header("Origin","https://attacker.example").header("Content-Type","text/plain")
            .POST(HttpRequest.BodyPublishers.noBody()).timeout(Duration.ofSeconds(5)).build();
        assertEquals(403,client.send(crossOrigin,HttpResponse.BodyHandlers.ofString()).statusCode());
      }
    } finally { server.stop(0); }
  }

  private static javax.net.ssl.SSLParameters parametersForHttps() {
    var parameters=new javax.net.ssl.SSLParameters();
    parameters.setEndpointIdentificationAlgorithm("HTTPS");
    parameters.setProtocols(new String[] {"TLSv1.3","TLSv1.2"});
    return parameters;
  }
}
