package org.innovateuk.ifs.validator;

import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.innovateuk.ifs.commons.rest.ValidationMessages.reject;

/**
 * This class validates the FormInputResponse, it checks if there is a value present.
 */
@Component
public class AcademicJesValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return FormInputResponse.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final FormInputResponse response = (FormInputResponse) target;

        if (FormInputType.FINANCE_UPLOAD.equals(response.getFormInput().getType())) {
            if (StringUtils.isEmpty(response.getValue()) || "".equals(response.getValue().trim())) {
                reject(errors, "validation.field.must.not.be.blank");
            }
        }
    }
}
