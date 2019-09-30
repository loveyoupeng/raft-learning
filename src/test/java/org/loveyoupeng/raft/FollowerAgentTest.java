package org.loveyoupeng.raft;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertSame;
import static org.loveyoupeng.raft.Role.Candidate;
import static org.loveyoupeng.raft.TestUtils.waitFollowerTimeout;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.loveyoupeng.raft.impl.command.AppendEntriesRequest;

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
    agent.onStart();
  }


  @Test(timeout = 2 * ELECTION_TIMEOUT_UPPER_BOUND)
  public void test_follower_time_out() throws Exception {
    final long start = System.currentTimeMillis();
    waitFollowerTimeout(agent);

    assertSame(Candidate, agent.getRole());
    final long duration = System.currentTimeMillis() - start;
    assertTrue(duration >= ELECTION_TIMEOUT_LOWER_BOUND - 5);
    assertTrue(duration <= ELECTION_TIMEOUT_UPPER_BOUND + 5);
  }

  @Test
  public void test_follower_handles_heart_beat() {
    final AppendEntriesRequest request = mock(AppendEntriesRequest.class);
    when(request.getAgentId()).thenReturn(MEMBER_ID_1);
    // TODO : adding test cases for heart beat
  }
}
