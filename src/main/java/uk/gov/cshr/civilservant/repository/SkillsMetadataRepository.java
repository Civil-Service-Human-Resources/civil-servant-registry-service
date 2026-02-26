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
    "where (:syncTimestampLte is null or cssm.syncTimestamp <= :syncTimestampLte)")
    Page<CivilServantSkillsMetadata> get(Pageable paging, LocalDateTime syncTimestampLte);

    List<CivilServantSkillsMetadata> getAllByCivilServant_Identity_UidIn(List<String> uids);

}
