package uk.gov.cshr.civilservant.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.*;

@MappedSuperclass
public abstract class SelfReferencingEntity<T> implements RegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String name;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonBackReference
    T parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    Set<T> children = new HashSet<>();

    public abstract T getParent();

    public abstract void setParent(T parent);

    public Set<T> getChildren() {
        return children;
    };

    public List<T> getChildrenAsList() {
        return new ArrayList<>(getChildren());
    };

    public void setChildren(Set<T> children) {
        this.children = children;
    };

    public void setChildren(List<T> children) {
        this.children = new LinkedHashSet<>(children);
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
