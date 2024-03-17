package io.memoria.atom.eventsourcing.command.exceptions;

import io.memoria.atom.eventsourcing.command.Command;

public class UnknownCommand extends CommandException {
  private static final String MESSAGE = "Unknown Command: %s[%s] implementation";

  protected UnknownCommand(Command command) {
    super(MESSAGE.formatted(command.getClass().getSimpleName(), command.meta()), command);
  }

  public static UnknownCommand of(Command command) {
    return new UnknownCommand(command);
  }
}