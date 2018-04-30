package org.innovateuk.ifs.project.spendprofile.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.assessment.domain.AssessmentParticipant;
import org.innovateuk.ifs.competition.domain.CompetitionParticipantRole;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectProcess;
import org.innovateuk.ifs.project.resource.ProjectCompositeId;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.assessment.builder.AssessmentParticipantBuilder.newAssessmentParticipant;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectProcessBuilder.newProjectProcess;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.SecurityRuleUtil.isInternalAdmin;
import static org.innovateuk.ifs.util.SecurityRuleUtil.isSupport;
import static org.innovateuk.ifs.workflow.domain.ActivityType.PROJECT_SETUP;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpendProfilePermissionRulesTest extends BasePermissionRulesTest<SpendProfilePermissionRules> {

    private ProjectResource projectResource1;
    private UserResource innovationLeadUserResourceOnProject1;
    private ProjectProcess projectProcess;

    @Before
    public void setup() {
        User innovationLeadUserOnProject1 = newUser().withRoles(singleton(Role.INNOVATION_LEAD)).build();
        innovationLeadUserResourceOnProject1 = newUserResource().withId(innovationLeadUserOnProject1.getId()).withRolesGlobal(singletonList(Role.INNOVATION_LEAD)).build();
        AssessmentParticipant competitionParticipant = newAssessmentParticipant().withUser(innovationLeadUserOnProject1).build();
        Competition competition = newCompetition().withLeadTechnologist(innovationLeadUserOnProject1).build();
        Application application1 = newApplication().withCompetition(competition).build();
        ApplicationResource applicationResource1 = newApplicationResource().withId(application1.getId()).withCompetition(competition.getId()).build();
        projectResource1 = newProjectResource().withApplication(applicationResource1).build();
        projectProcess = newProjectProcess().withActivityState(new ActivityState(PROJECT_SETUP, ProjectState.SETUP.getBackingState())).build();

        when(projectProcessRepositoryMock.findOneByTargetId(anyLong())).thenReturn(projectProcess);
        when(applicationRepositoryMock.findOne(application1.getId())).thenReturn(application1);
        when(assessmentParticipantRepositoryMock.getByCompetitionIdAndRole(competition.getId(), CompetitionParticipantRole.INNOVATION_LEAD)).thenReturn(Collections.singletonList(competitionParticipant));
    }

    @Test
    public void testInternalAdminTeamCanViewCompetitionStatus(){
        allGlobalRoleUsers.forEach(user -> {
            if (isInternalAdmin(user)) {
                assertTrue(rules.internalAdminTeamCanViewCompetitionStatus(newProjectResource().build(), user));
            } else {
                assertFalse(rules.internalAdminTeamCanViewCompetitionStatus(newProjectResource().build(), user));
            }
        });
    }

    @Test
    public void testSupportCanViewCompetitionStatus(){
        allGlobalRoleUsers.forEach(user -> {
            if (isSupport(user)) {
                assertTrue(rules.supportCanViewCompetitionStatus(newProjectResource().build(), user));
            } else {
                assertFalse(rules.supportCanViewCompetitionStatus(newProjectResource().build(), user));
            }
        });
    }

    @Test
    public void testAssignedInnovationLeadCanViewProjectStatus(){
        assertTrue(rules.assignedInnovationLeadCanViewSPStatus(projectResource1, innovationLeadUserResourceOnProject1));
        assertFalse(rules.assignedInnovationLeadCanViewSPStatus(projectResource1, innovationLeadUser()));
    }

    @Test
    public void testProjectManagerCanCompleteSpendProfile() throws Exception {
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();

        setUpUserAsProjectManager(project, user);

        assertTrue(rules.projectManagerCanCompleteSpendProfile(ProjectCompositeId.id(project.getId()), user));
    }

    @Test
    public void testLeadPartnerCanIncompleteAnySpendProfile() throws Exception {
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), newOrganisation().build().getId());

        setupUserAsLeadPartner(project, user);

        assertTrue(rules.leadPartnerCanMarkSpendProfileIncomplete(projectOrganisationCompositeId, user));
    }

    @Test
    public void testNonLeadPartnerCannotIncompleteSpendProfile() throws Exception {
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), newOrganisation().build().getId());

        setupUserNotAsLeadPartner(project, user);

        assertFalse(rules.leadPartnerCanMarkSpendProfileIncomplete(projectOrganisationCompositeId, user));
    }

    @Test
    public void testLeadPartnerCanViewAnySpendProfileData() throws Exception {

        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), newOrganisation().build().getId());

        setupUserAsLeadPartner(project, user);

        assertTrue(rules.leadPartnerCanViewAnySpendProfileData(projectOrganisationCompositeId, user));
    }
    @Test
    public void testUserNotLeadPartnerCannotViewSpendProfile() throws Exception {
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), newOrganisation().build().getId());

        setupUserNotAsLeadPartner(project, user);

        assertFalse(rules.leadPartnerCanViewAnySpendProfileData(projectOrganisationCompositeId, user));
    }

    @Test
    public void testPartnersCanViewTheirOwnSpendProfileData(){
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        OrganisationResource org = newOrganisationResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), org.getId());

        setupUserAsPartner(project, user, org);
        assertTrue(rules.partnersCanViewTheirOwnSpendProfileData(projectOrganisationCompositeId, user));

        setupUserNotAsPartner(project, user, org);
        assertFalse(rules.partnersCanViewTheirOwnSpendProfileData(projectOrganisationCompositeId, user));
    }

    @Test
    public void testProjectFinanceUserCanViewAnySpendProfileData(){
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(1L, newOrganisation().build().getId());

        allGlobalRoleUsers.forEach(user -> {
            if (user.equals(projectFinanceUser())) {
                assertTrue(rules.projectFinanceUserCanViewAnySpendProfileData(projectOrganisationCompositeId, user));
            } else {
                assertFalse(rules.projectFinanceUserCanViewAnySpendProfileData(projectOrganisationCompositeId, user));
            }
        });
    }

    @Test
    public void testPartnersCanViewTheirOwnSpendProfileCsv(){
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        OrganisationResource org = newOrganisationResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), org.getId());

        setupUserAsPartner(project, user, org);
        assertTrue(rules.partnersCanViewTheirOwnSpendProfileCsv(projectOrganisationCompositeId, user));

        setupUserNotAsPartner(project, user, org);
        assertFalse(rules.partnersCanViewTheirOwnSpendProfileCsv(projectOrganisationCompositeId, user));
    }

    @Test
    public void testInternalAdminUsersCanSeeSpendProfileCsv(){
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(1L, newOrganisation().build().getId());

        allGlobalRoleUsers.forEach(user -> {
            if (isInternalAdmin(user)) {
                assertTrue(rules.internalAdminUsersCanSeeSpendProfileCsv(projectOrganisationCompositeId, user));
            } else {
                assertFalse(rules.internalAdminUsersCanSeeSpendProfileCsv(projectOrganisationCompositeId, user));
            }
        });
    }

    @Test
    public void testSupportUsersCanSeeSpendProfileCsv(){
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(1L, newOrganisation().build().getId());

        allGlobalRoleUsers.forEach(user -> {
            if (isSupport(user)) {
                assertTrue(rules.supportUsersCanSeeSpendProfileCsv(projectOrganisationCompositeId, user));
            } else {
                assertFalse(rules.supportUsersCanSeeSpendProfileCsv(projectOrganisationCompositeId, user));
            }
        });
    }

    @Test
    public void testLeadPartnerCanViewAnySpendProfileCsv(){
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), newOrganisation().build().getId());

        setupUserAsLeadPartner(project, user);
        assertTrue(rules.leadPartnerCanViewAnySpendProfileCsv(projectOrganisationCompositeId, user));

        setupUserNotAsLeadPartner(project, user);
        assertFalse(rules.leadPartnerCanViewAnySpendProfileCsv(projectOrganisationCompositeId, user));
    }

    @Test
    public void testPartnersCanEditTheirOwnSpendProfileData(){
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        OrganisationResource org = newOrganisationResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), org.getId());

        setupUserAsPartner(project, user, org);
        assertTrue(rules.partnersCanEditTheirOwnSpendProfileData(projectOrganisationCompositeId, user));

        setupUserNotAsPartner(project, user, org);
        assertFalse(rules.partnersCanEditTheirOwnSpendProfileData(projectOrganisationCompositeId, user));
    }

    @Test
    public void testPartnersCanMarkSpendProfileAsComplete(){
        ProjectResource project = newProjectResource().build();
        UserResource user = newUserResource().build();
        OrganisationResource org = newOrganisationResource().build();
        ProjectOrganisationCompositeId projectOrganisationCompositeId =
                new ProjectOrganisationCompositeId(project.getId(), org.getId());

        setupUserAsPartner(project, user, org);
        assertTrue(rules.partnersCanMarkSpendProfileAsComplete(projectOrganisationCompositeId, user));

        setupUserNotAsPartner(project, user, org);
        assertFalse(rules.partnersCanMarkSpendProfileAsComplete(projectOrganisationCompositeId, user));
    }

    @Override
    protected SpendProfilePermissionRules supplyPermissionRulesUnderTest() {
        return new SpendProfilePermissionRules();
    }
}
