package org.loveyoupeng.raft;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertSame;
import static org.loveyoupeng.raft.Role.Candidate;
import static org.loveyoupeng.raft.TestUtils.waitFollowerTimeout;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
import org.loveyoupeng.raft.impl.command.Command;

public class CandidateAgentTest {

  public static final String AGENT_ID = "agentId";
  public static final String MEMBER_ID_1 = "memberId1";
  public static final String MEMBER_ID_2 = "memberId2";
  public static final String MEMBER_ID_3 = "memberId3";
  public static final String MEMBER_ID_4 = "memberId4";
  private QueuedPipe<Command> inputChannel;
  private Path logPath;
  private RaftAgent agent;
  private Map<String, Member> members;

  @Before
  public void before() throws Exception {
    logPath = Files.createTempDirectory("candidate-agent-test-");

    inputChannel = new OneToOneConcurrentArrayQueue<Command>(32);
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
  public void after() throws Exception {
    IoUtil.delete(logPath.toFile(), true);
  }

  @Test
  public void test_sending_request_for_vote_request() throws Exception {
    waitFollowerTimeout(agent);

    assertEquals(1, agent.doWork());
    assertEquals(1L, agent.getCurrentTerm());
    members.forEach((key, member) -> {
      verify(member, times(1)).requestForVote(AGENT_ID, 1L, 0L, 0L);
    });

    assertEquals(0, agent.doWork());
    assertEquals(1L, agent.getCurrentTerm());
    assertSame(Candidate, agent.getRole());
    members.values()
        .forEach(member -> verify(member, times(1))
            .requestForVote(anyString(), anyLong(), anyLong(), anyLong()));
  }

}
