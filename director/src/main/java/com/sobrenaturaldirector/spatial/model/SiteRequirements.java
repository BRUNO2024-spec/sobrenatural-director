package com.sobrenaturaldirector.spatial.model;
import java.util.*;
public final class SiteRequirements {private final Set<String> requirements;public SiteRequirements(Collection<String> values){TreeSet<String>x=new TreeSet<String>();if(values!=null)x.addAll(values);requirements=Collections.unmodifiableSet(x);}public Set<String> getRequirements(){return requirements;}public boolean contains(String value){return requirements.contains(value);}}
