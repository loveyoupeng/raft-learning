package org.loveyoupeng.raft.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Election {

  private final String agentId;
  private final Map<String, Boolean> votes;
  private boolean requestSend;

  Election(final String agentId) {
    this.agentId = agentId;
    requestSend = false;
    votes = new HashMap<>();
  }

  boolean isRequestSend() {
    return requestSend;
  }

  void setRequestSend(final boolean requestSend) {
    this.requestSend = requestSend;
  }

  void clear() {
    requestSend = false;
    votes.clear();
    votes.put(agentId, true);
  }


  public boolean isWin() {
    return false;
  }

  public Optional<Boolean> isGranted(final String agentId) {
    return votes.containsKey(agentId) ? of(votes.get(agentId)) : empty();
  }

  void responseToVote(final String agentId, final long currentTerm, final boolean granted) {
    votes.put(agentId, granted);
  }
}
