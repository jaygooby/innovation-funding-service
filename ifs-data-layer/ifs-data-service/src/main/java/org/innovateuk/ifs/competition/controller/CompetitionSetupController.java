package org.innovateuk.ifs.competition.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competition.transactional.CompetitionService;
import org.innovateuk.ifs.competition.transactional.CompetitionSetupService;
import org.innovateuk.ifs.setup.resource.SetupStatusResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * CompetitionController exposes Competition data and operations through a REST API.
 */
@RestController
@RequestMapping("/competition/setup")
public class CompetitionSetupController {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private CompetitionSetupService competitionSetupService;

    @PutMapping("/{id}")
    public RestResult<CompetitionResource> saveCompetition(@RequestBody CompetitionResource competitionResource,
                                                           @PathVariable("id") final Long id) {
        return competitionSetupService.update(id, competitionResource).toGetResponse();
    }

    @PutMapping("/{id}/update-competition-initial-details")
    public RestResult<Void> updateCompetitionInitialDetails(@RequestBody CompetitionResource competitionResource,
                                                           @PathVariable("id") final Long id) {
        CompetitionResource existingCompetitionResource = competitionService.getCompetitionById(id).getSuccessObjectOrThrowException();
        return competitionSetupService.updateCompetitionInitialDetails(id, competitionResource, existingCompetitionResource.getLeadTechnologist()).toPutResponse();
    }

    @PostMapping("/{id}/initialise-form/{competitionTypeId}")
    public RestResult<Void> initialiseForm(@PathVariable("id") Long competitionId,
                                           @PathVariable("competitionTypeId") Long competitionType) {
        return competitionSetupService.copyFromCompetitionTypeTemplate(competitionId, competitionType).toPostResponse();
    }

    @PostMapping("/generateCompetitionCode/{id}")
    public RestResult<String> generateCompetitionCode(@RequestBody ZonedDateTime dateTime, @PathVariable("id") final Long id) {
        return competitionSetupService.generateCompetitionCode(id, dateTime).toGetResponse();
    }

    @GetMapping("/sectionStatus/complete/{competitionId}/{section}")
    public RestResult<SetupStatusResource> markSectionComplete(@PathVariable("competitionId") final Long competitionId,
                                                               @PathVariable("section") final CompetitionSetupSection section) {
        return competitionSetupService.markSectionComplete(competitionId, section).toGetResponse();
    }

    @GetMapping("/sectionStatus/incomplete/{competitionId}/{section}")
    public RestResult<SetupStatusResource> markSectionInComplete(@PathVariable("competitionId") final Long competitionId,
                                                                 @PathVariable("section") final CompetitionSetupSection section) {
        return competitionSetupService.markSectionInComplete(competitionId, section).toGetResponse();
    }

    @GetMapping("/subsectionStatus/complete/{competitionId}/{parentSection}/{subsection}")
    public RestResult<SetupStatusResource> markSubsectionComplete(@PathVariable("competitionId") final Long competitionId,
                                                                  @PathVariable("parentSection") final CompetitionSetupSection parentSection,
                                                                  @PathVariable("subsection") final CompetitionSetupSubsection subsection) {
        return competitionSetupService.markSubsectionComplete(competitionId, parentSection, subsection).toGetResponse();
    }

    @GetMapping("/subsectionStatus/incomplete/{competitionId}/{parentSection}/{subsection}")
    public RestResult<SetupStatusResource> markSubsectionInComplete(@PathVariable("competitionId") final Long competitionId,
                                                                    @PathVariable("parentSection") final CompetitionSetupSection parentSection,
                                                                    @PathVariable("subsection") final CompetitionSetupSubsection subsection) {
        return competitionSetupService.markSubsectionInComplete(competitionId, parentSection, subsection).toGetResponse();
    }

    @GetMapping("/sectionStatus/{competitionId}")
    public RestResult<Map<CompetitionSetupSection, Optional<Boolean>>> getSectionStatuses(@PathVariable("competitionId") final Long competitionId) {
        return competitionSetupService.getSectionStatuses(competitionId).toGetResponse();
    }

    @GetMapping("/subsectionStatus/{competitionId}")
    public RestResult<Map<CompetitionSetupSubsection, Optional<Boolean>>> getSubsectionStatuses(@PathVariable("competitionId") final Long competitionId) {
        return competitionSetupService.getSubsectionStatuses(competitionId).toGetResponse();
    }

    @PostMapping("/{id}/mark-as-setup")
    public RestResult<Void> markAsSetup(@PathVariable("id") final Long competitionId) {
        return competitionSetupService.markAsSetup(competitionId).toPostResponse();
    }

    @PostMapping("/{id}/return-to-setup")
    public RestResult<Void> returnToSetup(@PathVariable("id") final Long competitionId) {
        return competitionSetupService.returnToSetup(competitionId).toPostResponse();
    }

    @PostMapping
    public RestResult<CompetitionResource> create() {
        return competitionSetupService.create().toPostCreateResponse();
    }

    @PostMapping("/non-ifs")
    public RestResult<CompetitionResource> createNonIfs() {
        return competitionSetupService.createNonIfs().toPostCreateResponse();
    }
}
