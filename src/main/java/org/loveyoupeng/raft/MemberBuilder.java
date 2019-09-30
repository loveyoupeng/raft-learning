package org.loveyoupeng.raft;

import org.loveyoupeng.raft.impl.DefaultMember;

public class MemberBuilder {

  private String memberId;

  public static MemberBuilder memberBuilder() {
    return new MemberBuilder();
  }

  public MemberBuilder memberId(final String memberId) {
    this.memberId = memberId;
    return this;
  }

  public Member build() {
    return new DefaultMember(memberId);
  }
}
