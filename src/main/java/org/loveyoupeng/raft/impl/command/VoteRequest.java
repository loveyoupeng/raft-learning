package org.loveyoupeng.raft.impl.command;

public interface VoteRequest extends Command {

  @Override
  default boolean accept(final CommandHandler handler) {
    return handler.process(this);
  }

  String getAgentId();

  long getProposedTerm();

  long getLastLogTerm();

  long getLastLogIndex();

}
