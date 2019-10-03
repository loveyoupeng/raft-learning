package org.loveyoupeng.raft.impl;

import java.util.HashMap;
import java.util.Map;

public class Election {

  private final String agentId;
  private final Map<String, Boolean> votes;
  private boolean requestSend;

  public Election(final String agentId) {
    this.agentId = agentId;
    requestSend = false;
    votes = new HashMap<>();
  }

  public boolean isRequestSend() {
    return requestSend;
  }

  public void setRequestSend(final boolean requestSend) {
    this.requestSend = requestSend;
  }

  public void clear() {
    requestSend = false;
    votes.clear();
    votes.put(agentId, true);
  }


}
