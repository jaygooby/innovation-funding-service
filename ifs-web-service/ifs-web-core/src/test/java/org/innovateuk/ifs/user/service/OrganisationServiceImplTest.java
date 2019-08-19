package org.innovateuk.ifs.user.service;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.RESEARCH;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class OrganisationServiceImplTest extends BaseServiceUnitTest<OrganisationService> {

    @Mock
    private OrganisationRestService organisationRestService;

    @Mock
    private UserRestService userRestService;

    @Mock
    private UserService userService;

    @Override
    protected OrganisationService supplyServiceUnderTest() {
        return new OrganisationServiceImpl(organisationRestService, userRestService, userService);
    }

    @Test
    public void getOrganisationType() {

        UserResource user = newUserResource().withId(2L).build();
        OrganisationResource organisation = newOrganisationResource()
                .withOrganisationTypeName(BUSINESS.name())
                .withOrganisationType(BUSINESS.getId())
                .withId(4L)
                .build();
        ProcessRoleResource processRole = newProcessRoleResource()
                .withUser(user)
                .withApplication(3L)
                .withOrganisation(organisation.getId())
                .build();

        when(userRestService.findProcessRole(user.getId(), processRole.getApplicationId())).thenReturn(restSuccess(processRole));
        when(organisationRestService.getOrganisationById(organisation.getId())).thenReturn(restSuccess(organisation));

        Long returnedOrganisationType = service.getOrganisationType(user.getId(), processRole.getApplicationId());

        assertEquals(organisation.getOrganisationType(), returnedOrganisationType);
    }

    @Test
    public void returnNullOrganisationType() {

        UserResource user = newUserResource().withId(2L).build();
        ProcessRoleResource processRole = newProcessRoleResource()
                .withApplication(3L)
                .withUser(user)
                .build();

        when(userRestService.findProcessRole(user.getId(), processRole.getApplicationId())).thenReturn(restSuccess(processRole));
        when(organisationRestService.getOrganisationById(null)).thenReturn(null);

        Long returnedOrganisationType = service.getOrganisationType(user.getId(), processRole.getApplicationId());

        assertNull(returnedOrganisationType);
    }

    @Test
    public void getOrganisationForUser() {

        UserResource user = newUserResource().withId(2L).build();
        OrganisationResource organisation = newOrganisationResource().withId(4L).build();
        ProcessRoleResource roleWithUser = newProcessRoleResource()
                .withUser(user)
                .withOrganisation(organisation.getId())
                .build();

        when(organisationRestService.getOrganisationById(organisation.getId())).thenReturn(restSuccess(organisation));

        Optional<OrganisationResource> result = service.getOrganisationForUser(user.getId(), singletonList(roleWithUser));

        assertEquals(organisation, result.get());
    }

    @Test
    public void getApplicationOrganisations() {

        OrganisationResource leadOrganisation = newOrganisationResource().withId(3L).build();
        OrganisationResource collaborator1 = newOrganisationResource().withId(18L).build();
        OrganisationResource collaborator2 = newOrganisationResource().withId(2L).build();

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        when(organisationRestService.getOrganisationById(collaborator1.getId())).thenReturn(restSuccess(collaborator1));
        when(organisationRestService.getOrganisationById(collaborator2.getId())).thenReturn(restSuccess(collaborator2));

        List<ProcessRoleResource> processRoleResources = newProcessRoleResource()
                .withOrganisation(leadOrganisation.getId(),
                        collaborator1.getId(),
                        collaborator2.getId())
                .withRole(LEADAPPLICANT, COLLABORATOR, COLLABORATOR, ASSESSOR)
                .build(3);

        SortedSet<OrganisationResource> sortedProcessRoles = service.getApplicationOrganisations(processRoleResources);

        assertEquals(sortedProcessRoles.first(), collaborator2);
        assertEquals(sortedProcessRoles.last(), collaborator1);
    }

    @Test
    public void getAcademicOrganisations() {

        OrganisationResource academicOrganisation = newOrganisationResource()
                .withOrganisationTypeName(RESEARCH.name())
                .withOrganisationType(RESEARCH.getId())
                .withId(12L)
                .build();
        OrganisationResource leadOrganisation = newOrganisationResource()
                .withOrganisationTypeName(BUSINESS.name())
                .withOrganisationType(BUSINESS.getId())
                .withId(3L)
                .build();

        SortedSet<OrganisationResource> sortedAcademicOrganisation = new TreeSet<>(Comparator.comparingLong(OrganisationResource::getId));
        sortedAcademicOrganisation.add(leadOrganisation);
        sortedAcademicOrganisation.add(academicOrganisation);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        when(organisationRestService.getOrganisationById(academicOrganisation.getId())).thenReturn(restSuccess(academicOrganisation));

        SortedSet<OrganisationResource> result = service.getAcademicOrganisations(sortedAcademicOrganisation);

        assertEquals(result.first(), academicOrganisation);
    }

    @Test
    public void getApplicationLeadOrganisation() {

        OrganisationResource leadOrganisation = newOrganisationResource().withId(3L).build();
        OrganisationResource collaborator = newOrganisationResource().withId(18L).build();

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        when(organisationRestService.getOrganisationById(collaborator.getId())).thenReturn(restSuccess(collaborator));

        List<ProcessRoleResource> processRoleResources = newProcessRoleResource()
                .withOrganisation(leadOrganisation.getId(),
                        collaborator.getId())
                .withRole(LEADAPPLICANT, COLLABORATOR)
                .build(2);

        Optional<OrganisationResource> result = service.getApplicationLeadOrganisation(processRoleResources);

        assertEquals(leadOrganisation, result.get());
    }

    @Test
    public void getLeadOrganisation() {

        UserResource user = newUserResource().build();
        OrganisationResource leadOrganisation = newOrganisationResource().withId(3L).build();
        ProcessRoleResource processRole = newProcessRoleResource()
                .withApplication(123L)
                .withUser(user)
                .withRole(LEADAPPLICANT)
                .withOrganisation(leadOrganisation.getId())
                .build();

        when(userService.getLeadApplicantProcessRole(processRole.getApplicationId())).thenReturn(processRole);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        OrganisationResource result = service.getLeadOrganisation(processRole.getApplicationId(), singletonList(leadOrganisation));

        assertEquals(leadOrganisation, result);
    }
}