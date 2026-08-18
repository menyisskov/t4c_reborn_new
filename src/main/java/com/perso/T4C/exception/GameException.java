package com.perso.T4C.exception;

public class GameException extends RuntimeException {
  public GameException(String message) {
    super(message);
  }

  public GameException(String message, Throwable cause) {
    super(message, cause);
  }

  public GameException(Throwable cause) {
    super("A game error occurred", cause);
  }
}
