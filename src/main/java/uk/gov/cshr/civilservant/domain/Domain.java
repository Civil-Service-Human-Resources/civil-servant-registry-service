package uk.gov.cshr.civilservant.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "domains")
@JsonIgnoreProperties(value = {"organisationalUnits"})
public class Domain implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private LocalDateTime createdTimestamp;

    @ManyToMany(mappedBy = "domains")
    private List<OrganisationalUnit> organisationalUnits = new ArrayList<>();

    public Domain(String domain) {
        this.domain = domain;
    }

    @PrePersist
    public void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
    }

}
