package org.loveyoupeng.raft.impl;

import org.loveyoupeng.raft.Role;
import org.loveyoupeng.raft.impl.command.AppendEntriesRequest;
import org.loveyoupeng.raft.impl.command.VoteRequest;
import org.loveyoupeng.raft.impl.command.VoteResponse;

public class CandidateAgentStrategy implements AgentRoleStrategy {

  private final DefaultRaftAgent agent;

  CandidateAgentStrategy(final DefaultRaftAgent agent) {
    this.agent = agent;
  }

  @Override
  public Role getRole() {
    return Role.Candidate;
  }

  @Override
  public int doWork() {
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

  @Override
  public boolean process(final VoteResponse voteResponse) {
    return agent.responseToVote(voteResponse.getAgentId(), voteResponse.getCurrentTerm(),
        voteResponse.isGranted());
  }
}
