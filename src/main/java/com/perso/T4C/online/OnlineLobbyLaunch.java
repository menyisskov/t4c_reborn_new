package com.perso.T4C.online;

import java.util.Arrays;
import java.util.Locale;

/**
 * Pure launch decisions for the opt-in online lobby. Nothing here touches the window, network or
 * profile files, so the default launch path and the lobby path can be unit tested headlessly.
 *
 * <p>The lobby is requested only explicitly: system property {@value #PROPERTY}, environment variable
 * {@value #ENVIRONMENT}, or the {@value #ARGUMENT} command-line argument. Without any of them the
 * client launches exactly as before.
 */
public final class OnlineLobbyLaunch {
  public static final String PROPERTY = "t4c.online.lobby";
  public static final String ENVIRONMENT = "T4C_ONLINE_LOBBY";
  public static final String ARGUMENT = "--lobby";

  /**
   * Conservative client-side hold applied after leaving a game session. The server does not report a
   * lease deadline: it grants a 30 second session lease in {@code OnlineStore} (a SQL literal, not a
   * shared constant), so the client waits that long plus a safety margin before offering a rejoin.
   * The server still revalidates every join; this only avoids a predictable lease refusal.
   */
  public static final long CLIENT_LEASE_HOLD_MILLIS = 35_000L;

  public enum Mode {
    /** Existing behavior: connect straight away (or show the connecting state) with the profile. */
    DIRECT,
    /** Show the online lobby first; the player picks the character. */
    LOBBY
  }

  private OnlineLobbyLaunch() {}

  /** True for an explicit opt-in from the property, environment or argument list. */
  public static boolean requested(String property, String environment, String[] args) {
    return truthy(property) || truthy(environment) || (args != null && Arrays.asList(args).contains(ARGUMENT));
  }

  /** The arguments without the lobby switch, so {@code args[0]} stays the endpoint. */
  public static String[] positional(String[] args) {
    if (args == null) return new String[0];
    return Arrays.stream(args).filter(argument -> !ARGUMENT.equals(argument)).toArray(String[]::new);
  }

  /**
   * Chooses the launch mode. The lobby needs the private profile because it supplies the account
   * key; requesting it without one is a configuration error rather than a silent fallback.
   */
  public static Mode select(boolean lobbyRequested, boolean profileLoaded) {
    if (!lobbyRequested) return Mode.DIRECT;
    if (!profileLoaded) throw new IllegalArgumentException("The online lobby needs an online profile");
    return Mode.LOBBY;
  }

  /** Epoch milliseconds before which the lobby refuses a rejoin after the session ended at {@code returnedAtMillis}. */
  public static long leaseDeadline(long returnedAtMillis) {
    return returnedAtMillis + CLIENT_LEASE_HOLD_MILLIS;
  }

  private static boolean truthy(String value) {
    if (value == null) return false;
    return switch (value.strip().toLowerCase(Locale.ROOT)) {
      case "1", "true", "yes", "on" -> true;
      default -> false;
    };
  }
}
