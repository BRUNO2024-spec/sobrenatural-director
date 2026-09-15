package com.sobrenaturaldirector.derivation;
import java.util.*; import com.sobrenaturaldirector.decision.model.*; import com.sobrenaturaldirector.model.history.HistorySummary; import com.sobrenaturaldirector.model.player.PlayerModel; import com.sobrenaturaldirector.model.world.WorldModel;
/** Pure mapper only. It never invokes DecisionEngine. */
public final class DecisionContextAssembler {
 public DecisionContext assemble(PlayerModel p,WorldModel w,HistorySummary h){Map<String,Long> cooldowns=new TreeMap<String,Long>(h.getCooldowns());Map<String,Double> budgets=new TreeMap<String,Double>();budgets.put("threat",10.0);return new DecisionContext(w==null?0:w.getWorldAgeTicks(),w==null?0:w.getWorldAgeTicks(),0,0,0,0,value(p.getHealthRatio(),0),value(p.getCombatReadiness(),0),0,false,false,false,0,budgets,Collections.<String,ProviderStatus>emptyMap(),Collections.<String>emptySet(),Collections.<String>emptySet(),h.getClaims(),cooldowns);}
 public Map<UUID,DecisionContext> assemblePerPlayer(DerivedModels models,HistorySummary h){Map<UUID,DecisionContext> r=new TreeMap<UUID,DecisionContext>();for(PlayerModel p:models.getPlayers()){WorldModel world=null;for(WorldModel x:models.getWorlds())if(x.getDimension()==p.getDimension()){world=x;break;}if(world!=null)r.put(p.getPlayerId(),assemble(p,world,h));}return Collections.unmodifiableMap(r);}
 private static double value(DerivedSignal s,double fallback){return s!=null&&s.getValue()!=null?s.getValue():fallback;}
}
