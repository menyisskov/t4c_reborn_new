package com.perso.T4C.server;

import com.perso.T4C.online.model.GameSocketProtocol;
import com.perso.T4C.online.model.OnlineProtocol;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/** Adapts a typed game-socket command to the existing authoritative route dispatcher. */
final class GameSocketExchange extends HttpExchange {
  private final Headers requestHeaders = new Headers();
  private final Headers responseHeaders = new Headers();
  private final URI requestUri;
  private final String method;
  private final InetSocketAddress remote;
  private final InetSocketAddress local;
  private final InputStream body;
  private final ByteArrayOutputStream response = new ByteArrayOutputStream();
  private final Map<String,Object> attributes = new HashMap<>();
  private int responseCode = -1;

  GameSocketExchange(GameSocketProtocol.Request request, String token, InetSocketAddress remote,
      InetSocketAddress local, String origin) {
    this.requestUri = URI.create("/" + request.command() + (request.query().isEmpty() ? "" : "?" + request.query()));
    this.method = request.method();
    this.remote = remote;
    this.local = local;
    boolean accountCreation=method.equals("POST") && request.command().equals("account/characters");
    if(!accountCreation && request.body().chars().anyMatch(character -> character > 0x7f))
      throw new IllegalArgumentException("Game action body must use the existing ASCII protocol");
    byte[] bytes = request.body().getBytes(accountCreation
        ? java.nio.charset.StandardCharsets.UTF_8 : java.nio.charset.StandardCharsets.US_ASCII);
    body = new ByteArrayInputStream(bytes);
    requestHeaders.set("Host", URI.create(origin).getRawAuthority());
    requestHeaders.set(OnlineProtocol.VERSION_HEADER, Integer.toString(OnlineProtocol.VERSION));
    String authorization=token==null ? request.authorization() : "Bearer "+token;
    if (authorization != null) requestHeaders.set("Authorization", authorization);
    if (method.equals("POST")) {
      requestHeaders.set("Origin", origin);
      requestHeaders.set("Content-Type", accountCreation ? "application/json" : "text/plain");
      requestHeaders.set("Content-Length", Integer.toString(bytes.length));
    }
  }

  int responseCode() { return responseCode; }
  @Override public int getResponseCode() { return responseCode; }
  byte[] responseBytes() { return response.toByteArray(); }

  @Override public Headers getRequestHeaders() { return requestHeaders; }
  @Override public Headers getResponseHeaders() { return responseHeaders; }
  @Override public URI getRequestURI() { return requestUri; }
  @Override public String getRequestMethod() { return method; }
  @Override public HttpContext getHttpContext() { return null; }
  @Override public void close() { }
  @Override public InputStream getRequestBody() { return body; }
  @Override public OutputStream getResponseBody() { return response; }
  @Override public void sendResponseHeaders(int code, long length) throws IOException {
    if (responseCode != -1 || length < 0) throw new IOException("Invalid internal response");
    responseCode = code;
  }
  @Override public InetSocketAddress getRemoteAddress() { return remote; }
  @Override public InetSocketAddress getLocalAddress() { return local; }
  @Override public String getProtocol() { return "T4C/" + OnlineProtocol.VERSION; }
  @Override public Object getAttribute(String name) { return attributes.get(name); }
  @Override public void setAttribute(String name, Object value) { attributes.put(name, value); }
  @Override public void setStreams(InputStream input, OutputStream output) {
    throw new UnsupportedOperationException("Socket exchange streams are fixed");
  }
  @Override public HttpPrincipal getPrincipal() { return null; }
}
