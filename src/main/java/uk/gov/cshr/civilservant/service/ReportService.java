package uk.gov.cshr.civilservant.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.dto.CivilServantDto;
import uk.gov.cshr.civilservant.dto.CivilServantReportDto;
import uk.gov.cshr.civilservant.dto.SkillsReportsDto;
import uk.gov.cshr.civilservant.dto.factory.CivilServantDtoFactory;
import uk.gov.cshr.civilservant.exception.UserNotFoundException;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;

@Service
public class ReportService {

  private final CivilServantRepository civilServantRepository;
  private final CivilServantDtoFactory civilServantDtoFactory;
  private final QuizService quizService;

  public ReportService(
      CivilServantRepository civilServantRepository,
      CivilServantDtoFactory civilServantDtoFactory,
      QuizService quizService) {
    this.civilServantRepository = civilServantRepository;
    this.civilServantDtoFactory = civilServantDtoFactory;
    this.quizService = quizService;
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantDto> getCivilServantMapByUserOrganisation(String userId) {
    CivilServant user =
        civilServantRepository
            .findByIdentity(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

    if (user.getOrganisationalUnit().isPresent()) {
      return civilServantRepository
          .findAllByOrganisationalUnit(user.getOrganisationalUnit().get())
          .stream()
          .collect(
              Collectors.toMap(
                  civilServant -> civilServant.getIdentity().getUid(),
                  civilServantDtoFactory::create));
    }
    return Collections.emptyMap();
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantDto> getCivilServantMapByUserProfession(String userId) {
    CivilServant user =
        civilServantRepository
            .findByIdentity(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

    if (user.getProfession().isPresent()) {
      return civilServantRepository
          .findAllByProfession(user.getProfession().get())
          .stream()
          .collect(
              Collectors.toMap(
                  civilServant -> civilServant.getIdentity().getUid(),
                  civilServantDtoFactory::create));
    }

    return Collections.emptyMap();
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantDto> getCivilServantMap() {
    return civilServantRepository
        .findAll()
        .stream()
        .collect(
            Collectors.toMap(
                civilServant -> civilServant.getIdentity().getUid(),
                civilServantDtoFactory::create));
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantReportDto> getCivilServantMapNormalised() {
    return civilServantRepository
        .findAllNormalised()
        .stream()
        .collect(
            Collectors.toMap(CivilServantReportDto::getUid, civilServantDto -> civilServantDto));
  }

  public Map<String, CivilServantReportDto> getCivilServantMapForUidsNormalised(List<String> uids) {
    return civilServantRepository
            .findAllByUidsNormalised(uids)
            .stream()
            .collect(
                    Collectors.toMap(CivilServantReportDto::getUid, civilServantDto -> civilServantDto));
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantReportDto> getCivilServantMapNormalisedWithCodes() {
    return civilServantRepository
        .findAllNormalisedWithCodes()
        .stream()
        .collect(
            Collectors.toMap(CivilServantReportDto::getUid, civilServantDto -> civilServantDto));
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantReportDto> getCivilServantMapByUserOrganisationNormalised(
      String userId) {
    CivilServant user =
        civilServantRepository
            .findByIdentity(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

    if (user.getOrganisationalUnit().isPresent()) {
      return civilServantRepository
          .findAllByOrganisationNormalised(user.getOrganisationalUnit().get())
          .stream()
          .collect(Collectors.toMap(CivilServantReportDto::getUid, civilServant -> civilServant));
    }
    return Collections.emptyMap();
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantReportDto> getCivilServantMapByUserProfessionNormalised(
      String userId) {
    CivilServant user =
        civilServantRepository
            .findByIdentity(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

    if (user.getProfession().isPresent()) {
      return civilServantRepository
          .findAllByProfessionNormalised(user.getProfession().get())
          .stream()
          .collect(Collectors.toMap(CivilServantReportDto::getUid, civilServant -> civilServant));
    }

    return Collections.emptyMap();
  }

  @Transactional(readOnly = true)
  public Map<String, CivilServantReportDto> getCivilServantMapByOrganisationCodeNormalised(
      String organisationCode) {
    List<CivilServantReportDto> allByOrganisationCodeNormalised =
        civilServantRepository.findAllByOrganisationCodeNormalised(organisationCode);
    return allByOrganisationCodeNormalised
        .stream()
        .collect(Collectors.toMap(CivilServantReportDto::getUid, civilServant -> civilServant));
  }

  @Transactional(readOnly = true)
  public List<SkillsReportsDto> getReportForSuperAdmin(LocalDateTime from, LocalDateTime to) {
    return quizService.getReportForSuperAdmin(from, to);
  }

  @Transactional(readOnly = true)
  public List<SkillsReportsDto> getReportForOrganisationAdmin(
      long organisationId, LocalDateTime from, LocalDateTime to) {
    return quizService.getReportForOrganisationAdmin(organisationId, from, to);
  }

  @Transactional(readOnly = true)
  public List<SkillsReportsDto> getReportForProfessionAdmin(
      long professionId, LocalDateTime from, LocalDateTime to) {
    return quizService.getReportForProfessionAdmin(professionId, from, to);
  }

  @Transactional(readOnly = true)
  public List<SkillsReportsDto> getReportForProfessionReporter(
      long organisationId, long professionId, LocalDateTime from, LocalDateTime to) {
    return quizService.getReportForProfessionReporter(organisationId, professionId, from, to);
  }
}
