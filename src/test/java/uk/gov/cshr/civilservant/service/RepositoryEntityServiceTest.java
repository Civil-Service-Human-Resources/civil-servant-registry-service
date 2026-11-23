package uk.gov.cshr.civilservant.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;

import uk.gov.cshr.civilservant.domain.OrganisationalUnit;

@ExtendWith(MockitoExtension.class)
public class RepositoryEntityServiceTest {

  @Mock
  private RepositoryEntityLinks repositoryEntityLinks;

  @InjectMocks
  private RepositoryEntityService<OrganisationalUnit> repositoryEntityService;

  @Test
  public void shouldReturnUriStringFromOrg() {
    OrganisationalUnit organisationalUnit = new OrganisationalUnit();
    organisationalUnit.setName("ParentTest1");
    organisationalUnit.setCode("PT1");
    organisationalUnit.setAbbreviation("PT1");
    organisationalUnit.setId(1L);

    Link link = Link.of("/organisationalUnits/1");

    when(repositoryEntityLinks.linkToItemResource(
            OrganisationalUnit.class, 1L))
            .thenReturn(link);

    String result = repositoryEntityService.getUri(organisationalUnit);

    assertThat(result, equalTo("/organisationalUnits/1"));
  }
}