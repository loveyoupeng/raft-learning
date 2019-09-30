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
}
