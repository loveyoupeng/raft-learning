package org.loveyoupeng.raft;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import org.agrona.IoUtil;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.loveyoupeng.raft.impl.command.Command;

public class RaftAgentTest {

  private Path logPath;

  @Before
  public void before() throws Exception {
    logPath = Files
        .createTempDirectory("raft-agent-test-");
  }

  @After
  public void after() throws Exception {
    IoUtil.delete(logPath.toFile(), true);
  }

  @Test
  public void test_init_raft_agent() {
    final Collection<Member> members = new ArrayList<>();
    members.add(MemberBuilder.memberBuilder().memberId("memberId1").build());
    members.add(MemberBuilder.memberBuilder().memberId("memberId2").build());
    final RaftAgent agent = RaftAgentBuilder.builder().agentId("agentId")
        .inputChannel(new ManyToManyConcurrentArrayQueue<Command>(32))
        .addMembers(members).electionTimeoutLowerBound(1000L).electionTimeoutUpperBound(1500L)
        .logPath(logPath)
        .build();

    assertEquals("agentId", agent.getAgentId());
    assertEquals(Role.Follower, agent.getRole());
    assertEquals("agentId(Follower)", agent.roleName());
    assertEquals(0L, agent.getCurrentTerm());
    assertEquals(0L, agent.getLastLogTerm());
    assertEquals(0L, agent.getLastLogIndex());
  }

  @Test
  public void test_agent_load_log() throws Exception {
    final ChronicleQueue queue = ChronicleQueue.single(logPath.toAbsolutePath().toString());
    final ExcerptAppender appender = queue.acquireAppender();
    appender.writeDocument(writer -> writer.write("term").fixedInt64(0));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(1));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(1));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(2));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(3));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(4));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(4));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(4));
    appender.writeDocument(writer -> writer.write("term").fixedInt64(6));

    final Collection<Member> members = new ArrayList<>();
    members.add(MemberBuilder.memberBuilder().memberId("memberId1").build());
    members.add(MemberBuilder.memberBuilder().memberId("memberId2").build());
    final RaftAgent agent = RaftAgentBuilder.builder().agentId("agentId")
        .inputChannel(new ManyToManyConcurrentArrayQueue<Command>(32))
        .addMembers(members).electionTimeoutLowerBound(1000L).electionTimeoutUpperBound(1500L)
        .logPath(logPath)
        .build();

    assertEquals("agentId", agent.getAgentId());
    assertEquals(Role.Follower, agent.getRole());
    assertEquals("agentId(Follower)", agent.roleName());
    assertEquals(6L, agent.getCurrentTerm());
    assertEquals(6L, agent.getLastLogTerm());
    assertEquals(8L, agent.getLastLogIndex());
  }
}
