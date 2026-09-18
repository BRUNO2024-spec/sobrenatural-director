package com.sobrenaturaldirector.derivation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;
import com.sobrenaturaldirector.narrative.PacingAssessment;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Existing runtime sources supplied to the context adapter; no gameplay logic. */
public final class DecisionContextLiveSources {
    private final PacingAssessment pacing;
    private final SemanticRegionProfile region;
    private final DirectorProviderRegistry providers;
    private final int eventConcurrency;
    private final Set<String> contentTags;
    private final Set<String> locationTags;
    public DecisionContextLiveSources(PacingAssessment pacing, SemanticRegionProfile region,
            DirectorProviderRegistry providers, int eventConcurrency, Set<String> contentTags,
            Set<String> locationTags) {
        if (eventConcurrency < 0) throw new IllegalArgumentException("eventConcurrency must be non-negative");
        this.pacing=pacing; this.region=region; this.providers=providers; this.eventConcurrency=eventConcurrency;
        this.contentTags=frozen(contentTags); this.locationTags=frozen(locationTags);
    }
    private static Set<String> frozen(Set<String> values) { return Collections.unmodifiableSet(new LinkedHashSet<String>(values==null?Collections.<String>emptySet():values)); }
    public PacingAssessment getPacing(){return pacing;} public SemanticRegionProfile getRegion(){return region;}
    public DirectorProviderRegistry getProviders(){return providers;} public int getEventConcurrency(){return eventConcurrency;}
    public Set<String> getContentTags(){return contentTags;} public Set<String> getLocationTags(){return locationTags;}
}
