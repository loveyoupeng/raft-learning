package org.loveyoupeng.raft.impl.command;

public interface CommandHandler {

  boolean process(final AppendEntriesRequest request);

  boolean process(final VoteRequest request);

  boolean process(final VoteResponse voteResponse);
}
