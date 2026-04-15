package com.ftgo.common;

/**
 * Thrown when an entity receives a state transition that is not valid for its current state.
 */
public class UnsupportedStateTransitionException extends RuntimeException {

    /**
     * Creates a new exception indicating the current state that rejected the transition.
     *
     * @param state the current state
     */
    public UnsupportedStateTransitionException(Enum state) {
        super("current state: " + state);
    }
}
