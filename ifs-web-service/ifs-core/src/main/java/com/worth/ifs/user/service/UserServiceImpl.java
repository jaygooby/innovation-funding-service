package com.worth.ifs.user.service;

import com.worth.ifs.application.UserApplicationRole;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.resource.UserRoleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/**
 * This class contains methods to retrieve and store {@link UserResource} related data,
 * through the RestService {@link UserRestService}.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRestService userRestService;

    @Autowired
    private ProcessRoleService processRoleService;

    private static final Log LOG = LogFactory.getLog(UserServiceImpl.class);

    @Override
    public UserResource findById(Long userId) {
        return userRestService.retrieveUserById(userId).getSuccessObject();
    }

    @Override
    // TODO DW - INFUND-1555 - get service to return RestResult
    public List<UserResource> getAssignable(Long applicationId) {
        return userRestService.findAssignableUsers(applicationId).getSuccessObjectOrThrowException();
    }

    @Override
    public Boolean isLeadApplicant(Long userId, ApplicationResource application) {
        List<ProcessRoleResource> userApplicationRoles = processRoleService.getByIds(application.getProcessRoles());
        return userApplicationRoles.stream().anyMatch(uar -> uar.getRoleName()
                .equals(UserApplicationRole.LEAD_APPLICANT.getRoleName()) && uar.getUser().equals(userId));

    }

    @Override
    public ProcessRoleResource getLeadApplicantProcessRoleOrNull(ApplicationResource application) {
        List<ProcessRoleResource> userApplicationRoles = processRoleService.getByIds(application.getProcessRoles());
        for(final ProcessRoleResource processRole : userApplicationRoles){
            if(processRole.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName())){
                return processRole;
            }
        }
        return null;
    }

    @Override
    public Set<UserResource> getAssignableUsers(ApplicationResource application) {
        return userRestService.findAssignableUsers(application.getId()).andOnSuccessReturn(a -> new HashSet<UserResource>(a)).getSuccessObjectOrThrowException();
    }

    @Override
    public RestResult<UserResource> createLeadApplicantForOrganisation(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId) {
        return userRestService.createLeadApplicantForOrganisation(firstName, lastName, password, email, title, phoneNumber, organisationId);
    }
    @Override
    public RestResult<UserResource> createLeadApplicantForOrganisationWithCompetitionId(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId, Long competitionId) {
        return userRestService.createLeadApplicantForOrganisationWithCompetitionId(firstName, lastName, password, email, title, phoneNumber, organisationId, competitionId);
    }

    @Override
    public RestResult<UserResource> updateDetails(Long id, String email, String firstName, String lastName, String title, String phoneNumber) {
        return userRestService.updateDetails(id, email, firstName, lastName, title, phoneNumber);
    }

    @Override
    public List<UserResource> findUserByType(UserRoleType type) {
        return userRestService.findByUserRoleType(type).getSuccessObjectOrThrowException();
    }

    @Override
    public RestResult<Void> verifyEmail(String hash) {
        return userRestService.verifyEmail(hash);
    }

    @Override
    public void resendEmailVerificationNotification(String email) {
        try {
            userRestService.resendEmailVerificationNotification(email).getSuccessObjectOrThrowException();
        }
        catch (ObjectNotFoundException e) {
            // Do nothing. We don't want to reveal that the address was not recognised
            LOG.debug(format("Purposely ignoring ObjectNotFoundException for email address: [%s] when resending email verification notification.", email));
        }
    }

    @Override
    public RestResult<UserResource> retrieveUserById(Long id) {
        return userRestService.retrieveUserById(id);
    }

    @Override
    public RestResult<Void> sendPasswordResetNotification(String email) {
        return userRestService.sendPasswordResetNotification(email);
    }

    @Override
    public RestResult<Void> checkPasswordResetHash(String hash) {
        return userRestService.checkPasswordResetHash(hash);
    }

    @Override
    public RestResult<Void> resetPassword(String hash, String password) {
        return userRestService.resetPassword(hash,password);
    }

    @Override
    public RestResult<UserResource> findUserByEmail(String email) {
        return userRestService.findUserByEmail(email);
    }

    @Override
    public RestResult<UserResource> findUserByEmailForAnonymousUserFlow(String email) {
        return userRestService.findUserByEmail(email);
    }
}
