package org.innovateuk.ifs.finance.resource.cost;

public enum OverheadRateType{
    NONE(null, null, "None"),
    DEFAULT_PERCENTAGE(20, "defaultPercentage", "Default %"),
    TOTAL(null, "total", "Custom Amount");

    private final Integer rate;
    private final String name;
    private final String label;

    OverheadRateType(Integer rate, String name, String label) {
        this.rate = rate;
        this.name = name;
        this.label = label;
    }

    public Integer getRate() {
        return rate;
    }
    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
}