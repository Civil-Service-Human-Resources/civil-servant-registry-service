package uk.gov.cshr.civilservant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organisationalUnit")
    private List<CivilServant> civilServants;

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
        this.civilServants = organisationalUnit.getCivilServants();
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

    @PreRemove
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
    public void removeDomain(Domain domain) {
        this.domains.remove(domain);
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
    public Collection<String> getAgencyDomains() {
        Collection<String> domains = Collections.emptyList();
        if (this.agencyToken != null) {
            domains = this.agencyToken.getAgencyDomains().stream().map(AgencyDomain::getDomain).collect(Collectors.toList());
        }
        return domains;
    }

    @JsonIgnore
    public Collection<String> getDomainStrings() {
        return this.domains.stream().map(Domain::getDomain).collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean doesDomainExistInAgencyToken(String domain) {
        return getAgencyDomains().contains(domain);
    }

    @JsonIgnore
    public boolean doesDomainExistInLinkedDomains(String domain) {
        return getDomainStrings().contains(domain);
    }

    @JsonIgnore
    public boolean doesDomainExist(String domain) {
        return doesDomainExistInLinkedDomains(domain) || doesDomainExistInAgencyToken(domain);
    }

    @JsonIgnore
    public String getValidDomainsString() {
        return String.format("Linked domains: %s | Agency domains: %s", getDomainStrings(), getAgencyDomains());
    }

    @JsonIgnore
    public List<CivilServant> getCivilServants() {
        return this.civilServants;
    }
}
