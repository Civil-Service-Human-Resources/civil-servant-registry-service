package uk.gov.cshr.civilservant.resource.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;

import uk.gov.cshr.civilservant.domain.CivilServant;

@RunWith(MockitoJUnitRunner.class)
public class LinkFactoryTest {

  @Mock
  private RepositoryEntityLinks repositoryEntityLinks;

  @InjectMocks
  private LinkFactory linkFactory;

  @Test
  public void shouldReturnRelationshipLink() {
    long id = 99L;
    String relationship = "profession";

    CivilServant civilServant = new CivilServant();
    civilServant.setId(id);

    Link collectionLink = Link.of("/civilServants");

    when(repositoryEntityLinks.linkToCollectionResource(CivilServant.class))
            .thenReturn(collectionLink);

    Link result = linkFactory.createRelationshipLink(civilServant, relationship);

    assertEquals("/civilServants/99/profession", result.getHref());
    assertEquals(relationship, result.getRel().value());
  }

  @Test
  public void shouldReturnSelfLink() {
    long id = 99L;

    CivilServant civilServant = new CivilServant();
    civilServant.setId(id);

    Link link = Link.of("/civilServants/99");

    when(repositoryEntityLinks.linkToItemResource(CivilServant.class, id))
            .thenReturn(link);

    Link result = linkFactory.createSelfLink(civilServant);

    assertEquals("/civilServants/99", result.getHref());
    assertEquals("self", result.getRel().value());
  }
}