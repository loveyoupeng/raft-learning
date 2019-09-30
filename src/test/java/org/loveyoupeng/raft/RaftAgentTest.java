package org.loveyoupeng.raft;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;

public class RaftAgentTest {

  @Test
  public void test_init_raft_agent() {
    final Collection<Member> members = new ArrayList<>();
    members.add(MemberBuilder.memberBuilder().memberId("memberId1").build());
    members.add(MemberBuilder.memberBuilder().memberId("memberId2").build());
    final RaftAgent agent = RaftAgentBuilder.builder().agentId("agentId")
        .addMembers(members).build();

    assertEquals("agentId", agent.getAgentId());
    assertEquals(Role.Follower, agent.getRole());
  }
}
