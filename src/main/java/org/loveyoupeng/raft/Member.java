package org.loveyoupeng.raft;

public interface Member {

  String getAgentId();

  void responseToVote(final String agentId, final long term, final boolean granted);

  void requestForVote(final String agentId, final long proposedTerm, final long lastLogTerm,
      final long lastLogIndex);
}
