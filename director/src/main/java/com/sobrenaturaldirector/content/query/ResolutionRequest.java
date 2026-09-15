package com.sobrenaturaldirector.content.query;

import java.util.*;
import com.sobrenaturaldirector.content.model.*;

public final class ResolutionRequest {
    public enum AdapterPolicy { ALLOW, REJECT }
    public enum FallbackPolicy { NONE, EXPLICIT_ONLY }
    private final Set<SemanticCapability> required,optional;private final Set<ContentKind> kinds;private final List<ProviderId> preferred;private final Set<ProviderId> excluded;private final EvidenceStatus evidence;private final AdapterPolicy adapter;private final FallbackPolicy fallback;
    public ResolutionRequest(Collection<SemanticCapability> required,Collection<SemanticCapability> optional,Collection<ContentKind> kinds,Collection<ProviderId> preferred,Collection<ProviderId> excluded,EvidenceStatus evidence,AdapterPolicy adapter,FallbackPolicy fallback){this.required=set(required);this.optional=set(optional);this.kinds=set(kinds);this.preferred=list(preferred);this.excluded=set(excluded);this.evidence=evidence==null?EvidenceStatus.UNKNOWN:evidence;this.adapter=adapter==null?AdapterPolicy.REJECT:adapter;this.fallback=fallback==null?FallbackPolicy.NONE:fallback;}
    private static <T extends Comparable<T>> Set<T> set(Collection<T> c){TreeSet<T>s=new TreeSet<T>();if(c!=null)s.addAll(c);return Collections.unmodifiableSet(s);}private static <T extends Comparable<T>> List<T> list(Collection<T> c){ArrayList<T>s=new ArrayList<T>();if(c!=null)s.addAll(c);Collections.sort(s);return Collections.unmodifiableList(s);}
    public Set<SemanticCapability> getRequired(){return required;}public Set<SemanticCapability> getOptional(){return optional;}public Set<ContentKind> getKinds(){return kinds;}public List<ProviderId> getPreferred(){return preferred;}public Set<ProviderId> getExcluded(){return excluded;}public EvidenceStatus getRequiredEvidence(){return evidence;}public AdapterPolicy getAdapterPolicy(){return adapter;}public FallbackPolicy getFallbackPolicy(){return fallback;}
}
