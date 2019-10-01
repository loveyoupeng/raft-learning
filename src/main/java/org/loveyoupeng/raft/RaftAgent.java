package org.loveyoupeng.raft;

import java.util.Optional;
import org.agrona.concurrent.Agent;

public interface RaftAgent extends Agent {


  String getAgentId();

  Role getRole();

  long getCurrentTerm();

  Optional<String> getVotedFor();

  long getLastLogTerm();

  long getLastLogIndex();
}
