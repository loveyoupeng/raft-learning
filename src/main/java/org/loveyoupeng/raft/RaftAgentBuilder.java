package org.loveyoupeng.raft;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.loveyoupeng.raft.impl.DefaultRaftAgent;


public class RaftAgentBuilder {

  private String agentId;
  private Set<Member> members = new HashSet<>();
  private long electionTimeoutLowerBound;
  private long electionTimeoutUpperBound;

  public static RaftAgentBuilder builder() {
    return new RaftAgentBuilder();
  }

  public RaftAgentBuilder agentId(final String agentId) {
    this.agentId = agentId;
    return this;
  }

  public RaftAgentBuilder addMembers(final Collection<Member> members) {
    this.members.addAll(members);
    return this;
  }

  public RaftAgent build() {
    return new DefaultRaftAgent(agentId, members, electionTimeoutLowerBound,
        electionTimeoutUpperBound);
  }

  public RaftAgentBuilder electionTimeoutLowerBound(final long electionTimeoutLowerBound) {
    this.electionTimeoutLowerBound = electionTimeoutLowerBound;
    return this;
  }


  public RaftAgentBuilder electionTimeoutUpperBound(final long electionTimeoutUpperBound) {
    this.electionTimeoutUpperBound = electionTimeoutUpperBound;
    return this;
  }
}
