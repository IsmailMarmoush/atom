package io.memoria.atom.eventsourcing.event.exceptions;


import io.memoria.atom.eventsourcing.event.Event;

public class EventException extends Exception {
  private final Event event;

  protected EventException(String msg, Event event) {
    super(msg);
    this.event = event;
  }

  public Event getEvent() {
    return event;
  }
}
