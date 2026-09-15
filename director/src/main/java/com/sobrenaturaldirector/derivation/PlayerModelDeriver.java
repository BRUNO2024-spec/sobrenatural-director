package com.sobrenaturaldirector.derivation;
import java.util.*; import com.sobrenaturaldirector.model.player.*; import com.sobrenaturaldirector.observation.model.*;
public final class PlayerModelDeriver {
    public PlayerDerivation derive(PlayerSnapshot p,boolean partial){List<DerivedSignal>s=new ArrayList<DerivedSignal>();List<String>w=new ArrayList<String>();
        Double health=p.getMaxHealth()>0?clamp(p.getHealth()/p.getMaxHealth()):null; Confidence hc=health==null?Confidence.UNKNOWN:(partial?Confidence.PARTIAL:Confidence.KNOWN); HealthBand band=health==null?HealthBand.UNKNOWN:(health<.2?HealthBand.CRITICAL:(health<.5?HealthBand.LOW:(health<.8?HealthBand.STABLE:HealthBand.HEALTHY)));
        s.add(new DerivedSignal("DER-PLAYER-HEALTH-001",health,"HEALTH_RATIO",hc,Provenance.DERIVED));
        Double hunger=clamp(p.getFoodLevel()/20.0); Double armor=clamp(p.getArmorValue()/20.0); Confidence raw=partial?Confidence.PARTIAL:Confidence.KNOWN;
        s.add(new DerivedSignal("DER-PLAYER-HUNGER-001",hunger,"HUNGER_RATIO",raw,Provenance.DERIVED));s.add(new DerivedSignal("DER-PLAYER-ARMOR-001",armor,"ARMOR_RATIO",Confidence.PARTIAL,Provenance.DERIVED));
        int occupied=p.getInventory().size(),stacks=0,armorSlots=0,damaged=0;for(InventorySlotSnapshot x:p.getInventory()){stacks+=x.getStackCount();if("ARMOR".equals(x.getSlotKind()))armorSlots++;if(x.isDamageable()&&x.getItemDamage()>0)damaged++;}
        s.add(new DerivedSignal("DER-PLAYER-EQUIPMENT-001",armor,"EQUIPMENT_READINESS",Confidence.PARTIAL,Provenance.INFERRED));
        s.add(new DerivedSignal("DER-PLAYER-COMBAT-001",null,"UNKNOWN",Confidence.UNKNOWN,Provenance.UNKNOWN));s.add(new DerivedSignal("DER-PLAYER-RANGED-001",null,"UNKNOWN",Confidence.UNKNOWN,Provenance.UNKNOWN));s.add(new DerivedSignal("DER-PLAYER-MELEE-001",null,"UNKNOWN",Confidence.UNKNOWN,Provenance.UNKNOWN));s.add(new DerivedSignal("DER-PLAYER-HEALING-001",null,"UNKNOWN",Confidence.UNKNOWN,Provenance.UNKNOWN));s.add(new DerivedSignal("DER-PLAYER-ESCAPE-001",null,"UNKNOWN",Confidence.UNKNOWN,Provenance.UNKNOWN));s.add(new DerivedSignal("DER-PLAYER-EXPLORATION-001",null,"DEFERRED",Confidence.UNKNOWN,Provenance.UNKNOWN));
        double survival=health==null?0:(health*.7+hunger*.15+armor*.15);s.add(new DerivedSignal("DER-PLAYER-SURVIVAL-001",health==null?null:survival,"SURVIVABILITY",health==null?Confidence.UNKNOWN:Confidence.PARTIAL,Provenance.INFERRED));
        if(partial)w.add("DERIVATION_INPUT_PARTIAL"); PlayerModel model=new PlayerModel(p.getPlayerId(),p.getDimension(),s.get(0),band,s.get(1),s.get(2),s.get(3),s.get(4),s.get(10),s.get(5),s.get(6),s.get(7),s.get(8),s.get(9),occupied,128-occupied,stacks,armorSlots,damaged,p.isAlive(),partial?Confidence.PARTIAL:Confidence.KNOWN);return new PlayerDerivation(model,new DerivationTrace(s,w)); }
    private static double clamp(double x){return x<0?0:(x>1?1:x);}
}
