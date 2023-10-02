package uk.gov.cshr.civilservant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.civilservant.domain.Roles;
import uk.gov.cshr.civilservant.domain.Status;
import uk.gov.cshr.civilservant.dto.CivilServantReportDto;
import uk.gov.cshr.civilservant.dto.SkillsReportsDto;
import uk.gov.cshr.civilservant.service.ReportService;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class ReportControllerTest extends CSRSControllerTestBase {

  public static final String TEXT_CSV = "text/csv";
  @Autowired ObjectMapper objectMapper;
  @MockBean private ReportService reportService;

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"ORGANISATION_REPORTER"})
  public void shouldGetCivilServantsByUserOrganisationWithCorrectRole() throws Exception {
    CivilServantReportDto civilServant1 = new CivilServantReportDto();
    civilServant1.setName("User 1");
    CivilServantReportDto civilServant2 = new CivilServantReportDto();
    civilServant2.setName("User 2");

    when(reportService.getCivilServantMapByUserOrganisationNormalised("user"))
        .thenReturn(ImmutableMap.of("1", civilServant1, "2", civilServant2));

    mockMvc
        .perform(get("/report/civilServants").with(csrf()).accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("1.name", equalTo("User 1")))
        .andExpect(jsonPath("2.name", equalTo("User 2")));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"PROFESSION_REPORTER"})
  public void shouldGetCivilServantsByUserProfessionWithCorrectRole() throws Exception {
    CivilServantReportDto civilServant1 = new CivilServantReportDto();
    civilServant1.setName("User 1");
    CivilServantReportDto civilServant2 = new CivilServantReportDto();
    civilServant2.setName("User 2");

    when(reportService.getCivilServantMapByUserProfessionNormalised("user"))
        .thenReturn(ImmutableMap.of("1", civilServant1, "2", civilServant2));

    mockMvc
        .perform(get("/report/civilServants").with(csrf()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("1.name", equalTo("User 1")))
        .andExpect(jsonPath("2.name", equalTo("User 2")));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"CSHR_REPORTER"})
  public void shouldGetAllCivilServants() throws Exception {
    CivilServantReportDto civilServant1 = new CivilServantReportDto();
    civilServant1.setName("User 1");

    CivilServantReportDto civilServant2 = new CivilServantReportDto();
    civilServant2.setName("User 2");

    when(reportService.getCivilServantMapNormalised())
        .thenReturn(ImmutableMap.of("1", civilServant1, "2", civilServant2));

    mockMvc
        .perform(get("/report/civilServants").with(csrf()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("1.name", equalTo("User 1")))
        .andExpect(jsonPath("2.name", equalTo("User 2")));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"INVALID_ROLE"})
  public void shouldReturn404WithIncorrectRole() throws Exception {
    mockMvc
        .perform(get("/report/civilServants").with(csrf()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {Roles.CSHR_REPORTER, Roles.LEARNING_MANAGER})
  public void shouldGetReportsForSuperAdmin() throws Exception {
    // Given
    List<SkillsReportsDto> skillsReportsDtoList = buildAReport();
    File file = new File("src/test/resources/test_data/reports.csv");

    // when
    when(reportService.getReportForSuperAdmin(any(), any())).thenReturn(skillsReportsDtoList);

    mockMvc
        .perform(
            get("/report/skills/report-for-super-admin")
                .param("from", LocalDate.now().minusDays(1).toString())
                .param("to", LocalDate.now().toString())
                .with(csrf())
                .accept(TEXT_CSV))
        .andExpect(status().isOk())
        .andExpect(content().string(FileUtils.readFileToString(file)));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"Unacceptable.Role"})
  public void shouldError() throws Exception {
    // Given  - an unacceptable role

    // then
    mockMvc
        .perform(
            get("/report/skills/report-for-super-admin")
                .param("from", LocalDate.now().minusDays(1).toString())
                .param("to", LocalDate.now().toString())
                .with(csrf())
                .accept(TEXT_CSV))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {Roles.ORGANISATION_AUTHOR, Roles.ORGANISATION_REPORTER})
  public void shouldGetReportsForOrgAdmin() throws Exception {
    // Given
    List<SkillsReportsDto> skillsReportsDtoList = buildAReport();
    File file = new File("src/test/resources/test_data/reports.csv");

    // when
    when(reportService.getReportForOrganisationAdmin(anyLong(), any(), any()))
        .thenReturn(skillsReportsDtoList);

    // then
    mockMvc
        .perform(
            get("/report/skills/report-for-department-admin")
                .param("from", LocalDate.now().minusDays(1).toString())
                .param("to", LocalDate.now().toString())
                .param("organisationId", "1")
                .with(csrf())
                .accept(TEXT_CSV))
        .andExpect(status().isOk())
        .andExpect(content().string(FileUtils.readFileToString(file)));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {Roles.PROFESSION_AUTHOR, Roles.PROFESSION_REPORTER})
  public void shouldGetReportsForProfAdmin() throws Exception {
    // Given
    List<SkillsReportsDto> skillsReportsDtoList = buildAReport();
    File file = new File("src/test/resources/test_data/reports.csv");

    // when
    when(reportService.getReportForProfessionAdmin(anyLong(), any(), any()))
        .thenReturn(skillsReportsDtoList);

    mockMvc
        .perform(
            get("/report/skills/report-for-profession-admin")
                .param("from", LocalDate.now().minusDays(1).toString())
                .param("to", LocalDate.now().toString())
                .param("organisationId", "1")
                .param("professionId", "1")
                .with(csrf())
                .accept(TEXT_CSV))
        .andExpect(status().isOk())
        .andExpect(content().string(FileUtils.readFileToString(file)));
  }

  private List<SkillsReportsDto> buildAReport() {
    List<SkillsReportsDto> skillsReportsDtoList = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      SkillsReportsDto skillsReportsDto =
          SkillsReportsDto.builder()
              .correctCount(i + 1)
              .skippedCount(i + 2)
              .incorrectCount(i + 3)
              .questionId(i)
              .questionName("Question number " + i)
              .professionName("Some profession " + i)
              .questionTheme("Some question theme " + i)
              .timesAttempted(i * 31)
              .status(Status.PUBLISHED)
              .quizName("Quiz for profession " + i)
              .build();
      skillsReportsDtoList.add(skillsReportsDto);
    }
    return skillsReportsDtoList;
  }
}
