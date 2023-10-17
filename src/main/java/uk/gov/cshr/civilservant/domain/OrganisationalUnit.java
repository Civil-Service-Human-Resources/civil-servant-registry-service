package uk.gov.cshr.civilservant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.PreDestroy;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class OrganisationalUnit extends SelfReferencingEntity<OrganisationalUnit> {

    private static final long serialVersionUID = 1L;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Column(unique = true, nullable = false, length = 20)
    private String abbreviation;

    @Column(name = "payment_methods")
    private String paymentMethods = PaymentMethod.PURCHASE_ORDER.toString();

    @OneToOne(cascade = {CascadeType.ALL})
    private AgencyToken agencyToken;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(
            name = "organisational_unit_domains",
            joinColumns = @JoinColumn(name = "organisational_unit_id" , referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "domain_id" , referencedColumnName = "id"),
    foreignKey = @ForeignKey(name = "organisational_unit_id_FK"))
    @OrderBy("domain")
    private Set<Domain> domains = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(nullable = false)
    private LocalDateTime updatedTimestamp;

    public OrganisationalUnit(OrganisationalUnit organisationalUnit) {
        this.id = organisationalUnit.getId();
        this.code = organisationalUnit.getCode();
        this.name = organisationalUnit.getName();
        this.parent = organisationalUnit.getParent();
        this.children = organisationalUnit.getChildren();
        this.abbreviation = organisationalUnit.getAbbreviation();
        this.setPaymentMethods(organisationalUnit.getPaymentMethods());
        this.agencyToken = organisationalUnit.agencyToken;
        this.domains = organisationalUnit.getDomains();
        this.createdTimestamp = organisationalUnit.getCreatedTimestamp();
        this.updatedTimestamp = organisationalUnit.getUpdatedTimestamp();
    }

    public OrganisationalUnit() {
    }

    public OrganisationalUnit(String name, String code, String abbreviation) {
        this.name = name;
        this.code = code;
        this.abbreviation = abbreviation;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getParentId() {
        if (this.hasParent()) {
            return this.parent.getId();
        } else {
            return null;
        }
    }

    @Override
    public OrganisationalUnit getParent() {
        return parent;
    }

    @Override
    public void setParent(OrganisationalUnit parent) {
        this.parent = parent;
    }

    public List<String> getPaymentMethods() {
        if (null == paymentMethods || paymentMethods.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(paymentMethods.split(",")));
    }

    public void setPaymentMethods(List<String> paymentMethods) {
        this.paymentMethods = String.join(",", paymentMethods);
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public boolean hasParent() {
        return getParent() != null;
    }

    @Override
    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    public AgencyToken getAgencyToken() {
        return agencyToken;
    }

    public void setAgencyToken(AgencyToken agencyToken) {
        this.agencyToken = agencyToken;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    @PrePersist
    public void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();
    }

    @PreDestroy
    public void onDelete() {
        this.domains.clear();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedTimestamp = LocalDateTime.now();
    }

    public Set<Domain> getDomains() {
        return domains;
    }

    @JsonIgnore
    public void addDomain(Domain domain) {
        this.domains.add(domain);
    }

    @JsonIgnore
    public List<OrganisationalUnit> getDescendantsAsFlatList() {
        List<OrganisationalUnit> flatList = getHierarchyAsFlatList();
        flatList.remove(0);
        return flatList;
    }

    @JsonIgnore
    public List<OrganisationalUnit> getHierarchyAsFlatList() {
        ArrayList<OrganisationalUnit> hierarchy = new ArrayList<>(Collections.singletonList(this));
        this.children.forEach(c -> hierarchy.addAll(c.getHierarchyAsFlatList()));
        return hierarchy;
    }

    @JsonIgnore
    public boolean doesDomainExist(String domain) {
        return this.domains.stream().anyMatch(d -> d.getDomain().equals(domain));
    }
}
