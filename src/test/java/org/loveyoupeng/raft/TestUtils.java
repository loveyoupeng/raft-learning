package org.loveyoupeng.raft;

import static org.loveyoupeng.raft.Role.Follower;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.loveyoupeng.raft.impl.DefaultRaftAgent;
import org.loveyoupeng.raft.impl.Election;

class TestUtils {

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

  static void reloadLog(final RaftAgent agent) throws Exception {
    final Method method = DefaultRaftAgent.class
        .getDeclaredMethod("initFromLog");
    method.setAccessible(true);

    method.invoke(agent);

    method.setAccessible(false);
  }

  static Election getElection(final RaftAgent agent) throws Exception {
    final Field electionField = DefaultRaftAgent.class.getDeclaredField("election");
    electionField.setAccessible(true);
    final Election election = (Election) electionField.get(agent);
    electionField.setAccessible(false);
    return election;
  }
}
