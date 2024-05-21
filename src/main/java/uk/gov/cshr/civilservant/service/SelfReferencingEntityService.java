package uk.gov.cshr.civilservant.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.SelfReferencingEntity;
import uk.gov.cshr.civilservant.dto.DtoEntity;
import uk.gov.cshr.civilservant.dto.factory.DtoFactory;
import uk.gov.cshr.civilservant.repository.SelfReferencingEntityRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
public abstract class SelfReferencingEntityService<
    T extends SelfReferencingEntity<T>, K extends DtoEntity> {
  private final DtoFactory<K, T> dtoFactory;
  private SelfReferencingEntityRepository<T> repository;

  SelfReferencingEntityService(
      SelfReferencingEntityRepository<T> repository, DtoFactory<K, T> dtoFactory) {
    this.repository = repository;
    this.dtoFactory = dtoFactory;
  }

  public List<T> getTree() {
    List<T> list = this.getParents();
    sortList(list);
    return list;
  }

  private void sortList(List<T> list) {
    list.forEach(entity ->
            {
              if (entity.hasChildren()) {
                List<T> children = entity.getChildrenAsList();
                children.sort(Comparator.comparing(T::getName, String.CASE_INSENSITIVE_ORDER));
                sortList(children);
              }
            }
    );
  }

  /** This will return all parent entities with any children as a list */
  @Transactional(readOnly = true)
  public List<T> getParents() {
    return repository
        .findAllByOrderByNameAsc()
        .stream()
        .filter(org -> !org.hasParent())
        .sorted(Comparator.comparing(T::getName, String.CASE_INSENSITIVE_ORDER))
        .collect(Collectors.toList());
  }

  /** This will return all Dto entities as a list, sorted by formattedName */
  @Transactional(readOnly = true)
  public List<K> getListSortedByValue() {
    return repository
        .findAll()
        .stream()
        .map(o -> dtoFactory.create(o))
        .sorted(Comparator.comparing(K::getFormattedName, String.CASE_INSENSITIVE_ORDER))
        .collect(Collectors.toList());
  }
}
