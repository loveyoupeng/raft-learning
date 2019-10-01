package org.loveyoupeng.raft.impl.command;

public interface Command {

  boolean accept(final CommandHandler handler);
}
