package com.perso.T4C.exception;

/**
 * Generic exception class for all game-related errors.
 * This exception wraps checked exceptions and provides a centralized error handling mechanism.
 */
public class GameException extends RuntimeException {

    /**
     * Constructs a GameException with the specified detail message.
     *
     * @param message the detail message
     */
    public GameException(String message) {
        super(message);
    }

    /**
     * Constructs a GameException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause (which is saved for later retrieval by getCause() method)
     */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a GameException with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by getCause() method)
     */
    public GameException(Throwable cause) {
        super("A game error occurred", cause);
    }
}

