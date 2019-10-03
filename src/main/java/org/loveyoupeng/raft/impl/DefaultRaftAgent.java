package org.loveyoupeng.raft.impl;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.TailerDirection;
import net.openhft.chronicle.wire.DocumentContext;
import org.agrona.concurrent.QueuedPipe;
import org.loveyoupeng.raft.Member;
import org.loveyoupeng.raft.RaftAgent;
import org.loveyoupeng.raft.Role;
import org.loveyoupeng.raft.impl.command.Command;
import org.loveyoupeng.raft.impl.command.CommandHandler;

public class DefaultRaftAgent implements RaftAgent {

  private final String agentId;
  private final QueuedPipe<Command> inputChannel;
  private final long electionTimeoutLowerBound;
  private final long electionTimeoutUpperBound;
  private final Map<String, Member> members;
  private final AgentRoleStrategy followerAgentStrategy;
  private final AgentRoleStrategy candidateAgentStrategy;
  private final Path logPath;
  private final ExcerptAppender appender;
  private final ExcerptTailer tailer;
  private final ChronicleQueue logQueue;
  private long electionTimeout;
  private long currentTerm;
  private AgentRoleStrategy roleStrategy;
  private long activityTimestamp;
  private String votedFor;
  private long lastLogTerm;
  private long lastLogIndex;

  public DefaultRaftAgent(final String agentId,
      final QueuedPipe<Command> inputChannel,
      final Path logPath, final Set<Member> members,
      final long electionTimeoutLowerBound, final long electionTimeoutUpperBound) {
    this.agentId = agentId;
    this.inputChannel = inputChannel;
    this.currentTerm = 0L;
    // TODO : last log progress should rebuild from persistent logs
    this.lastLogIndex = 0L;
    this.lastLogTerm = 0L;
    this.electionTimeoutLowerBound = electionTimeoutLowerBound;
    this.electionTimeoutUpperBound = electionTimeoutUpperBound;
    this.members =
        members.stream().collect(toMap(Member::getAgentId, identity()));
    followerAgentStrategy = new FollowerAgentStrategy(this);
    candidateAgentStrategy = new CandidateAgentStrategy(this);
    this.roleStrategy = this.followerAgentStrategy;
    votedFor = null;
    this.logPath = logPath;
    logQueue = ChronicleQueue.single(logPath.toAbsolutePath().toString());
    appender = logQueue.acquireAppender();
    tailer = logQueue.createTailer();
    initFromLog();
  }

  private void initFromLog() {
    final DocumentContext documentContext = tailer.direction(TailerDirection.BACKWARD).toEnd()
        .readingDocument();
    if (documentContext.isPresent()) {
      this.lastLogTerm = documentContext.wire().read("term").int64();
      this.lastLogIndex = tailer.index() & ((1L << 32) - 1);
      this.currentTerm = lastLogTerm;
    }
    tailer.direction(TailerDirection.FORWARD).toEnd();
  }

  public Path getLogPath() {
    return logPath;
  }

  @Override
  public String getAgentId() {
    return agentId;
  }

  @Override
  public Role getRole() {
    return roleStrategy.getRole();
  }

  @Override
  public long getCurrentTerm() {
    return currentTerm;
  }

  @Override
  public Optional<String> getVotedFor() {
    return votedFor == null ? Optional.empty() : Optional.of(votedFor);
  }

  @Override
  public void onStart() {
    roleStrategy.initWork();
  }

  @Override
  public int doWork() throws Exception {
    return roleStrategy.doWork();
  }

  @Override
  public String roleName() {
    return agentId + "(" + roleStrategy.getRole() + ")";
  }

  public void touchTimestamp() {
    activityTimestamp = System.currentTimeMillis();
  }

  public boolean electionTimeout() {
    return activityTimestamp + electionTimeout < System.currentTimeMillis();
  }

  public void resetElectionTimeout() {
    this.electionTimeout = ThreadLocalRandom.current()
        .nextLong(electionTimeoutLowerBound, electionTimeoutUpperBound);
  }

  public void switchToCandidate() {
    roleStrategy = candidateAgentStrategy;
  }

  public int process(final CommandHandler handler) {
    int result = 0;
    Command command;
    while ((command = inputChannel.poll()) != null) {
      result++;
      if (command.accept(handler)) {
        break;
      }
    }
    return result;
  }

  public void grantVoteFor(final String candidateId, final long proposedTerm) {
    this.votedFor = candidateId;
    if (proposedTerm > currentTerm) {
      currentTerm = proposedTerm;
    }
    members.get(candidateId).responseToVote(agentId, currentTerm, true);

  }

  @Override
  public long getLastLogTerm() {
    return lastLogTerm;
  }

  @Override
  public long getLastLogIndex() {
    return lastLogIndex;
  }

  public void clearVotedFor() {
    votedFor = null;
  }

  public void rejectVoteFor(final String candidateId, final long proposedTerm) {
    members.get(candidateId).responseToVote(agentId, currentTerm, false);
  }
}
