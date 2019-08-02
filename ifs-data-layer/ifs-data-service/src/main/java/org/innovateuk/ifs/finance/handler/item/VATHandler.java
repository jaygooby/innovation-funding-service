package org.innovateuk.ifs.finance.handler.item;

import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.domain.ApplicationFinanceRow;
import org.innovateuk.ifs.finance.domain.FinanceRow;
import org.innovateuk.ifs.finance.domain.ProjectFinanceRow;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.VAT;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class VATHandler extends FinanceRowHandler<VAT> {
    public static final String COST_KEY = "vat";

    @Override
    public void validate(@NotNull VAT vat, @NotNull BindingResult bindingResult) {
        super.validate(vat, bindingResult);
    }

    @Override
    public FinanceRowItem toResource(FinanceRow cost) {
        return buildRowItem(cost);
    }

    private FinanceRowItem buildRowItem(FinanceRow cost){
        return new VAT(cost.getId(), "VAT", Boolean.valueOf(cost.getItem()), cost.getTarget().getId());
    }

    @Override
    public ApplicationFinanceRow toApplicationDomain(VAT vat) {
        return new ApplicationFinanceRow(vat.getId(), COST_KEY, vat.getRegistered() != null ? vat.getRegistered().toString() : "false", vat.getName(), 0, BigDecimal.ZERO, null, vat.getCostType());
    }

    @Override
    public ProjectFinanceRow toProjectDomain(VAT vat) {
        return new ProjectFinanceRow(vat.getId(), COST_KEY, vat.getRegistered() != null ? vat.getRegistered().toString() : "false", vat.getName(), 0, BigDecimal.ZERO, null, vat.getCostType());
    }

    @Override
    public List<ApplicationFinanceRow> initializeCost(ApplicationFinance applicationFinance) {
        ArrayList<ApplicationFinanceRow> costs = new ArrayList<>();
        costs.add(toApplicationDomain(new VAT(applicationFinance.getId())));
        return costs;
    }
}