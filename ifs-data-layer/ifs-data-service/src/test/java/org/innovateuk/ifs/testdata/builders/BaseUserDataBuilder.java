package org.innovateuk.ifs.testdata.builders;

import org.innovateuk.ifs.testdata.builders.data.BaseUserData;
import org.innovateuk.ifs.token.domain.Token;
import org.innovateuk.ifs.token.resource.TokenType;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;

/**
 * Base builder for generating data for non-active and active registered users
 */
public abstract class BaseUserDataBuilder<T extends BaseUserData, S> extends BaseDataBuilder<T, S> {

    public abstract S registerUser(String firstName, String lastName, String emailAddress, String phoneNumber);

    protected void registerUser(String firstName, String lastName, String emailAddress, String phoneNumber, List<Role> roles, T data) {

        doAs(systemRegistrar(), () -> {
            doRegisterUserWithExistingOrganisation(firstName, lastName, emailAddress, phoneNumber, roles, data);
        });
    }

    public S verifyEmail() {

        return with(data -> {

            doAs(systemRegistrar(), () -> {
                UserResource user = data.getUser();

                Optional<Token> verifyToken = tokenRepository.findByTypeAndClassNameAndClassPk(TokenType.VERIFY_EMAIL_ADDRESS, User.class.getName(), user.getId());

                verifyToken.map(token -> registrationService.activateUser(token.getClassPk()).andOnSuccessReturnVoid(v -> {
                    tokenService.handleExtraAttributes(token);
                    tokenService.removeToken(token);
                })).orElseThrow(() -> new RuntimeException("No Invite Token exists for user " + user.getId()));
            });
        });
    }

    public S deactivateUser() {
        return with(data -> {
            doAs(ifsAdmin(), () -> {
                UserResource user = data.getUser();
                registrationService.deactivateUser(user.getId());
            });
        });
    }

    private void updateUserInUserData(T data, Long userId) {
        UserResource user = baseUserService.getUserById(userId).getSuccess();
        data.setUser(user);
    }

    private UserResource createUserViaRegistration(String firstName, String lastName, String emailAddress, String phoneNumber, List<Role> roles) {

        UserResource created = registrationService.createUser(newUserResource().
                withFirstName(firstName).
                withLastName(lastName).
                withEmail(emailAddress).
                withPhoneNumber(phoneNumber).
                withRolesGlobal(roles).
                withPassword("Passw0rd").
                build()).
                getSuccess();

        return created;
    }

    private void doRegisterUserWithExistingOrganisation(String firstName, String lastName, String emailAddress, String phoneNumber, List<Role> roles, T data) {
        UserResource registeredUser = createUserViaRegistration(firstName, lastName, emailAddress, phoneNumber, roles);
        updateUserInUserData(data, registeredUser.getId());
    }

    public BaseUserDataBuilder(List<BiConsumer<Integer, T>> newActions, ServiceLocator serviceLocator) {
        super(newActions, serviceLocator);
    }
}
