package org.loveyoupeng.raft.impl;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.loveyoupeng.raft.Role.Candidate;
import static org.loveyoupeng.raft.Role.Follower;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.loveyoupeng.raft.Member;
import org.loveyoupeng.raft.RaftAgent;
import org.loveyoupeng.raft.Role;

public class DefaultRaftAgent implements RaftAgent {

  private final String agentId;
  private final long electionTimeoutLowerBound;
  private final long electionTimeoutUpperBound;
  private final Map<String, Member> members;
  private Role role;
  private long electionTimeouut;

  public DefaultRaftAgent(final String agentId, final Set<Member> members,
      final long electionTimeoutLowerBound, final long electionTimeoutUpperBound) {
    this.agentId = agentId;
    this.electionTimeoutLowerBound = electionTimeoutLowerBound;
    this.electionTimeoutUpperBound = electionTimeoutUpperBound;
    this.role = Follower;
    this.members =
        members.stream().collect(toMap(Member::getAgentId, identity()));

  }

  @Override
  public String getAgentId() {
    return agentId;
  }

  @Override
  public Role getRole() {
    return role;
  }

  @Override
  public void onStart() {
    electionTimeouut = System.currentTimeMillis() + nextTimeout();
  }

  private long nextTimeout() {
    return ThreadLocalRandom.current()
        .nextLong(electionTimeoutLowerBound, electionTimeoutUpperBound);
  }

  @Override
  public int doWork() throws Exception {
    if (Follower == role) {
      if (System.currentTimeMillis() > electionTimeouut) {
        role = Candidate;
      }
    }

    return 0;
  }

  @Override
  public String roleName() {
    return agentId + "(" + role + ")";
  }
}
