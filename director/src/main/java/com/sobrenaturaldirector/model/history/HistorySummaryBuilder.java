package com.sobrenaturaldirector.model.history;
import java.util.*;
public final class HistorySummaryBuilder { public HistorySummary build(Map<String,Integer> intents,Map<String,Integer> themes,Map<String,Integer> providers,Map<String,Long> cooldowns,Set<String> claims,boolean recentMajorEvent,long referenceTick,long lastMajorTick){return new HistorySummary(intents,themes,providers,cooldowns,claims,recentMajorEvent,lastMajorTick<0?Long.MAX_VALUE:Math.max(0,referenceTick-lastMajorTick));} public HistorySummary empty(){return new HistorySummary(null,null,null,null,null,false,Long.MAX_VALUE);} }
