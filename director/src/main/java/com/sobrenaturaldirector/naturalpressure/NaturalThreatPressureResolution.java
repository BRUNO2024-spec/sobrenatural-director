package com.sobrenaturaldirector.naturalpressure;

public final class NaturalThreatPressureResolution {
    private final NaturalThreatPressureLevel level; private final String directiveId, reason; private final boolean providerAvailable;
    public NaturalThreatPressureResolution(NaturalThreatPressureLevel level, String directiveId, String reason, boolean providerAvailable){this.level=level;this.directiveId=directiveId==null?"":directiveId;this.reason=reason==null?"":reason;this.providerAvailable=providerAvailable;}
    public NaturalThreatPressureLevel getLevel(){return level;} public String getDirectiveId(){return directiveId;} public String getReason(){return reason;} public boolean isProviderAvailable(){return providerAvailable;}
}
