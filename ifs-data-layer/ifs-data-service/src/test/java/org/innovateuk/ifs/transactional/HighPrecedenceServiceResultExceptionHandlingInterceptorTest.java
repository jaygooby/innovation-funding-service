package org.innovateuk.ifs.transactional;

import org.innovateuk.ifs.commons.BaseIntegrationTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;

import static org.innovateuk.ifs.commons.error.CommonFailureKeys.GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link HighPrecedenceServiceResultExceptionHandlingInterceptor}, to ensure that Spring Security exceptions
 * are successfully converted into failing "forbidden" ServiceResults.
 */
public class HighPrecedenceServiceResultExceptionHandlingInterceptorTest extends BaseIntegrationTest {

    @Autowired
    private ServiceFailureExceptionHandlingAdviceTestService testService;

    @Test
    @Rollback
    public void testTryingToAccessSecuredMethodHandlesAccessDeniedExceptionSuccessfully() {
        SecurityContextHolder.clearContext();

        ServiceResult<String> result = testService.accessDeniedMethod();
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION));
    }
}
