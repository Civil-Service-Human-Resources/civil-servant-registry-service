package uk.gov.cshr.civilservant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.CivilServantSkillsMetadata;

@Repository
public interface SkillsMetadataRepository extends JpaRepository<CivilServantSkillsMetadata, Long>, PagingAndSortingRepository<CivilServantSkillsMetadata, Long> {

    @Query(value = "select cssm " +
            "from CivilServantSkillsMetadata cssm " +
    "where cssm.syncTimestamp is null")
    Page<CivilServantSkillsMetadata> getByNotSynced(Pageable paging);

    @Query(value = "select cssm " +
            "from CivilServantSkillsMetadata cssm " +
            "where cssm.syncTimestamp is not null")
    Page<CivilServantSkillsMetadata> getBySynced(Pageable paging);

    @Query(value = "select cssm from CivilServantSkillsMetadata cssm ")
    Page<CivilServantSkillsMetadata> getAll(Pageable paging);

//    @Query(value = "select CivilServantSkillsMetadata " +
//            "from CivilServantSkillsMetadata cssm " +
//            "where ?1 is null or IF((cssm.syncTimestamp is null), 'false', 'true') = ?1 ")
//    Page<CivilServantSkillsMetadata> getByNotSynced(@Nullable Boolean synced, Pageable paging);

}
