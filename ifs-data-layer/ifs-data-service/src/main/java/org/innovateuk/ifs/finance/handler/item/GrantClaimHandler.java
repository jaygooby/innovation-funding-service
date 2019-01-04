package org.innovateuk.ifs.finance.handler.item;

import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;
import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.domain.ApplicationFinanceRow;
import org.innovateuk.ifs.finance.domain.FinanceRow;
import org.innovateuk.ifs.finance.domain.ProjectFinanceRow;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.GrantClaim;
import org.innovateuk.ifs.finance.validator.GrantClaimValidator;
import org.innovateuk.ifs.publiccontent.repository.PublicContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import javax.validation.groups.Default;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the grant claims, i.e. converts the costs to be stored into the database
 * or for sending it over.
 */
@Component
public class GrantClaimHandler extends FinanceRowHandler<GrantClaim> {
    public static final String GRANT_CLAIM = "Grant Claim";
    public static final String COST_KEY = "grant-claim";

    @Autowired
    private GrantClaimValidator grantClaimValidator;

    //TODO remove IFS-4982
    @Autowired
    private PublicContentRepository publicContentRepository;

    @Override
    public void validate(GrantClaim grantClaim, BindingResult bindingResult) {
        super.validate(grantClaim, bindingResult, Default.class);
        grantClaimValidator.validate(grantClaim, bindingResult);
    }

    @Override
    public ApplicationFinanceRow toCost(GrantClaim grantClaim) {
        return new ApplicationFinanceRow(grantClaim.getId(), COST_KEY, "", GRANT_CLAIM, grantClaim.getGrantClaimPercentage(), BigDecimal.ZERO, null,null);
    }

    @Override
    public ProjectFinanceRow toProjectCost(GrantClaim costItem) {
        return new ProjectFinanceRow(costItem.getId(), COST_KEY, "", GRANT_CLAIM, costItem.getGrantClaimPercentage(), BigDecimal.ZERO, null,null);
    }

    @Override
    public FinanceRowItem toCostItem(ApplicationFinanceRow cost) {
        return buildRowItem(cost);
    }

    @Override
    public FinanceRowItem toCostItem(ProjectFinanceRow cost) {
        return buildRowItem(cost);
    }

    private FinanceRowItem buildRowItem(FinanceRow cost){
        return new GrantClaim(cost.getId(), cost.getQuantity());
    }

    @Override
    public List<ApplicationFinanceRow> initializeCost(ApplicationFinance applicationFinance) {
        Competition competition = applicationFinance.getApplication().getCompetition();
        ArrayList<ApplicationFinanceRow> costs = new ArrayList<>();
        costs.add(initializeFundingLevel(competition));
        return costs;
    }

    private ApplicationFinanceRow initializeFundingLevel(Competition competition) {
        GrantClaim costItem = new GrantClaim();
        if (publicContentRepository.findByCompetitionId(competition.getId()).getFundingType() == FundingType.PROCUREMENT) {
            costItem.setGrantClaimPercentage(100);
        } else {
            costItem.setGrantClaimPercentage(null);
        }
        return toCost(costItem);
    }
}
