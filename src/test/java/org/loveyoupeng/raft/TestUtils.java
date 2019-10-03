package org.loveyoupeng.raft;

import static org.loveyoupeng.raft.Role.Follower;

import java.lang.reflect.Field;
import org.loveyoupeng.raft.impl.DefaultRaftAgent;

public class TestUtils {

  static void waitFollowerTimeout(final RaftAgent agent) throws Exception {
    while (Follower == agent.getRole()) {
      agent.doWork();
    }
  }

  static void setAgentTerm(final RaftAgent agent, final long term)
      throws NoSuchFieldException, IllegalAccessException {
    final Field currentTermField = DefaultRaftAgent.class.getDeclaredField("currentTerm");
    currentTermField.setAccessible(true);
    currentTermField.setLong(agent, term);
    currentTermField.setAccessible(false);
  }
}
