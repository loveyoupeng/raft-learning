package org.loveyoupeng.raft.impl.command;

public interface AppendEntriesRequest extends Command {

  default void accept(final CommandHandler handler) {
    handler.process(this);
  }

  String getAgentId();
}
