package uk.gov.cshr.civilservant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.civilservant.controller.v2.models.SimplePage;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.dto.CivilServantProfileDto;
import uk.gov.cshr.civilservant.dto.UpdateOrganisationDTO;
import uk.gov.cshr.civilservant.exception.CivilServantNotFoundException;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.resource.CivilServantResource;
import uk.gov.cshr.civilservant.resource.factory.CivilServantResourceFactory;
import uk.gov.cshr.civilservant.security.CustomOAuth2Authentication;
import uk.gov.cshr.civilservant.service.CivilServantService;
import uk.gov.cshr.civilservant.service.LineManagerService;
import uk.gov.cshr.civilservant.service.identity.IdentityDTO;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RepositoryRestController
@RequestMapping("/civilServants")
@RestResource(exported = false)
public class CivilServantController implements ResourceProcessor<RepositoryLinksResource> {

    private final LineManagerService lineManagerService;

    private final CivilServantRepository civilServantRepository;
    private final CivilServantService civilServantService;

    private final RepositoryEntityLinks repositoryEntityLinks;

    private final CivilServantResourceFactory civilServantResourceFactory;

    public CivilServantController(LineManagerService lineManagerService, CivilServantRepository civilServantRepository,
                                  CivilServantService civilServantService, RepositoryEntityLinks repositoryEntityLinks,
                                  CivilServantResourceFactory civilServantResourceFactory) {
        this.lineManagerService = lineManagerService;
        this.civilServantRepository = civilServantRepository;
        this.civilServantService = civilServantService;
        this.repositoryEntityLinks = repositoryEntityLinks;
        this.civilServantResourceFactory = civilServantResourceFactory;
    }

    @GetMapping
    public ResponseEntity<Resources<Void>> list() {
        log.debug("Listing civil servant links");

        Resources<Void> resource = new Resources<>(new ArrayList<>());
        resource.add(repositoryEntityLinks.linkToSingleResource(CivilServant.class, "me").withRel("me"));
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource<CivilServantResource>> get() {
        log.debug("Getting civil servant details for logged in user");

        return civilServantRepository.findByPrincipal().map(
                civilServant -> ResponseEntity.ok(civilServantResourceFactory.create(civilServant)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping(value = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity patch(@RequestBody @Valid Resource<CivilServant> csResource) {
        log.debug("Patching civil servant details for logged in user");
        civilServantService.update(csResource.getContent());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/login")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource<CivilServantResource>> performLogin(CustomOAuth2Authentication identity) {
        log.debug("Checking civil servant login details & fetching the profile");
        IdentityDTO dto = new IdentityDTO(identity.getUid(), identity.getUserEmail(), identity.getRoles());
        return civilServantService.performLogin(dto).map(
                        civilServant -> ResponseEntity.ok(civilServantResourceFactory.create(civilServant)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/me/organisationalUnit")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public Resource<CivilServantResource> patchOrganisation(@RequestBody UpdateOrganisationDTO updateOrganisationDTO) {
        log.debug("Updating organisational unit for logged in user");
        CivilServant cs = civilServantService.updateMyOrganisationalUnit(updateOrganisationDTO.getOrganisationalUnitId());
        return civilServantResourceFactory.create(cs);
    }

    @PatchMapping("/manager")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Resource<CivilServantResource>> updateLineManager(@RequestParam(value = "email") String email) {

        Optional<CivilServant> optionalCivilServant = civilServantRepository.findByPrincipal();

        if (optionalCivilServant.isPresent()) {

            IdentityDTO lineManagerIdentity = lineManagerService.checkLineManager(email);
            if (lineManagerIdentity == null) {
                log.debug("Line manager email address not found in identity-service.");
                return ResponseEntity.notFound().build();
            }

            Optional<CivilServant> optionalLineManager = civilServantRepository.findByIdentity(lineManagerIdentity.getUid());
            if (!optionalLineManager.isPresent()) {
                log.debug("Line manager email address exists in identity-service, but no profile. uid = {}", lineManagerIdentity);
                return ResponseEntity.notFound().build();
            }

            CivilServant lineManager = optionalLineManager.get();
            CivilServant civilServant = optionalCivilServant.get();
            if (lineManager.equals(civilServant)) {
                log.info("User tried to set line manager to themself, {}.", civilServant);
                return ResponseEntity.badRequest().build();
            }

            civilServant.setLineManager(lineManager);
            civilServantRepository.save(civilServant);

            lineManagerService.notifyLineManager(civilServant, lineManager, email);

            return ResponseEntity.ok(civilServantResourceFactory.create(civilServant));
        }
        return ResponseEntity.unprocessableEntity().build();
    }

    @DeleteMapping("/{uid}/delete")
    @PreAuthorize("hasAnyAuthority('IDENTITY_DELETE', 'CLIENT')")
    @Transactional
    public ResponseEntity deleteCivilServant(@PathVariable String uid) {
        civilServantRepository.findByIdentity(uid).ifPresent(civilServant -> civilServantRepository.delete(civilServant));

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resource/{uid}/remove_organisation")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void removeOrganisation(@PathVariable("uid") String uid) {
        log.info(String.format("Removing organisational unit for user %s", uid));
        CivilServant cs = civilServantRepository.findByIdentity(uid).orElseThrow(CivilServantNotFoundException::new);
        cs.setOrganisationalUnit(null);
        civilServantRepository.saveAndFlush(cs);
    }

    @GetMapping("/resource/{uid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource<CivilServantResource>> getByUID(@PathVariable("uid") String uid) {
        Resource<CivilServantResource> resource = civilServantService.getCivilServantResourceWithUid(uid);
        if (resource != null) {
            return ResponseEntity.ok(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/resource/{uid}/profile")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public CivilServantProfileDto getFullProfileByUid(@PathVariable("uid") String uid) {
        return civilServantService.getFullProfile(uid);
    }

    @GetMapping("/organisation/{code}")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public SimplePage<String> civilServantUidsByOrganisationCode(Pageable pageable, @PathVariable("code") String code) {
        Page<Identity> identities = civilServantRepository.findAllIdentitiesByOrganisationCode(pageable, code);
        return new SimplePage<>(identities.getContent()
                .stream()
                .map(Identity::getUid)
                .collect(Collectors.toList()), identities.getTotalElements(), pageable);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(CivilServantController.class).withRel("civilServants"));
        return resource;
    }
}
