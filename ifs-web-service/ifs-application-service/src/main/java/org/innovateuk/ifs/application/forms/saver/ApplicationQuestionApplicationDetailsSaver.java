package org.innovateuk.ifs.application.forms.saver;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.util.TimeZoneUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.springframework.util.StringUtils.hasText;

/**
 * This Saver will handle save all application details that are related to the application.
 */
@Service
public class ApplicationQuestionApplicationDetailsSaver extends AbstractApplicationSaver {

    public ValidationMessages handleApplicationDetailsValidationMessages(List<ValidationMessages> validationMessagesList) {

        List<Error> fieldErrors = validationMessagesList
                .stream()
                .filter(validationMessages -> "target".equals(validationMessages.getObjectName()))
                .map(ValidationMessages::getErrors)
                .flatMap(errors -> errors
                        .stream()
                        .filter(e -> hasText(e.getErrorKey()))
                        .map(mapErrorToApplicationFieldError())
                )
                .collect(toList());

        return new ValidationMessages(fieldErrors);
    }

    public void setApplicationDetails(ApplicationResource application, ApplicationResource updatedApplication) {
        if (updatedApplication == null) {
            return;
        }

        setApplicationName(application, updatedApplication);
        setResubmissionDetails(application, updatedApplication);
        setStartDateDetails(application, updatedApplication);
        setDurationInMonths(application, updatedApplication);
    }

    private Function<Error, Error> mapErrorToApplicationFieldError() {
        return e -> {
            if ("validation.applicationteam.pending.invites".equals(e.getErrorKey())) {
                return mapErrorToApplicationTeamFieldError().apply(e);
            }
            return mapErrorToApplicationDetailsFieldError().apply(e);
        };
    }

    private Function<Error, Error> mapErrorToApplicationTeamFieldError() {
        return e -> fieldError("organisation." + e.getArguments().get(0), e.getFieldRejectedValue(), e.getErrorKey());
    }

    private Function<Error, Error> mapErrorToApplicationDetailsFieldError() {
        return e -> fieldError("application." + e.getFieldName(), e.getFieldRejectedValue(),
                e.getErrorKey(), e.getArguments());
    }

    private void setApplicationName(ApplicationResource application, ApplicationResource updatedApplication) {
        if (updatedApplication.getName() != null) {
            application.setName(updatedApplication.getName());
        }
    }

    private void setStartDateDetails(ApplicationResource application, ApplicationResource updatedApplication) {
        if (updatedApplication.getStartDate() != null) {
            if (dateEmptyOrInPast(updatedApplication.getStartDate())) {
                application.setStartDate(null);
            } else {
                application.setStartDate(updatedApplication.getStartDate());
            }
        } else {
            application.setStartDate(null);
        }
    }

    private void setDurationInMonths(ApplicationResource application, ApplicationResource updatedApplication) {
        if (updatedApplication.getDurationInMonths() != null) {
            application.setDurationInMonths(updatedApplication.getDurationInMonths());
        } else {
            application.setDurationInMonths(null);
        }
    }

    private boolean dateEmptyOrInPast(LocalDate date) {
        return date.isEqual(LocalDate.MIN)
                || date.isBefore(LocalDate.now(TimeZoneUtil.UK_TIME_ZONE));
    }

    private void setResubmissionDetails(ApplicationResource application, ApplicationResource updatedApplication) {
        if (updatedApplication.getResubmission() != null) {
            application.setResubmission(updatedApplication.getResubmission());
            if (updatedApplication.getResubmission()) {
                application.setPreviousApplicationNumber(updatedApplication.getPreviousApplicationNumber());
                application.setPreviousApplicationTitle(updatedApplication.getPreviousApplicationTitle());
            } else {
                application.setPreviousApplicationNumber(null);
                application.setPreviousApplicationTitle(null);
            }
        }
    }
}
