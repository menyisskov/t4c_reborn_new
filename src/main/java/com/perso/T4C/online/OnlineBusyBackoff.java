package com.perso.T4C.online;

/**
 * Bounded exponential backoff with jitter for a definite "503 busy" refusal of a start-up call.
 * Pure arithmetic so the schedule can be tested without a network.
 */
final class OnlineBusyBackoff {
  /** Retries after the first attempt, so a call is tried at most {@code MAX_RETRIES + 1} times. */
  static final int MAX_RETRIES = 6;
  static final long BASE_MILLIS = 250;
  static final long CAP_MILLIS = 3000;

  private OnlineBusyBackoff() {}

  /** Upper bound before jitter for the given zero-based retry: 250, 500, 1000, 2000, 3000, 3000. */
  static long ceilingMillis(int retry) {
    if (retry < 0) throw new IllegalArgumentException("Negative retry");
    return retry >= 6 ? CAP_MILLIS : Math.min(CAP_MILLIS, BASE_MILLIS << retry);
  }

  /** Equal jitter: half the ceiling plus a random share of the other half. {@code unit} is in [0,1). */
  static long delayMillis(int retry, double unit) {
    if (!(unit >= 0 && unit < 1)) throw new IllegalArgumentException("Jitter must be in [0,1)");
    long ceiling = ceilingMillis(retry);
    return ceiling / 2 + (long) (unit * (ceiling - ceiling / 2));
  }

  /** Only a definite busy refusal, which the server issues before it applies the request. */
  static boolean isBusy(int status, String reason) {
    return status == 503 && "busy".equals(reason);
  }
}
