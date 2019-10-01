package org.loveyoupeng.raft.impl;

import static org.loveyoupeng.raft.Role.Follower;

import org.loveyoupeng.raft.Role;
import org.loveyoupeng.raft.impl.command.AppendEntriesRequest;
import org.loveyoupeng.raft.impl.command.VoteRequest;

public class FollowerAgentStrategy implements AgentRoleStrategy {

  private final DefaultRaftAgent agent;

  public FollowerAgentStrategy(final DefaultRaftAgent agent) {
    this.agent = agent;
  }


  @Override
  public Role getRole() {
    return Follower;
  }

  @Override
  public int doWork() throws Exception {
    if (agent.electionTimeout()) {
      agent.switchToCandidate();
      return 1;
    }
    return agent.process(this);
  }

  @Override
  public void initWork() {
    agent.clearVotedFor();
    agent.resetElectionTimeout();
    agent.touchTimestamp();
  }

  @Override
  public boolean process(final AppendEntriesRequest request) {
    // TODO : to be implemented
    return false;
  }

  @Override
  public boolean process(final VoteRequest request) {
    if (request.getProposedTerm() >= agent.getCurrentTerm()) {
      if (agent.getVotedFor().orElse(request.getAgentId()).equals(request.getAgentId())) {
        if (agent.getLastLogTerm() < request.getLastLogTerm() || (
            agent.getLastLogTerm() == request.getLastLogTerm() && agent.getLastLogIndex() <= request
                .getLastLogIndex())) {
          agent.voteFor(request.getAgentId(), request.getProposedTerm());
        }
      }
    } else {
      //TODO : to be implemented
    }
    return true;
  }
}
