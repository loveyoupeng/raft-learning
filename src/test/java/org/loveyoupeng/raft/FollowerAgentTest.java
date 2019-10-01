package org.loveyoupeng.raft;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.loveyoupeng.raft.Role.Candidate;
import static org.loveyoupeng.raft.TestUtils.waitFollowerTimeout;

import java.util.ArrayList;
import java.util.Collection;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.agrona.concurrent.QueuedPipe;
import org.junit.Before;
import org.junit.Test;
import org.loveyoupeng.raft.impl.command.Command;
import org.loveyoupeng.raft.impl.command.VoteRequest;

public class FollowerAgentTest {


  public static final String MEMBER_ID_1 = "memberId1";
  public static final String MEMBER_ID_2 = "memberId2";
  public static final String AGENT_ID = "agentId";
  public static final long ELECTION_TIMEOUT_LOWER_BOUND = 500L;
  public static final long ELECTION_TIMEOUT_UPPER_BOUND = 1000L;
  private Collection<Member> members;
  private RaftAgent agent;
  private QueuedPipe<Command> inputChannel;

  @Before
  public void before() {
    members = new ArrayList<>();
    members.add(MemberBuilder.memberBuilder().memberId(MEMBER_ID_1).build());
    members.add(MemberBuilder.memberBuilder().memberId(MEMBER_ID_2).build());
    inputChannel = new OneToOneConcurrentArrayQueue<>(16);
    agent = RaftAgentBuilder.builder().agentId(AGENT_ID).inputChannel(inputChannel)
        .electionTimeoutLowerBound(
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
  public void test_follower_request_for_vote_granted() throws Exception {
    final VoteRequest request = new MockVoteRequest(MEMBER_ID_1, 1L, 0L, 0L);

    inputChannel.offer(request);
    assertEquals(1, agent.doWork());

    assertTrue(inputChannel.isEmpty());
    assertEquals(MEMBER_ID_1, agent.getVotedFor().orElseThrow());
    assertEquals(1L, agent.getCurrentTerm());
  }
}
