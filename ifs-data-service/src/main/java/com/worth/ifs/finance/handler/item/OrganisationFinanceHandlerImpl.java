package com.worth.ifs.finance.handler.item;

import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.repository.CostRepository;
import com.worth.ifs.finance.resource.category.CostCategory;
import com.worth.ifs.finance.resource.category.DefaultCostCategory;
import com.worth.ifs.finance.resource.category.LabourCostCategory;
import com.worth.ifs.finance.resource.category.OtherFundingCostCategory;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.CostType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

/**
 * OrganisationFinanceHandlerImpl maintains the finances from
 * an organisation's perspective and calculates the totals
 */
@Component
public class OrganisationFinanceHandlerImpl implements OrganisationFinanceHandler {
    private final Log log = LogFactory.getLog(getClass());
    EnumMap<CostType, CostCategory> costCategories = new EnumMap<>(CostType.class);

    @Autowired
    CostRepository costRepository;

    @Override
    public EnumMap<CostType, CostCategory> getOrganisationFinances(Long applicationFinanceId) {
        List<Cost> costs = costRepository.findByApplicationFinanceId(applicationFinanceId);
        createCostCategories();
        addCostsToCategories(costs);
        calculateTotals();
        return costCategories;
    }

    @Override
    public EnumMap<CostType, CostCategory> getOrganisationFinanceTotals(Long applicationFinanceId) {
        getOrganisationFinances(applicationFinanceId);
        resetCosts();
        return costCategories;
    }

    private void createCostCategories() {
        for(CostType costType : CostType.values()) {
            CostCategory costCategory = createCostCategoryByType(costType);
            costCategories.put(costType, costCategory);
        }
    }

    private void addCostsToCategories(List<Cost> costs) {
        costs.stream().forEach(c -> addCostToCategory(c));
    }

    /**
     * The costs are converted to a representation based on its type that can be used in the view and
     * are added to a specific category (e.g. labour).
     * @param cost Cost to be added
     */
    private void addCostToCategory(Cost cost) {
        CostType costType = CostType.fromString(cost.getQuestion().getFormInputs().get(0).getFormInputType().getTitle());
        CostHandler costHandler = getCostHandler(costType);
        CostItem costItem = costHandler.toCostItem(cost);
        CostCategory costCategory = costCategories.get(costType);
        costCategory.addCost(costItem);
    }

    private void calculateTotals() {
        costCategories.values()
                .forEach(cc -> cc.calculateTotal());
    }

    private void resetCosts() {
        costCategories.values()
                .forEach(cc -> cc.setCosts(new ArrayList<>()));
    }

    public Cost costItemToCost(CostItem costItem) {
        CostHandler costHandler = getCostHandler(costItem.getCostType());
        return costHandler.toCost(costItem);
    }

    public List<Cost> costItemsToCost(List<CostItem> costItems) {
        List<Cost> costs = new ArrayList<>();
        costItems.stream()
                .forEach(costItem -> costs.add(costItemToCost(costItem)));
        return costs;
    }

    private CostHandler getCostHandler(CostType costType) {
        switch(costType) {
            case LABOUR:
                return new LabourCostHandler();
            case CAPITAL_USAGE:
                return new CapitalUsageHandler();
            case MATERIALS:
                return new MaterialsHandler();
            case OTHER_COSTS:
                return new OtherCostHandler();
            case OVERHEADS:
                return new OverheadsHandler();
            case SUBCONTRACTING_COSTS:
                return new SubContractingCostHandler();
            case TRAVEL:
                return new TravelCostHandler();
            case FINANCE:
                return new GrantClaimHandler();
            case OTHER_FUNDING:
                return new OtherFundingHandler();
        }
        log.error("Not a valid FinanceType: " + costType);
        throw new IllegalArgumentException("Not a valid FinanceType: " + costType);
    }

    private CostCategory createCostCategoryByType(CostType costType) {
        switch(costType) {
            case LABOUR:
                return new LabourCostCategory();
            case OTHER_FUNDING:
                return new OtherFundingCostCategory();
            default:
                return new DefaultCostCategory();
        }
    }
}
