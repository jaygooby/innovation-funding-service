package com.worth.ifs.transactional;

import com.worth.ifs.assessment.dto.Feedback;
import com.worth.ifs.util.Either;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Service to handle crosscutting business processes related to Assessors and their role within the system.
 *
 * Created by dwatson on 06/10/15.
 */
public interface AssessorService {

    /**
     * Update the Assessor's feedback to a given Response, creating a new AssessorFeedback if one does not yet
     * exist for this Assessor
     *
     * @param feedback
     * @return
     */
    @PreAuthorize("hasPermission(#feedback, 'UPDATE')")
    Either<ServiceFailure, ServiceSuccess> updateAssessorFeedback(Feedback feedback);

}
