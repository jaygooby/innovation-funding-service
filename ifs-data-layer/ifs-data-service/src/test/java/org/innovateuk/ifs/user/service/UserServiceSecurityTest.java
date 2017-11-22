package org.innovateuk.ifs.user.service;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.token.domain.Token;
import org.innovateuk.ifs.token.security.TokenLookupStrategies;
import org.innovateuk.ifs.token.security.TokenPermissionRules;
import org.innovateuk.ifs.user.resource.SearchCategory;
import org.innovateuk.ifs.user.resource.UserOrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserPageResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.security.UserPermissionRules;
import org.innovateuk.ifs.user.transactional.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;

import java.util.List;
import java.util.Set;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.user.builder.UserOrganisationResourceBuilder.newUserOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.externalApplicantRoles;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Testing how the secured methods in UserService interact with Spring Security
 */
public class UserServiceSecurityTest extends BaseServiceSecurityTest<UserService> {

    private UserPermissionRules userRules;
    private TokenPermissionRules tokenRules;
    private TokenLookupStrategies tokenLookupStrategies;

    @Before
    public void lookupPermissionRules() {
        userRules = getMockPermissionRulesBean(UserPermissionRules.class);
        tokenRules = getMockPermissionRulesBean(TokenPermissionRules.class);
        tokenLookupStrategies = getMockPermissionEntityLookupStrategiesBean(TokenLookupStrategies.class);
    }

    @Test
    public void testFindAssignableUsers() {
        classUnderTest.findAssignableUsers(123L);
        assertViewMultipleUsersExpectations();
    }

    @Test
    public void testFindByEmail() {
        assertAccessDenied(() -> classUnderTest.findByEmail("asdf@example.com"), this::assertViewSingleUserExpectations);
    }

    @Test
    public void testChangePassword() {

        Token token = new Token();
        when(tokenLookupStrategies.getTokenByHash("hash")).thenReturn(token);

        assertAccessDenied(() -> classUnderTest.changePassword("hash", "newpassword"), () -> {
            verify(tokenRules).systemRegistrationUserCanUseTokensToResetPaswords(token, getLoggedInUser());
            verifyNoMoreInteractionsWithRules();
        });
    }

    @Test
    public void testSendPasswordResetNotification() {

        UserResource user = newUserResource().build();
        assertAccessDenied(() -> classUnderTest.sendPasswordResetNotification(user), () -> {
            verify(userRules).usersCanChangeTheirOwnPassword(user, getLoggedInUser());
            verify(userRules).systemRegistrationUserCanChangePasswordsForUsers(user, getLoggedInUser());
            verifyNoMoreInteractionsWithRules();
        });
    }

    @Test
    public void testFindRelatedUsers() {
        classUnderTest.findRelatedUsers(123L);
        assertViewMultipleUsersExpectations();
    }

    private void assertViewSingleUserExpectations() {
        assertViewXUsersExpectations(1);
    }

    private void assertViewMultipleUsersExpectations() {
        assertViewXUsersExpectations(2);
    }

    private void assertViewXUsersExpectations(int numberOfUsers) {
        verify(userRules, times(numberOfUsers)).anyUserCanViewThemselves(isA(UserResource.class), eq(getLoggedInUser()));
        verify(userRules, times(numberOfUsers)).assessorsCanViewConsortiumUsersOnApplicationsTheyAreAssessing(isA(UserResource.class), eq(getLoggedInUser()));
        verify(userRules, times(numberOfUsers)).internalUsersCanViewEveryone(isA(UserResource.class), eq(getLoggedInUser()));
        verify(userRules, times(numberOfUsers)).consortiumMembersCanViewOtherConsortiumMembers(isA(UserResource.class), eq(getLoggedInUser()));
        verify(userRules, times(numberOfUsers)).systemRegistrationUserCanViewEveryone(isA(UserResource.class), eq(getLoggedInUser()));
        verifyNoMoreInteractionsWithRules();
    }

