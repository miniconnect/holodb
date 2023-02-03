package hu.webarticum.holodb.jpa.annotation;

import hu.webarticum.holodb.app.config.HoloConfigColumn.DistributionQuality;

public enum HoloColumnDistributionQuality {
    
    LOW(DistributionQuality.LOW),
    
    MEDIUM(DistributionQuality.MEDIUM),
    
    HIGH(DistributionQuality.HIGH),
    
    UNDEFINED(null),
    
    ;
    
    
    private final DistributionQuality distributionQuality;
    
    
    private HoloColumnDistributionQuality(DistributionQuality distributionQuality) {
        this.distributionQuality = distributionQuality;
    }
    
    
    public DistributionQuality distributionQuality() {
        return distributionQuality;
    }
    
}
