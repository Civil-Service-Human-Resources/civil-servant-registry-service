package uk.gov.cshr.civilservant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.persistence.*;
@Data
@Entity
public class AgencyDomain implements RegistryEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false, length = 150)
    private String domain;

    public AgencyDomain() {
    }

    public AgencyDomain(String domain) {
        this.domain = domain;
    }
}
