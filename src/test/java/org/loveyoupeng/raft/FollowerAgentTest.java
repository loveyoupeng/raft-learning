package org.loveyoupeng.raft;

import static org.junit.Assert.assertSame;
import static org.loveyoupeng.raft.Role.Candidate;
import static org.loveyoupeng.raft.Role.Follower;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;

public class FollowerAgentTest {


  public static final String MEMBER_ID_1 = "memberId1";
  public static final String MEMBER_ID_2 = "memberId2";
  public static final String AGENT_ID = "agentId";
  public static final long ELECTION_TIMEOUT_LOWER_BOUND = 500L;
  public static final long ELECTION_TIMEOUT_UPPER_BOUND = 1000L;
  private Collection<Member> members;
  private RaftAgent agent;

  @Before
  public void before() {
    members = new ArrayList<>();
    members.add(MemberBuilder.memberBuilder().memberId(MEMBER_ID_1).build());
    members.add(MemberBuilder.memberBuilder().memberId(MEMBER_ID_2).build());
    agent = RaftAgentBuilder.builder().agentId(AGENT_ID).electionTimeoutLowerBound(
        ELECTION_TIMEOUT_LOWER_BOUND)
        .electionTimeoutUpperBound(ELECTION_TIMEOUT_UPPER_BOUND).addMembers(members).build();
  }


  @Test(timeout = 2 * ELECTION_TIMEOUT_UPPER_BOUND)
  public void test_follower_time_out() throws Exception {
    while (Follower == agent.getRole()) {
      agent.doWork();
    }

    assertSame(Candidate, agent.getRole());
  }
}
