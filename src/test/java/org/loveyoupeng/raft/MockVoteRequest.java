package org.loveyoupeng.raft;

import org.loveyoupeng.raft.impl.command.VoteRequest;

public class MockVoteRequest implements VoteRequest {

  private final String agentId;
  private final long proposedTerm;
  private final long lastLogTerm;
  private final long lastLogIndex;

  public MockVoteRequest(final String agentId, final long proposedTerm, final long lastLogTerm,
      final long lastLogIndex) {
    this.agentId = agentId;
    this.proposedTerm = proposedTerm;
    this.lastLogTerm = lastLogTerm;
    this.lastLogIndex = lastLogIndex;
  }

  @Override
  public String getAgentId() {
    return agentId;
  }

  @Override
  public long getProposedTerm() {
    return proposedTerm;
  }

  @Override
  public long getLastLogTerm() {
    return lastLogTerm;
  }

  @Override
  public String toString() {
    return "MockVoteRequest{" +
        "agentId='" + agentId + '\'' +
        ", proposedTerm=" + proposedTerm +
        ", lastLogTerm=" + lastLogTerm +
        ", lastLogIndex=" + lastLogIndex +
        '}';
  }

  @Override
  public long getLastLogIndex() {
    return lastLogIndex;
  }
}
