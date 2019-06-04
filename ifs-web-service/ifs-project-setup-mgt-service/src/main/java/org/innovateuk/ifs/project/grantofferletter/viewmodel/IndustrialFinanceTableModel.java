package org.innovateuk.ifs.project.grantofferletter.viewmodel;

import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.resource.category.FinanceRowCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
* Holder of values for the industrial finance table, used on the grant offer letter template page
*/

public class IndustrialFinanceTableModel extends BaseFinanceTableModel {

    private final boolean showTotalsColumn;
    private final Map<String, ProjectFinanceResource> finances;
    private final List<String> organisations;
    private final BigDecimal totalEligibleCosts;
    private final BigDecimal totalGrant;
    private final BigDecimal rateOfGrant;

    public IndustrialFinanceTableModel(boolean showTotalsColumn,
                                       Map<String, ProjectFinanceResource> finances,
                                       List<String> organisations,
                                       BigDecimal totalEligibleCosts,
                                       BigDecimal totalGrant,
                                       BigDecimal rateOfGrant) {
        this.showTotalsColumn = showTotalsColumn;
        this.finances = finances;
        this.organisations = organisations;
        this.totalEligibleCosts = totalEligibleCosts;
        this.totalGrant = totalGrant;
        this.rateOfGrant = rateOfGrant;
    }

    public boolean isShowTotalsColumn() {
        return showTotalsColumn;
    }

    public Map<String, ProjectFinanceResource> getFinances() {
        return finances;
    }

    public List<String> getOrganisations() {
        return organisations;
    }

    public BigDecimal getTotalForType(FinanceRowType type) {
        return finances
                .values()
                .stream()
                .map(finance -> finance.getFinanceOrganisationDetails(type))
                .map(FinanceRowCostCategory::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalEligibleCosts() {
        return totalEligibleCosts;
    }

    public BigDecimal getTotalGrant() {
        return totalGrant;
    }

    public BigDecimal getRateOfGrant() {
        return rateOfGrant;
    }
}
