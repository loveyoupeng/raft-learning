package org.loveyoupeng.raft.impl.command;

public interface CommandHandler {

  void process(final AppendEntriesRequest request);
}
