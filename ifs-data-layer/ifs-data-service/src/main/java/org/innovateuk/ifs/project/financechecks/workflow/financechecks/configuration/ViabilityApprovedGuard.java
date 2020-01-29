package org.innovateuk.ifs.project.financechecks.workflow.financechecks.configuration;

import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.transactional.ProjectFinanceService;
import org.innovateuk.ifs.project.core.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.finance.resource.ViabilityEvent;
import org.innovateuk.ifs.project.finance.resource.ViabilityState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;


@Component
public class ViabilityApprovedGuard implements Guard<ViabilityState, ViabilityEvent> {

    @Autowired
    private ProjectFinanceService projectFinanceService;

    @Override
    public boolean evaluate(StateContext<ViabilityState, ViabilityEvent> context) {
        PartnerOrganisation partnerOrganisation = (PartnerOrganisation) context.getMessage().getHeaders().get("target");
        return isFundingLevelWithinMaximum(
                projectFinanceService.financeChecksDetails(partnerOrganisation.getProject().getId(), partnerOrganisation.getOrganisation().getId()).getSuccess());
    }

    private boolean isFundingLevelWithinMaximum(ProjectFinanceResource finance) {
        return finance.getMaximumFundingLevel() > finance.getGrantClaimPercentage();
    }
}
