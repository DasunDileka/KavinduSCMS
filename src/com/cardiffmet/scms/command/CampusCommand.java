package com.cardiffmet.scms.command;

/**
 * Behavioural <strong>Command</strong>: encapsulates an action as an object ({@link #execute()}),
 * allowing UI layers to trigger domain operations without embedding workflow details.
 */
@FunctionalInterface
public interface CampusCommand {

    void execute();
}
