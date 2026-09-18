package com.sobrenaturaldirector.control;

import com.sobrenaturaldirector.domain.StableId;

/** Immutable bounded lease; live runtime objects are deliberately absent. */
public final class ControlLease {
    private final String leaseId, source; private final long startTick, expiryTick; private final RestorationPolicy restoration; private final boolean active;
    public ControlLease(String id,String source,long start,long expiry,RestorationPolicy restoration,boolean active){leaseId=StableId.require(id,"leaseId");this.source=StableId.require(source,"leaseSource");if(start<0||expiry<start)throw new IllegalArgumentException("invalid lease interval");if(restoration==null)throw new IllegalArgumentException("restoration");startTick=start;expiryTick=expiry;this.restoration=restoration;this.active=active;}
    public String getLeaseId(){return leaseId;} public String getSource(){return source;} public long getStartTick(){return startTick;} public long getExpiryTick(){return expiryTick;} public RestorationPolicy getRestoration(){return restoration;} public boolean isActive(){return active;}
    public boolean validAt(long tick){return active&&tick>=startTick&&tick<=expiryTick;}
}
