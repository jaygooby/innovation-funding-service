package com.worth.ifs.finance.resource.cost;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * {@code SubContractingCost} implements {@link CostItem}
 */
public class SubContractingCost implements CostItem {
    private Long id;

    @NotNull
    @DecimalMin(value = "1")
    @Digits(integer = MAX_DIGITS, fraction = 0)
    private BigDecimal cost;

    @NotBlank
    @Length(max = MAX_STRING_LENGTH, message = "{org.hibernate.validator.constraints.MaxLength.message}")
    private String country;

    @NotBlank
    @Length(max = MAX_STRING_LENGTH, message = "{org.hibernate.validator.constraints.MaxLength.message}")
    private String name;

    @NotBlank
    @Length(max = MAX_STRING_LENGTH, message = "{org.hibernate.validator.constraints.MaxLength.message}")
    private String role;

    public SubContractingCost(){
    }

    public SubContractingCost(Long id, BigDecimal cost, String country, String name, String role) {
        this();
        this.id = id;
        this.cost = cost;
        this.country = country;
        this.name = name;
        this.role = role;
    }

    @Override
    public Long getId() {
        return id;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public String getCountry() {

        return (StringUtils.length(country) >  MAX_DB_STRING_LENGTH ? country.substring(0, MAX_DB_STRING_LENGTH) : country);
    }

    @Override
    public String getName() {
        return (StringUtils.length(name) >  MAX_DB_STRING_LENGTH ? name.substring(0, MAX_DB_STRING_LENGTH) : name);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getMinRows() {
        return 0;
    }

    public String getRole() {
        return (StringUtils.length(role) >  MAX_DB_STRING_LENGTH ? role.substring(0, MAX_DB_STRING_LENGTH) : role);
    }

    @Override
    public BigDecimal getTotal() {
        return cost;
    }

    @Override
    public CostType getCostType() {
        return CostType.SUBCONTRACTING_COSTS;
    }
}
