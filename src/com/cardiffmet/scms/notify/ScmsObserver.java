package com.cardiffmet.scms.notify;

/**
 * Observer interface for campus events (behavioural Observer pattern).
 */
@FunctionalInterface
public interface ScmsObserver {

    void onEvent(ScmsEvent event);
}
