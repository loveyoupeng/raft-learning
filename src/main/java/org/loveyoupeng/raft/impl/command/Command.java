package org.loveyoupeng.raft.impl.command;

public interface Command {

  void accept(final CommandHandler handler);


}
