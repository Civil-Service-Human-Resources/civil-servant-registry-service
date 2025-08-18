package uk.gov.cshr.civilservant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class CivilServant implements RegistryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String fullName;

  @ManyToOne(fetch = FetchType.LAZY)
  @OneToMany(cascade = CascadeType.DETACH)
  private OrganisationalUnit organisationalUnit;

  @ManyToOne(fetch = FetchType.LAZY)
  private Grade grade;

  @ManyToOne(fetch = FetchType.LAZY)
  private Profession profession;

  @ManyToMany(fetch = FetchType.LAZY)
  private Set<Interest> interests;

  @OneToOne(cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Identity identity;

  @ManyToMany(fetch = FetchType.LAZY)
  private Set<Profession> otherAreasOfWork;

  @ManyToMany(fetch = FetchType.LAZY)
  private Set<OrganisationalUnit> otherOrganisationalUnits;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  private CivilServant lineManager;

  public CivilServant(Identity identity) {
    this.identity = identity;
  }

  public Optional<OrganisationalUnit> getOrganisationalUnit() {
    return Optional.ofNullable(organisationalUnit);
  }

  public Optional<Grade> getGradeOptional() {
    return Optional.ofNullable(grade);
  }

  public Optional<Profession> getProfession() {
    return Optional.ofNullable(profession);
  }

  public Set<Interest> getInterests() {
    if(interests != null) {
      return unmodifiableSet(interests);
    }
    return null;
  }

  public void setInterests(Set<Interest> interests) {
    if (interests != null) {
      this.interests = new HashSet<>();
      this.interests.addAll(interests);
    }
  }

  public void setOtherAreasOfWork(Set<Profession> otherAreasOfWork) {
    if (otherAreasOfWork != null) {
      this.otherAreasOfWork = new HashSet<>();
      this.otherAreasOfWork.addAll(otherAreasOfWork);
    }
  }

  public void setOtherOrganisationalUnits(Set<OrganisationalUnit> otherOrganisationalUnits) {
    if(otherOrganisationalUnits != null) {
      this.otherOrganisationalUnits = new HashSet<>();
      this.otherOrganisationalUnits.addAll(otherOrganisationalUnits);
    }
  }

  public Set<OrganisationalUnit> getOtherOrganisationalUnits() {
    return otherOrganisationalUnits == null ? new HashSet<>() : otherOrganisationalUnits;
  }

  public Optional<CivilServant> getLineManager() {
    return Optional.ofNullable(lineManager);
  }

  public void setLineManager(CivilServant lineManager) {
    this.lineManager = lineManager;
  }

  @JsonIgnore
  public boolean hasOtherOrganisation(Long otherOrganisationId) {
    return getOtherOrganisationalUnits().stream().anyMatch(o -> o.getId().equals(otherOrganisationId));
  }

  @JsonProperty
  public String getLineManagerName() {
    if (lineManager != null) {
      return lineManager.getFullName();
    }
    return null;
  }

  @JsonProperty
  public String getLineManagerUid() {
    if (lineManager != null) {
      return lineManager.getIdentity().getUid();
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    CivilServant that = (CivilServant) o;

    return new EqualsBuilder().append(identity, that.identity).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(identity).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("fullName", fullName)
        .append("organisationalUnit", organisationalUnit)
        .append("grade", grade)
        .append("profession", profession)
        .append("otherAreasOfWork", otherAreasOfWork)
        .append("otherOrganisationalUnits", otherOrganisationalUnits)
        .append("interests", interests)
        .append("identity", identity)
        .append("lineManager", lineManager)
        .toString();
  }
}
