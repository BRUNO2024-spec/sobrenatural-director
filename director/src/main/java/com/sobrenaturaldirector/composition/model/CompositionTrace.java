package com.sobrenaturaldirector.composition.model;

import java.util.*;

public final class CompositionTrace { public static final int MAX_EVENTS=128;private final List<String> events;public CompositionTrace(Collection<String> events){List<String>x=new ArrayList<String>(events==null?Collections.<String>emptyList():events);if(x.size()>MAX_EVENTS)throw new IllegalArgumentException("trace exceeds bound");this.events=Collections.unmodifiableList(x);}public List<String> getEvents(){return events;}}