    private void verifyNoMoreInteractionsWithRules() {
        verifyNoMoreInteractions(tokenRules);
        verifyNoMoreInteractions(userRules);
    }

    @Test
    public void updateDetails() {
        UserResource user = newUserResource().build();

        assertAccessDenied(() -> classUnderTest.updateDetails(user), () -> {
            verify(userRules).usersCanUpdateTheirOwnProfiles(user, getLoggedInUser());
            verifyNoMoreInteractions(userRules);
        });
    }

    @Test
    public void testFindActiveByProcessRoles(){
        assertAccessDenied(() -> classUnderTest.findActiveByProcessRoles(UserRoleType.internalRoles(), new PageRequest(0, 5)), () -> {
            verify(userRules).internalUsersCanViewEveryone(isA(UserPageResource.class), eq(getLoggedInUser()));
            verifyNoMoreInteractions(userRules);
        });
    }

    @Test
    public void testFindInactiveByProcessRoles(){
        assertAccessDenied(() -> classUnderTest.findInactiveByProcessRoles(UserRoleType.internalRoles(), new PageRequest(0, 5)), () -> {
            verify(userRules).internalUsersCanViewEveryone(isA(UserPageResource.class), eq(getLoggedInUser()));
            verifyNoMoreInteractions(userRules);
        });
    }

    //TODO - Will be deleted/fixed once junits for IFS-1986 are complete.
/*    @Test
    public void testFindAllByProcessRoles(){
        classUnderTest.findAllByProcessRoles(externalApplicantRoles());
        verify(userRules, times(2)).internalUsersCanViewUserOrganisation(isA(UserOrganisationResource.class), eq(getLoggedInUser()));
        verifyNoMoreInteractions(userRules);
    }*/

    @Override
    protected Class<? extends UserService> getClassUnderTest() {
        return TestUserService.class;
    }

    /**
     * Test class for use in Service Security tests.
     */
    public static class TestUserService implements UserService {
        @Override
        public ServiceResult<UserResource> findByEmail(String email) {
            return serviceSuccess(newUserResource().build());
        }

        @Override
        public ServiceResult<UserResource> findInactiveByEmail(String email) {
            return serviceSuccess(newUserResource().build());
        }

        @Override
        public ServiceResult<Set<UserResource>> findAssignableUsers(Long applicationId) {
            return serviceSuccess(newUserResource().buildSet(2));
        }

        @Override
        public ServiceResult<Set<UserResource>> findRelatedUsers(Long applicationId) {
            return serviceSuccess(newUserResource().buildSet(2));
        }

        @Override
        public ServiceResult<Void> sendPasswordResetNotification(@P("user") UserResource user) {
            return null;
        }

        @Override
        public ServiceResult<Void> changePassword(@P("hash") String hash, String password) {
            return null;
        }

        @Override
        public ServiceResult<Void> updateDetails(@P("userBeingUpdated") UserResource userBeingUpdated) {
            return null;
        }

        @Override
        public ServiceResult<UserPageResource> findActiveByProcessRoles(Set<UserRoleType> roleTypes, Pageable pageable) {
            return serviceSuccess(new UserPageResource());
        }

        //TODO - Will be deleted/fixed once junits for IFS-1986 are complete.
/*        @Override
        public ServiceResult<List<UserOrganisationResource>> findAllByProcessRoles(Set<UserRoleType> roleTypes) {
            return serviceSuccess(newUserOrganisationResource().build(2));
        }*/

        @Override
        public ServiceResult<List<UserOrganisationResource>> findByProcessRolesAndSearchCriteria(Set<UserRoleType> roleTypes, String searchString, SearchCategory searchCategory) {
            return serviceSuccess(newUserOrganisationResource().build(2));
        }

        @Override
        public ServiceResult<UserPageResource> findInactiveByProcessRoles(Set<UserRoleType> roleTypes, Pageable pageable) {
            return serviceSuccess(new UserPageResource());
        }

    }
}
