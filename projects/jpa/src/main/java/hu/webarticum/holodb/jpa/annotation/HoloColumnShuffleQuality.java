package hu.webarticum.holodb.jpa.annotation;

import hu.webarticum.holodb.app.config.HoloConfigColumn.ShuffleQuality;

public enum HoloColumnShuffleQuality {

    NOOP(ShuffleQuality.NOOP),
    
    VERY_LOW(ShuffleQuality.VERY_LOW),
    
    LOW(ShuffleQuality.LOW),
    
    MEDIUM(ShuffleQuality.MEDIUM),
    
    HIGH(ShuffleQuality.HIGH),
    
    VERY_HIGH(ShuffleQuality.VERY_HIGH),
    
    UNDEFINED(null),
    
    ;
    
    
    private final ShuffleQuality shuffleQuality;
    
    
    private HoloColumnShuffleQuality(ShuffleQuality shuffleQuality) {
        this.shuffleQuality = shuffleQuality;
    }
    
    
    public ShuffleQuality shuffleQuality() {
        return shuffleQuality;
    }
    
}
