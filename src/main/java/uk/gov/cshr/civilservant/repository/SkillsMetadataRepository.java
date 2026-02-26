package uk.gov.cshr.civilservant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.CivilServantSkillsMetadata;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SkillsMetadataRepository extends JpaRepository<CivilServantSkillsMetadata, Long>, PagingAndSortingRepository<CivilServantSkillsMetadata, Long> {

    @Query(value = "select cssm " +
            "from CivilServantSkillsMetadata cssm " +
            "where cssm.syncTimestamp is null")
    Page<CivilServantSkillsMetadata> getByNotSynced(Pageable paging);

    @Query(value = "select cssm " +
            "from CivilServantSkillsMetadata cssm " +
            "where cssm.syncTimestamp is not null " +
            "and (:lastSyncedLte is null or cssm.syncTimestamp <= :lastSyncedLte)")
    Page<CivilServantSkillsMetadata> getBySynced(Pageable paging, LocalDateTime lastSyncedLte);

    List<CivilServantSkillsMetadata> getAllByCivilServant_Identity_UidIn(List<String> uids);

}
