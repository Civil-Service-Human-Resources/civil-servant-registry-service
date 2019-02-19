package uk.gov.cshr.civilservant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@Entity
public class CivilServant implements RegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @ManyToOne
    @OneToMany(cascade = CascadeType.DETACH)
    private OrganisationalUnit organisationalUnit;

    @ManyToOne
    private Grade grade;

    @ManyToOne
    private Profession profession;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Interest> interests = new HashSet<>();

    @OneToOne
    @JsonIgnore
    private Identity identity;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Profession> otherAreasOfWork = new HashSet<>();

    @JsonIgnore
    @ManyToOne
    private CivilServant lineManager;

    private String supplier;


    public CivilServant() {
    }

    public CivilServant(Identity identity) {
        this.identity = identity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Identity getIdentity() {
        return identity;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Optional<OrganisationalUnit> getOrganisationalUnit() {
        return Optional.ofNullable(organisationalUnit);
    }

    public void setOrganisationalUnit(OrganisationalUnit organisationalUnit) {
        this.organisationalUnit = organisationalUnit;
    }

    public Optional<Grade> getGrade() {
        return Optional.ofNullable(grade);
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public Optional<Profession> getProfession() {
        return Optional.ofNullable(profession);
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }


    public Set<Interest> getInterests() { return unmodifiableSet(interests);  }

    public void setInterests(Set<Interest> interests) {
        this.interests.clear();
        if (interests != null) {
            this.interests.addAll(interests);
        }
    }

    public Set<Profession> getOtherAreasOfWork() {
        return otherAreasOfWork;
    }

    public void setOtherAreasOfWork(Set<Profession> otherAreasOfWork) {
        this.otherAreasOfWork.clear();
        if (otherAreasOfWork != null) {
            this.otherAreasOfWork.addAll(otherAreasOfWork);
        }
    }

    public Optional<CivilServant> getLineManager() {
        return Optional.ofNullable(lineManager);
    }

    public void setLineManager(CivilServant lineManager) {
        this.lineManager = lineManager;
    }

    public Optional<String> getSupplier() {
        return Optional.of(supplier);
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    @JsonProperty
    public String getLineManagerName() {
       if (lineManager != null) {
           return lineManager.getFullName();
       }
       return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CivilServant that = (CivilServant) o;

        return new EqualsBuilder()
                .append(identity, that.identity)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(identity)
                .toHashCode();
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
                .append("interests", interests)
                .append("identity", identity)
                .append("lineManager", lineManager)
                .toString();
    }

}