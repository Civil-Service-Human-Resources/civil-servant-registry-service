package uk.gov.cshr.civilservant.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import jakarta.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Identity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 36)
  private String uid;

  protected Identity() {}

  public Identity(String uid) {
    checkArgument(isNotBlank(uid));
    this.uid = uid;
  }

  public Long getId() {
    return id;
  }

  public String getUid() {
    return uid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Identity identity = (Identity) o;

    return new EqualsBuilder().append(uid, identity.uid).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(uid).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("id", id).append("uid", uid).toString();
  }
}
