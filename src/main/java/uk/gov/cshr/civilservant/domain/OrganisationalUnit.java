package uk.gov.cshr.civilservant.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public OrganisationalUnit(OrganisationalUnit organisationalUnit) {
        this.id = organisationalUnit.getId();
        this.code = organisationalUnit.getCode();
        this.name = organisationalUnit.getName();
        this.parent = organisationalUnit.getParent();
        this.children = organisationalUnit.getChildren();
        this.abbreviation = organisationalUnit.getAbbreviation();
        this.setPaymentMethods(organisationalUnit.getPaymentMethods());
        this.agencyToken = organisationalUnit.agencyToken;
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

    @Override
    public List<OrganisationalUnit> getChildren() {
        return Collections.checkedList(children, OrganisationalUnit.class);
    }

    @Override
    public void setChildren(List<OrganisationalUnit> children) {
        this.children = Collections.checkedList(children, OrganisationalUnit.class);
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
}
