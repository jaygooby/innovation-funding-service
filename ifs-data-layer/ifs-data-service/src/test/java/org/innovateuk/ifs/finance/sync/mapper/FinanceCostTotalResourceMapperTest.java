package org.innovateuk.ifs.finance.sync.mapper;

import org.assertj.core.api.AssertionsForClassTypes;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.category.DefaultCostCategory;
import org.innovateuk.ifs.finance.resource.category.FinanceRowCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.finance.resource.cost.Materials;
import org.innovateuk.ifs.finance.resource.cost.OtherCost;
import org.innovateuk.ifs.finance.resource.sync.FinanceCostTotalResource;
import org.innovateuk.ifs.finance.resource.sync.FinanceType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.finance.builder.DefaultCostCategoryBuilder.newDefaultCostCategory;
import static org.innovateuk.ifs.finance.builder.MaterialsCostBuilder.newMaterials;
import static org.innovateuk.ifs.finance.builder.OtherCostBuilder.newOtherCost;
import static org.innovateuk.ifs.finance.builder.sync.FinanceCostTotalResourceBuilder.newFinanceCostTotalResource;

/**
 * TODO: Add description
 */
public class FinanceCostTotalResourceMapperTest {

    private FinanceCostTotalResourceMapper financeCostTotalResourceMapper;

    @Before
    public void init() {
        financeCostTotalResourceMapper = new FinanceCostTotalResourceMapper();
    }

    @Test
    public void mapFromApplicationFinanceResourceList() {
        Long financeId = 1L;

        OtherCost otherCost = newOtherCost()
                .withCost(BigDecimal.valueOf(1000)).build();
        DefaultCostCategory otherCostCategory = newDefaultCostCategory().withCosts(Arrays.asList(otherCost)).build();
        otherCostCategory.calculateTotal();

        Materials materialCost = newMaterials()
                .withCost(BigDecimal.valueOf(500))
                .withQuantity(10).build();
        DefaultCostCategory materialCostCategory = newDefaultCostCategory().withCosts(Arrays.asList(materialCost)).build();
        materialCostCategory.calculateTotal();

        Map<FinanceRowType, FinanceRowCostCategory> costs = new HashMap<>();
        costs.put(FinanceRowType.OTHER_COSTS, otherCostCategory);
        costs.put(FinanceRowType.MATERIALS, materialCostCategory);

        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withId(financeId)
                .withFinanceOrganisationDetails(costs).build();

        List<FinanceCostTotalResource> actualResult =
                financeCostTotalResourceMapper.mapFromApplicationFinanceResourceToList(applicationFinanceResource);

        FinanceCostTotalResource expectedOtherCostTotalResource = newFinanceCostTotalResource()
                .withName(FinanceRowType.OTHER_COSTS.getName())
                .withTotal(BigDecimal.valueOf(1000))
                .withType(FinanceType.APPLICATION)
                .withFinanceId(financeId).build();

        FinanceCostTotalResource expectedMaterialCostTotalResource = newFinanceCostTotalResource()
                .withName(FinanceRowType.MATERIALS.getName())
                .withTotal(BigDecimal.valueOf(5000))
                .withType(FinanceType.APPLICATION)
                .withFinanceId(financeId).build();

        AssertionsForClassTypes.assertThat(actualResult.get(0)).isEqualToComparingFieldByField(expectedOtherCostTotalResource);
        AssertionsForClassTypes.assertThat(actualResult.get(1)).isEqualToComparingFieldByField(expectedMaterialCostTotalResource);
    }

    @Test
    public void mapFromApplicationFinanceResourceListToList() {
        Long financeId = 1L;

        OtherCost otherCost = newOtherCost()
                .withCost(BigDecimal.valueOf(1000)).build();
        DefaultCostCategory otherCostCategory = newDefaultCostCategory().withCosts(Arrays.asList(otherCost)).build();
        otherCostCategory.calculateTotal();

        Materials materialCost = newMaterials()
                .withCost(BigDecimal.valueOf(500))
                .withQuantity(10).build();
        DefaultCostCategory materialCostCategory = newDefaultCostCategory().withCosts(Arrays.asList(materialCost)).build();
        materialCostCategory.calculateTotal();

        Map<FinanceRowType, FinanceRowCostCategory> costs = new HashMap<>();
        costs.put(FinanceRowType.OTHER_COSTS, otherCostCategory);
        costs.put(FinanceRowType.MATERIALS, materialCostCategory);

        List<ApplicationFinanceResource> applicationFinanceResource = newApplicationFinanceResource()
                .withId(financeId)
                .withFinanceOrganisationDetails(costs).build(2);

        List<FinanceCostTotalResource> actualResult =
                financeCostTotalResourceMapper.mapFromApplicationFinanceResourceListToList(applicationFinanceResource);

        FinanceCostTotalResource expectedOtherCostTotalResource = newFinanceCostTotalResource()
                .withName(FinanceRowType.OTHER_COSTS.getName())
                .withTotal(BigDecimal.valueOf(1000))
                .withType(FinanceType.APPLICATION)
                .withFinanceId(financeId).build();

        FinanceCostTotalResource expectedMaterialCostTotalResource = newFinanceCostTotalResource()
                .withName(FinanceRowType.MATERIALS.getName())
                .withTotal(BigDecimal.valueOf(5000))
                .withType(FinanceType.APPLICATION)
                .withFinanceId(financeId).build();

        AssertionsForClassTypes.assertThat(actualResult.get(0)).isEqualToComparingFieldByField(expectedOtherCostTotalResource);
        AssertionsForClassTypes.assertThat(actualResult.get(1)).isEqualToComparingFieldByField(expectedMaterialCostTotalResource);
        AssertionsForClassTypes.assertThat(actualResult.get(2)).isEqualToComparingFieldByField(expectedOtherCostTotalResource);
        AssertionsForClassTypes.assertThat(actualResult.get(3)).isEqualToComparingFieldByField(expectedMaterialCostTotalResource);
    }
}