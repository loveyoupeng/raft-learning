package org.loveyoupeng.raft.impl;

import org.loveyoupeng.raft.Member;

public class DefaultMember implements Member {

  private String agentId;

  public DefaultMember(final String agentId) {
    this.agentId = agentId;
  }

  @Override
  public String getAgentId() {
    return agentId;
  }

  @Override
  public void responseToVote(final String agentId, final long term, final boolean granted) {
    // TODO : to be implemented
  }

  @Override
  public void requestForVote(final String agentId, final long proposedTerm, final long lastLogTerm,
      final long lastLogIndex) {
    // TODO : to be implemented
  }
}
