package org.loveyoupeng.raft;

import static java.util.Optional.empty;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.loveyoupeng.raft.Role.Candidate;
import static org.loveyoupeng.raft.TestUtils.getElection;
import static org.loveyoupeng.raft.TestUtils.waitFollowerTimeout;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.agrona.IoUtil;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.agrona.concurrent.QueuedPipe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.loveyoupeng.raft.command.MockVoteResponse;
import org.loveyoupeng.raft.impl.Election;
import org.loveyoupeng.raft.impl.command.Command;
import org.loveyoupeng.raft.impl.command.VoteResponse;

public class CandidateAgentTest {

  private static final String AGENT_ID = "agentId";
  private static final String MEMBER_ID_1 = "memberId1";
  private static final String MEMBER_ID_2 = "memberId2";
  private static final String MEMBER_ID_3 = "memberId3";
  private static final String MEMBER_ID_4 = "memberId4";
  private QueuedPipe<Command> inputChannel;
  private Path logPath;
  private RaftAgent agent;
  private Map<String, Member> members;

  @Before
  public void before() throws Exception {
    logPath = Files.createTempDirectory("candidate-agent-test-");

    inputChannel = new OneToOneConcurrentArrayQueue<>(32);
    members = ImmutableMap.<String, Member>builder()
        .put(MEMBER_ID_1, mock(Member.class))
        .put(MEMBER_ID_2, mock(Member.class))
        .put(MEMBER_ID_3, mock(Member.class))
        .put(MEMBER_ID_4, mock(Member.class))
        .build();

    members.forEach((key, value) -> when(value.getAgentId()).thenReturn(key));
    agent = RaftAgentBuilder.builder().agentId(AGENT_ID).inputChannel(inputChannel).logPath(logPath)
        .addMembers(members.values()).electionTimeoutLowerBound(1000L)
        .electionTimeoutUpperBound(1500L)
        .build();

    agent.onStart();
  }

  @After
  public void after() {
    IoUtil.delete(logPath.toFile(), true);
  }

  @Test
  public void test_sending_request_for_vote_request() throws Exception {
    waitFollowerTimeout(agent);

    assertEquals(1, agent.doWork());
    assertEquals(1L, agent.getCurrentTerm());
    members.forEach((key, member) -> {
      verify(member, times(1)).requestForVote(AGENT_ID, 1L, 0L, 0L);
      reset(member);
    });

    assertEquals(0, agent.doWork());
    assertEquals(1L, agent.getCurrentTerm());
    assertSame(Candidate, agent.getRole());
    members.values()
        .forEach(member -> verify(member, never())
            .requestForVote(anyString(), anyLong(), anyLong(), anyLong()));
  }

  @Test
  public void test_handling_response_to_vote_granted() throws Exception {
    waitFollowerTimeout(agent);
    assertEquals(1, agent.doWork());
    assertEquals(0, agent.doWork());

    final VoteResponse response = new MockVoteResponse(MEMBER_ID_1, 1L, true);

    assertTrue(inputChannel.offer(response));
    assertEquals(1, agent.doWork());
    assertTrue(inputChannel.isEmpty());

    final Election election = getElection(agent);
    assertFalse(election.isWin());
    assertTrue(election.isGranted(AGENT_ID).orElse(false));
    assertTrue(election.isGranted(MEMBER_ID_1).orElse(false));
    assertSame(empty(), election.isGranted(MEMBER_ID_2));
    assertSame(empty(), election.isGranted(MEMBER_ID_3));
    assertSame(empty(), election.isGranted(MEMBER_ID_4));
  }

}
