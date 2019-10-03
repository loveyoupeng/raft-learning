package org.loveyoupeng.raft;

public interface Member {

  String getAgentId();

  void responseToVote(final String agentId, final long term, final boolean granted);
}
