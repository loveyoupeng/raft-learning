package org.loveyoupeng.raft;

import org.agrona.concurrent.Agent;

public interface RaftAgent extends Agent {


  String getAgentId();

  Role getRole();

  long getCurrentTerm();
}
