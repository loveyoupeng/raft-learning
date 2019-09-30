package org.loveyoupeng.raft;

import static org.loveyoupeng.raft.Role.Follower;

public class TestUtils {

  static void waitFollowerTimeout(final RaftAgent agent) throws Exception {
    while (Follower == agent.getRole()) {
      agent.doWork();
    }
  }
}
