package io.memoria.atom.eventsourcing.exceptions.command;

import io.memoria.atom.eventsourcing.Command;
import io.memoria.atom.eventsourcing.State;
import io.memoria.atom.eventsourcing.exceptions.ESException;

public class MismatchingCommandState extends ESException {
  protected MismatchingCommandState(String msg) {
    super(msg);
  }

  public static MismatchingCommandState of(Command command) {
    var msg = "The command's stateId:%s doesn't belong here".formatted(command.shardKey());
    return new MismatchingCommandState(msg);
  }

  public static MismatchingCommandState of(Command command, State state) {
    var msg = "The Command's stateId:%s doesn't match stateId:%s".formatted(command.shardKey(), state.shardKey());
    return new MismatchingCommandState(msg);
  }
}
