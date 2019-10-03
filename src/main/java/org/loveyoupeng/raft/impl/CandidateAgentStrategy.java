package org.loveyoupeng.raft.impl;

import org.loveyoupeng.raft.Role;
import org.loveyoupeng.raft.impl.command.AppendEntriesRequest;
import org.loveyoupeng.raft.impl.command.VoteRequest;

public class CandidateAgentStrategy implements AgentRoleStrategy {

  private final DefaultRaftAgent agent;

  public CandidateAgentStrategy(final DefaultRaftAgent agent) {
    this.agent = agent;
  }

  @Override
  public Role getRole() {
    return Role.Candidate;
  }

  @Override
  public int doWork() throws Exception {
    int work = agent.electionWork();
    work += agent.process(this);
    return work;
  }

  @Override
  public void initWork() {
    agent.touchTimestamp();
    agent.initElection();
  }

  @Override
  public boolean process(final AppendEntriesRequest request) {
    //TODO : to be implemented
    return false;
  }

  @Override
  public boolean process(final VoteRequest request) {
    //TODO : to be implemented
    return false;
  }
}
