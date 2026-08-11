package uk.gov.cshr.civilservant.controller;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.civilservant.controller.models.GetCivilServantsForUidsParams;
import uk.gov.cshr.civilservant.domain.Roles;
import uk.gov.cshr.civilservant.dto.CivilServantDto;
import uk.gov.cshr.civilservant.dto.CivilServantReportDto;
import uk.gov.cshr.civilservant.dto.SkillsReportsDto;
import uk.gov.cshr.civilservant.mapping.RoleMapping;
import uk.gov.cshr.civilservant.service.ReportService;
import uk.gov.cshr.civilservant.utils.strategy.HeaderColumnNameAndOrderMappingStrategy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {
  private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @RoleMapping("ORGANISATION_REPORTER")
  @GetMapping("/civilServants")
  public ResponseEntity<Map<String, CivilServantReportDto>> listAllCivilServantsByOrganisation(
      Principal principal) {
    return ResponseEntity.ok(
        reportService.getCivilServantMapByUserOrganisationNormalised(principal.getName()));
  }

  @RoleMapping("CSHR_REPORTER")
  @GetMapping("/civilServants")
  public ResponseEntity<Map<String, CivilServantReportDto>> listAllCivilServants() {
    return ResponseEntity.ok(reportService.getCivilServantMapNormalised());
  }

  @GetMapping(value = "/civil-servants-for-uids", params = "uids")
  public ResponseEntity<Map<String, CivilServantReportDto>> listCivilServantsForUids(GetCivilServantsForUidsParams params) {
    return ResponseEntity.ok(reportService.getCivilServantMapForUidsNormalised(params));
  }

  @GetMapping("/civilServants/code")
  public ResponseEntity<Map<String, CivilServantReportDto>> listAllCivilServantsWithCodes() {
    return ResponseEntity.ok(reportService.getCivilServantMapNormalisedWithCodes());
  }

  @GetMapping(value = "/civilServants", params = "code")
  public ResponseEntity<Map<String, CivilServantReportDto>> listAllCivilServantsByOrganisation(
      @RequestParam("code") String organisationalUnitCode) {
    return ResponseEntity.ok(
        reportService.getCivilServantMapByOrganisationCodeNormalised(organisationalUnitCode));
  }

  @GetMapping("/civilServants")
  public ResponseEntity<Map<String, CivilServantDto>> unauthorised(Principal principal) {
    // default to returning a 403 if none of the above roles are found.
    LOG.debug(String.format("Unauthorised. Required role not found in %s", principal));
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @GetMapping(value = "/skills/report-for-super-admin", produces = "text/csv")
  @RoleMapping({Roles.CSHR_REPORTER, Roles.LEARNING_MANAGER})
  public void listAllSkillsResultsAcrossOrganisations(
      @RequestParam(value = "professionId", required = false) Long professionId,
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      HttpServletResponse response)
      throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {

    LocalDateTime fromDate = LocalDateTime.of(from, LocalTime.MIN);

    LocalDateTime toDate = LocalDateTime.of(to, LocalTime.MAX);

    List<SkillsReportsDto> reportsDtoList =
        professionId != null
            ? reportService.getReportForProfessionAdmin(professionId, fromDate, toDate)
            : reportService.getReportForSuperAdmin(fromDate, toDate);

    writeCsvResponse(response, reportsDtoList);
  }

  @GetMapping(value = "/skills/report-for-department-admin", produces = "text/csv")
  @RoleMapping({Roles.ORGANISATION_REPORTER, Roles.ORGANISATION_AUTHOR})
  public void listAllSkillsResultsForYourOrgAndProfession(
      @RequestParam(value = "professionId", required = false) Long professionId,
      @RequestParam("organisationId") long organisationId,
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      HttpServletResponse response)
      throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
    LocalDateTime fromDate = LocalDateTime.of(from, LocalTime.MIN);

    LocalDateTime toDate = LocalDateTime.of(to, LocalTime.MAX);

    List<SkillsReportsDto> reportsDtoList =
        professionId != null
            ? reportService.getReportForProfessionReporter(
                organisationId, professionId, fromDate, toDate)
            : reportService.getReportForOrganisationAdmin(organisationId, fromDate, toDate);

    writeCsvResponse(response, reportsDtoList);
  }

  @GetMapping(value = "/skills/report-for-profession-admin", produces = "text/csv")
  @RoleMapping({Roles.PROFESSION_AUTHOR, Roles.PROFESSION_REPORTER})
  public void listAllSkillsResultsByProfession(
      @RequestParam("professionId") long professionId,
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      HttpServletResponse response)
      throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
    LocalDateTime fromDate = LocalDateTime.of(from, LocalTime.MIN);

    LocalDateTime toDate = LocalDateTime.of(to, LocalTime.MAX);

    List<SkillsReportsDto> reportsDtoList =
        reportService.getReportForProfessionAdmin(professionId, fromDate, toDate);

    writeCsvResponse(response, reportsDtoList);
  }

  @GetMapping(value = "/skills/report-for-profession-reporter", produces = "text/csv")
  @RoleMapping({Roles.PROFESSION_AUTHOR, Roles.PROFESSION_REPORTER})
  public void listAllSkillsResultsByProfessionAndOrg(
      @RequestParam("organisationId") long organisationId,
      @RequestParam("professionId") long professionId,
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      HttpServletResponse response)
      throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
    LocalDateTime fromDate = LocalDateTime.of(from, LocalTime.MIN);

    LocalDateTime toDate = LocalDateTime.of(to, LocalTime.MAX);

    List<SkillsReportsDto> reportsDtoList =
        reportService.getReportForProfessionReporter(
            organisationId, professionId, fromDate, toDate);

    writeCsvResponse(response, reportsDtoList);
  }

  private void writeCsvResponse(HttpServletResponse response, List<SkillsReportsDto> reportsDtoList)
      throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
    try (Writer writer = response.getWriter()) {
      StatefulBeanToCsv beanToCsv =
          new StatefulBeanToCsvBuilder(writer)
              .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
              .withMappingStrategy(
                      new HeaderColumnNameAndOrderMappingStrategy<>(
                              SkillsReportsDto.class
                      ))
              .build();

      beanToCsv.write(reportsDtoList);
      response.flushBuffer();
    }
  }
}
