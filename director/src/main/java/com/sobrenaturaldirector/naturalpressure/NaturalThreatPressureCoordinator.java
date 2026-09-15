package com.sobrenaturaldirector.naturalpressure;

import java.util.ArrayList; import java.util.Collections; import java.util.Comparator; import java.util.List;

public final class NaturalThreatPressureCoordinator {
    private final List<NaturalThreatPressureDirective> directives = new ArrayList<NaturalThreatPressureDirective>();
    public synchronized void put(NaturalThreatPressureDirective directive){if(directive==null)throw new IllegalArgumentException("directive is required"); remove(directive.getDirectiveId()); directives.add(directive);}
    public synchronized boolean remove(String directiveId){for(int i=0;i<directives.size();i++)if(directives.get(i).getDirectiveId().equals(directiveId)){directives.remove(i);return true;}return false;}
    public synchronized void purgeExpired(long tick){for(int i=directives.size()-1;i>=0;i--)if(directives.get(i).isExpired(tick))directives.remove(i);}
    public synchronized List<NaturalThreatPressureDirective> getDirectives(){return Collections.unmodifiableList(new ArrayList<NaturalThreatPressureDirective>(directives));}
    public synchronized NaturalThreatPressureResolution resolve(String provider,String category,int dimension,int chunkX,int chunkZ,long tick,boolean providerAvailable){purgeExpired(tick); NaturalThreatPressureDirective winner=null; for(NaturalThreatPressureDirective d:directives) if(d.getProvider().equals(provider)&& (d.getCategory().length()==0||d.getCategory().equals(category)) && d.getScope().contains(dimension,chunkX,chunkZ) && (winner==null||compare(d,winner)<0)) winner=d; if(!providerAvailable)return new NaturalThreatPressureResolution(NaturalThreatPressureLevel.NORMAL,winner==null?"":winner.getDirectiveId(),"provider-unavailable",false); return winner==null?new NaturalThreatPressureResolution(NaturalThreatPressureLevel.NORMAL,"","no-directive",true):new NaturalThreatPressureResolution(winner.getLevel(),winner.getDirectiveId(),winner.getReason(),true);}
    private static int compare(NaturalThreatPressureDirective a,NaturalThreatPressureDirective b){int v=Integer.compare(b.getPriority(),a.getPriority()); if(v!=0)return v; v=Boolean.compare(a.getScope().isWorldWide(),b.getScope().isWorldWide()); if(v!=0)return v; v=Long.compare(b.getCreatedAt(),a.getCreatedAt()); return v!=0?v:a.getDirectiveId().compareTo(b.getDirectiveId());}
}
