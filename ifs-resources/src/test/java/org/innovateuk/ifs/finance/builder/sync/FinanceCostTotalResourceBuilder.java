package org.innovateuk.ifs.finance.builder.sync;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.finance.resource.sync.FinanceCostTotalResource;
import org.innovateuk.ifs.finance.resource.sync.FinanceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;

public class FinanceCostTotalResourceBuilder extends BaseBuilder<FinanceCostTotalResource, FinanceCostTotalResourceBuilder> {
    protected FinanceCostTotalResourceBuilder(List<BiConsumer<Integer, FinanceCostTotalResource>> multiActions) {
        super(multiActions);
    }

    @Override
    protected FinanceCostTotalResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, FinanceCostTotalResource>> actions) {
        return new FinanceCostTotalResourceBuilder(actions);
    }

    @Override
    protected FinanceCostTotalResource createInitial() {
        return new FinanceCostTotalResource();
    }

    public static FinanceCostTotalResourceBuilder newFinanceCostTotalResource() {
        return new FinanceCostTotalResourceBuilder(emptyList());
    }

    public FinanceCostTotalResourceBuilder withName(String... costName) {
        return withArraySetFieldByReflection("name", costName);
    }

    public FinanceCostTotalResourceBuilder withTotal(BigDecimal... costTotal) {
        return withArraySetFieldByReflection("total", costTotal);
    }

    public FinanceCostTotalResourceBuilder withType(FinanceType... financeTypes) {
        return withArray((financeType, totalResource) -> setField("financeType", financeType.getName(), totalResource), financeTypes);
    }

    public FinanceCostTotalResourceBuilder withFinanceId(Long... financeId) {
        return withArraySetFieldByReflection("financeId", financeId);
    }
}
