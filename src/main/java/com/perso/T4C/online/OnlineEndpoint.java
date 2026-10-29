package com.perso.T4C.online;

import java.net.URI;

/** Origin-only endpoints; credentials must never travel over remote plaintext. */
public final class OnlineEndpoint {
  private OnlineEndpoint() {}

  public static URI validate(URI endpoint) {
    if (endpoint == null || endpoint.getHost() == null || endpoint.getUserInfo() != null
        || endpoint.getRawQuery() != null || endpoint.getRawFragment() != null
        || !(endpoint.getRawPath().isEmpty() || endpoint.getRawPath().equals("/"))
        || endpoint.getPort() == 0 || endpoint.getPort() > 65535)
      throw new IllegalArgumentException("Expected an HTTPS server origin or local development endpoint");
    boolean secure = "https".equalsIgnoreCase(endpoint.getScheme());
    boolean local = "http".equalsIgnoreCase(endpoint.getScheme())
        && "127.0.0.1".equals(endpoint.getHost()) && endpoint.getPort() > 0;
    if (!secure && !local)
      throw new IllegalArgumentException("Remote online connections require HTTPS");
    return URI.create((secure ? "https" : "http") + "://" + endpoint.getRawAuthority());
  }
}
