package com.cardiffmet.scms.notify;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Behavioural <strong>Observer</strong> — subject: implementations of {@link ScmsObserver} subscribe
 * and receive {@link ScmsEvent}s when campus state changes (the repository publishes events).
 */
public final class NotificationCenter {

    private final List<ScmsObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(ScmsObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(ScmsObserver observer) {
        observers.remove(observer);
    }

    public void publish(ScmsEvent event) {
        for (ScmsObserver observer : observers) {
            observer.onEvent(event);
        }
    }
}
