package org.innovateuk.ifs.publiccontent.transactional;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PublicContentService {

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(value = "GET_PUBLIC_CONTENT",
            description = "The Competition Admin, or project finance user can get the public content for a competition.")
    ServiceResult<PublicContentResource> findByCompetitionId(Long id);


    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(value = "INITIALISE_PUBLIC_CONTENT",
            description = "The Competition Admin, or project finance user can initalise the public content for a competition.")
    ServiceResult<Void> initialiseByCompetitionId(Long id);


    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(value = "PUBLISH_PUBLIC_CONTENT",
            description = "The Competition Admin, or project finance user can publish the public content for a competition.")
    ServiceResult<Void> publishByCompetitionId(Long id);

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(value = "UPDATE_SECTION",
            description = "The Competition Admin, or project finance user can publish the public content for a competition.")
    ServiceResult<Void> updateSection(PublicContentResource resource, PublicContentSectionType section);
}
