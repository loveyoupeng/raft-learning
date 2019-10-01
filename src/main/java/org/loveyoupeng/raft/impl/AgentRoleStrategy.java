package org.loveyoupeng.raft.impl;

import org.loveyoupeng.raft.Role;
import org.loveyoupeng.raft.impl.command.CommandHandler;

public interface AgentRoleStrategy extends CommandHandler {

  Role getRole();

  int doWork() throws Exception;

  void initWork();

}
