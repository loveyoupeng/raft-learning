package org.loveyoupeng.raft.impl;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.loveyoupeng.raft.Role.Follower;

import java.util.Map;
import java.util.Set;
import org.loveyoupeng.raft.Member;
import org.loveyoupeng.raft.RaftAgent;
import org.loveyoupeng.raft.Role;

public class DefaultRaftAgent implements RaftAgent {

  private final String agentId;
  private final Map<String, Member> members;
  private Role role;

  public DefaultRaftAgent(final String agentId, final Set<Member> members) {
    this.agentId = agentId;
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
}
