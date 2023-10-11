package uk.gov.cshr.civilservant.domain;

import lombok.Builder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;

@Builder
@Entity
public class Profession extends SelfReferencingEntity<Profession> {
  public Profession() {}

  public Profession(String name) {
    this.name = name;
  }

  @Override
  public Profession getParent() {
    return parent;
  }

  @Override
  public void setParent(Profession parent) {
    this.parent = parent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Profession that = (Profession) o;

    return new EqualsBuilder().append(name, that.name).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("name", name)
        .append("parent", parent)
        .toString();
  }
}
