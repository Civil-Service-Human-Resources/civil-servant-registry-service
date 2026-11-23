package uk.gov.cshr.civilservant.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.Identity;


@SpringBootTest
@Transactional
public class IdentityRepositoryTest {

  @Autowired private IdentityRepository identityRepository;

  @Test
  public void shouldFindIdentityByExistingUid() {

    final String uid = "uid";

    identityRepository.save(new Identity(uid));

    Optional<Identity> identity = identityRepository.findByUid(uid);

    assertTrue(identity.isPresent());
    assertEquals(identity.get().getUid(), uid);
  }

  @Test
  public void shouldNotFindIdentityByUnrecognisedUid() {
    Optional<Identity> identity = identityRepository.findByUid("??");
    assertFalse(identity.isPresent());
  }
}
