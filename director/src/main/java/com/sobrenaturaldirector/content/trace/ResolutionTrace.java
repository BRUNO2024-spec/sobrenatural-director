package com.sobrenaturaldirector.content.trace;

import java.util.*;

public final class ResolutionTrace {private final String requestId;private final int examined,returned;private final List<String> reasons;public ResolutionTrace(String id,int examined,int returned,Collection<String> reasons){requestId=id;this.examined=examined;this.returned=returned;ArrayList<String>x=new ArrayList<String>();if(reasons!=null)x.addAll(reasons);if(x.size()>16)x=new ArrayList<String>(x.subList(0,16));this.reasons=Collections.unmodifiableList(x);}public String getRequestId(){return requestId;}public int getExamined(){return examined;}public int getReturned(){return returned;}public List<String> getReasons(){return reasons;}}
