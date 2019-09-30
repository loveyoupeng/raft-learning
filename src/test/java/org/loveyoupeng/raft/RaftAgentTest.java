package org.loveyoupeng.raft;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.junit.Test;
import org.loveyoupeng.raft.impl.command.Command;

public class RaftAgentTest {

  @Test
  public void test_init_raft_agent() {
    final Collection<Member> members = new ArrayList<>();
    members.add(MemberBuilder.memberBuilder().memberId("memberId1").build());
    members.add(MemberBuilder.memberBuilder().memberId("memberId2").build());
    final RaftAgent agent = RaftAgentBuilder.builder().agentId("agentId")
        .inputChannel(new ManyToManyConcurrentArrayQueue<Command>(32))
        .addMembers(members).electionTimeoutLowerBound(1000L).electionTimeoutUpperBound(1500L)
        .build();

    assertEquals("agentId", agent.getAgentId());
    assertEquals(Role.Follower, agent.getRole());
    assertEquals("agentId(Follower)", agent.roleName());
    assertEquals(0L, agent.getCurrentTerm());
  }
}
