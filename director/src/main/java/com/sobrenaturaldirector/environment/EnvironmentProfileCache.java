package com.sobrenaturaldirector.environment;

import java.util.LinkedHashMap;
import java.util.Map;
import com.sobrenaturaldirector.environment.model.RegionKey;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;

/** Bounded transient cache; raw blocks are never retained. */
public final class EnvironmentProfileCache {
    public static final int MAX_PROFILES = 128;
    private final Map<RegionKey,SemanticRegionProfile> values = new LinkedHashMap<RegionKey,SemanticRegionProfile>(MAX_PROFILES, .75f, true) {
        private static final long serialVersionUID = 1L;
        @Override protected boolean removeEldestEntry(Map.Entry<RegionKey,SemanticRegionProfile> eldest) { return size()>MAX_PROFILES; }
    };
    public void put(SemanticRegionProfile profile){if(profile==null)throw new IllegalArgumentException("profile is required");values.put(profile.getEvidence().getRegion(),profile);}
    public SemanticRegionProfile get(RegionKey key){return values.get(key);}
    public int size(){return values.size();}
    public Map<RegionKey,SemanticRegionProfile> snapshot(){return new LinkedHashMap<RegionKey,SemanticRegionProfile>(values);}
}
