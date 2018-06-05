package org.innovateuk.ifs.util;

import org.innovateuk.ifs.user.resource.UserResource;

import static org.innovateuk.ifs.user.resource.Role.*;

public final class SecurityRuleUtil {

    private SecurityRuleUtil() {}

    public static boolean isCompAdmin(UserResource user) {
        return user.hasRole(COMP_ADMIN);
    }

    public static boolean isInternal(UserResource user) {
        return user.hasRole(COMP_ADMIN) ||
                user.hasRole(PROJECT_FINANCE) ||
                user.hasRole(SUPPORT) ||
                user.hasRole(INNOVATION_LEAD);
    }

    public static boolean isInternalAdmin(UserResource user) {
        return user.hasRole(COMP_ADMIN) ||
                user.hasRole(PROJECT_FINANCE);
    }

    public static boolean isProjectFinanceUser(UserResource user) {
        return user.hasRole(PROJECT_FINANCE);
    }

    public static boolean isSystemMaintenanceUser(UserResource user) {
        return user.hasRole(SYSTEM_MAINTAINER);
    }

    public static boolean isSystemRegistrationUser(UserResource user) {
        return user.hasRole(SYSTEM_REGISTRATION_USER);
    }

    public static boolean isAssessor(UserResource user) {
        return user.hasRole(ASSESSOR);
    }

    public static boolean isSupport(UserResource user) { return user.hasRole(SUPPORT); }

    public static boolean isInnovationLead(UserResource user) { return user.hasRole(INNOVATION_LEAD); }

    public static boolean isIFSAdmin(UserResource user) { return user.hasRole(IFS_ADMINISTRATOR); }
}
