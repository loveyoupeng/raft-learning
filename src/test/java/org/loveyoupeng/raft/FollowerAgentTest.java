package org.loveyoupeng.raft;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.loveyoupeng.raft.Role.Candidate;
import static org.loveyoupeng.raft.TestUtils.setAgentTerm;
import static org.loveyoupeng.raft.TestUtils.waitFollowerTimeout;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import org.agrona.IoUtil;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.agrona.concurrent.QueuedPipe;
import org.junit.After;
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
  private RaftAgent agent;
  private QueuedPipe<Command> inputChannel;
  private Member member1;
  private Member member2;
  private Path logPath;

  @Before
  public void before() throws Exception {
    member1 = mock(Member.class);
    member2 = mock(Member.class);
    when(member1.getAgentId()).thenReturn(MEMBER_ID_1);
    when(member2.getAgentId()).thenReturn(MEMBER_ID_2);
    logPath = Files.createTempDirectory("follower-agent-test-");

    inputChannel = new OneToOneConcurrentArrayQueue<>(16);
    agent = RaftAgentBuilder.builder().agentId(AGENT_ID).inputChannel(inputChannel).logPath(logPath)
        .electionTimeoutLowerBound(
            ELECTION_TIMEOUT_LOWER_BOUND)
        .electionTimeoutUpperBound(ELECTION_TIMEOUT_UPPER_BOUND)
        .addMembers(asList(member1, member2)).build();
    agent.onStart();
  }

  @After
  public void after() {
    IoUtil.delete(logPath.toFile(), true);
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
    verify(member1, times(1)).responseToVote(AGENT_ID, 1L, true);
  }

  @Test
  public void test_follower_request_for_rejected_due_to_lower_term() throws Exception {
    setAgentTerm(agent, 3L);
    final VoteRequest request = new MockVoteRequest(MEMBER_ID_1, 2L, 0L, 0L);

    inputChannel.offer(request);
    assertEquals(1, agent.doWork());

    assertTrue(inputChannel.isEmpty());
    assertFalse(agent.getVotedFor().isPresent());
    assertEquals(3L, agent.getCurrentTerm());
    verify(member1, times(1)).responseToVote(AGENT_ID, 3L, false);
  }

  @Test
  public void test_follower_request_for_rejected_due_to_already_voted() throws Exception {
    final VoteRequest winnerRequest = new MockVoteRequest(MEMBER_ID_1, 2L, 0L, 0L);

    inputChannel.offer(winnerRequest);
    assertEquals(1, agent.doWork());

    assertTrue(inputChannel.isEmpty());
    assertEquals(MEMBER_ID_1, agent.getVotedFor().orElseThrow());
    assertEquals(2L, agent.getCurrentTerm());
    verify(member1, times(1)).responseToVote(AGENT_ID, 2L, true);

    final VoteRequest loserRequest = new MockVoteRequest(MEMBER_ID_2, 2L, 0L, 0L);

    inputChannel.offer(loserRequest);
    assertEquals(1, agent.doWork());

    assertTrue(inputChannel.isEmpty());
    assertEquals(MEMBER_ID_1, agent.getVotedFor().orElseThrow());
    assertEquals(2L, agent.getCurrentTerm());
    verify(member1, times(1)).responseToVote(AGENT_ID, 2L, false);

  }

  @Test
  public void test_follower_request_for_rejected_due_to_larger_logger_term() throws Exception {
    try (final ChronicleQueue log = ChronicleQueue.single(logPath.toAbsolutePath().toString())) {
      final ExcerptAppender appender = log.acquireAppender();
      appender.writeDocument(writer -> writer.write("term").fixedInt64(0));
      appender.writeDocument(writer -> writer.write("term").fixedInt64(3));

      TestUtils.reloadLog(agent);

      final VoteRequest request = new MockVoteRequest(MEMBER_ID_1, 4L, 2L, 3L);
      inputChannel.offer(request);

      assertEquals(1, agent.doWork());

      assertTrue(inputChannel.isEmpty());
      assertFalse(agent.getVotedFor().isPresent());
      assertEquals(4L, agent.getCurrentTerm());
      verify(member1, times(1)).responseToVote(AGENT_ID, 4L, false);
    }
  }

  @Test
  public void test_follower_request_for_rejected_due_to_larger_logger_index() throws Exception {
    try (final ChronicleQueue log = ChronicleQueue.single(logPath.toAbsolutePath().toString())) {
      final ExcerptAppender appender = log.acquireAppender();
      appender.writeDocument(writer -> writer.write("term").fixedInt64(0));
      appender.writeDocument(writer -> writer.write("term").fixedInt64(2));
      appender.writeDocument(writer -> writer.write("term").fixedInt64(2));
      appender.writeDocument(writer -> writer.write("term").fixedInt64(2));
      appender.writeDocument(writer -> writer.write("term").fixedInt64(3));

      TestUtils.reloadLog(agent);

      final VoteRequest request = new MockVoteRequest(MEMBER_ID_1, 4L, 3L, 3L);
      inputChannel.offer(request);

      assertEquals(1, agent.doWork());

      assertTrue(inputChannel.isEmpty());
      assertFalse(agent.getVotedFor().isPresent());
      assertEquals(4L, agent.getCurrentTerm());
      verify(member1, times(1)).responseToVote(AGENT_ID, 4L, false);
    }
  }


}
