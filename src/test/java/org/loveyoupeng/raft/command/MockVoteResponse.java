package org.loveyoupeng.raft.command;

import org.loveyoupeng.raft.impl.command.VoteResponse;

public class MockVoteResponse implements VoteResponse {

  private final String agentId;
  private final long currentTerm;
  private final boolean granted;

  public MockVoteResponse(final String agentId, final long currentTerm, final boolean granted) {
    this.agentId = agentId;
    this.currentTerm = currentTerm;
    this.granted = granted;
  }

  @Override
  public String getAgentId() {
    return agentId;
  }

  @Override
  public long getCurrentTerm() {
    return currentTerm;
  }

  @Override
  public boolean isGranted() {
    return granted;
  }
}
