package org.loveyoupeng.raft.impl.command;

public interface VoteResponse extends Command {

  String getAgentId();

  long getCurrentTerm();

  boolean isGranted();

  @Override
  default boolean accept(final CommandHandler handler) {
    return handler.process(this);
  }
}
