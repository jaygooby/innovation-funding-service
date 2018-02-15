package org.innovateuk.ifs.interview.workflow.actions;

import org.innovateuk.ifs.interview.domain.InterviewAssignment;
import org.innovateuk.ifs.interview.domain.InterviewAssignmentMessageOutcome;
import org.innovateuk.ifs.interview.resource.InterviewAssignmentEvent;
import org.innovateuk.ifs.interview.resource.InterviewAssignmentState;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

@Component
public class NotifyInterviewAssignmentAction extends BaseInterviewAssignmentAction {

    @Override
    protected void doExecute(InterviewAssignment interviewAssignment, StateContext<InterviewAssignmentState, InterviewAssignmentEvent> context) {
        InterviewAssignmentMessageOutcome interviewAssignmentMessageOutcome =
                (InterviewAssignmentMessageOutcome) context.getMessageHeader("message");
        interviewAssignment.setMessage(interviewAssignmentMessageOutcome);
    }
}