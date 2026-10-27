package com.perso.T4C.online.model;

/** Wire compatibility only; this is not authentication or a content-integrity proof. */
public final class OnlineProtocol {
  private OnlineProtocol() {}

  public static final int VERSION = 5;
  public static final String VERSION_HEADER = "X-T4C-Protocol";
}
