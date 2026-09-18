package com.sobrenaturaldirector.runtimeprobe;

import java.util.*;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.WorldServer;
import net.minecraft.nbt.NBTTagCompound;
import com.sobrenaturaldirector.SobrenaturalDirector;
import com.sobrenaturaldirector.action.*;
import com.sobrenaturaldirector.composition.model.*;
import com.sobrenaturaldirector.control.*;
import com.sobrenaturaldirector.persistence.*;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.situation.*;
import com.sobrenaturaldirector.narrative.*;

/** Disposable-only narrative closure probe using the real reobfuscated provider stack. */
@Mod(modid="sobrenaturaldirector_narrative_situation_probe", name="Director Narrative Situation Probe", version="NARRATIVE-SITUATION-1")
public final class NarrativeSituationClosureProbe {
    private boolean done;
    @Mod.EventHandler public void init(FMLInitializationEvent e){FMLCommonHandler.instance().bus().register(this);}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||done)return;
        WorldServer world=net.minecraftforge.common.DimensionManager.getWorld(0);if(world==null||SobrenaturalDirector.getActionPlanExecutor()==null)return;done=true;
        try { run(world); log("DEDICATED_NARRATIVE_E2E=PASS"); } catch(Throwable t){log("DEDICATED_NARRATIVE_E2E=FAIL "+t.getClass().getName()+" "+t.getMessage());}
    }
    private void run(WorldServer world){
        long tick=world.getTotalWorldTime(); DimensionRef d=new DimensionRef(world.provider.dimensionId,"runtime",Collections.singleton("narrative-e2e"),true); DirectorWorldSavedData saved=saved(world); SituationMemory memory=new SituationMemory();
        new NarrativeThreadManager().create(saved,"thread-narrative-e2e","investigation",d.getDimensionId(),8,8,Collections.singleton("investigation"),tick,"E2E");
        SituationContext investigationContext=new SituationContext(d.getDimensionId(),tick,8,8,77L,true,true,true,true,Collections.singleton("forest"));
        SituationInstance investigation=new SituationIntelligencePipeline().composeExecutable(investigationContext,memory,registry(),SobrenaturalDirector.getActionCatalog(),"thread-narrative-e2e");
        SituationExecutionService.Result ir=service().executeAndPersist(investigation,specs(investigation,world,d,tick),context(world),memory,tick,saved,new SituationPersistenceCoordinator());
        log("INVESTIGATION="+investigation.getGoal()+" state="+ir.getInstance().getState()+" action="+ir.getOutcome().getStatus()+" reason="+ir.getOutcome().getReason()+" journal="+journal(investigation)); require(ir.getOutcome().getStatus()==ActionExecutionStatus.COMPLETED,"investigation provider execution");
        NBTTagCompound roundTrip=new NBTTagCompound();saved.writeToNBT(roundTrip);DirectorWorldSavedData restored=new DirectorWorldSavedData();restored.readFromNBT(roundTrip);require(restored.getSituation(investigation.getId())!=null,"situation restart restore");log("RESTART_RESTORE=PASS id="+investigation.getId());
        memory.record(new SituationMemoryEntry("penalty-investigation",SituationGoal.INVESTIGATION,tick,d.getDimensionId(),8,"penalty",Collections.<com.sobrenaturaldirector.content.model.ProviderId>emptyList(),0));
        SituationContext discoveryContext=new SituationContext(d.getDimensionId(),tick+1,9,9,77L,false,false,false,true,Collections.singleton("forest"));
        SituationInstance discovery=new SituationIntelligencePipeline().composeExecutable(discoveryContext,memory,registry(),SobrenaturalDirector.getActionCatalog(),"thread-narrative-e2e");
        SituationExecutionService.Result dr=service().executeAndPersist(discovery,specs(discovery,world,d,tick+1),context(world),memory,tick+1,saved,new SituationPersistenceCoordinator());
        log("DISCOVERY="+discovery.getGoal()+" state="+dr.getInstance().getState()+" action="+dr.getOutcome().getStatus()+" reason="+dr.getOutcome().getReason()); require(dr.getOutcome().getStatus()==ActionExecutionStatus.COMPLETED,"discovery provider execution");
        memory.record(new SituationMemoryEntry("penalty-discovery",SituationGoal.DISCOVERY,tick+1,d.getDimensionId(),9,"penalty",Collections.<com.sobrenaturaldirector.content.model.ProviderId>emptyList(),0));
        for(int i=2;i<6;i++){memory.record(new SituationMemoryEntry("penalty-investigation-"+i,SituationGoal.INVESTIGATION,tick+2,d.getDimensionId(),10,"penalty",Collections.<com.sobrenaturaldirector.content.model.ProviderId>emptyList(),0));memory.record(new SituationMemoryEntry("penalty-discovery-"+i,SituationGoal.DISCOVERY,tick+2,d.getDimensionId(),10,"penalty",Collections.<com.sobrenaturaldirector.content.model.ProviderId>emptyList(),0));}
        PersistentNarrativeThread linkedThread=saved.getNarrativeThread("thread-narrative-e2e"); require(linkedThread!=null&&linkedThread.getSituationIds().size()==2,"multi-situation thread membership"); log("THREAD_MULTI_SITUATION=PASS members="+linkedThread.getSituationIds().size());
        SituationContext ambientContext=new SituationContext(d.getDimensionId(),tick+2,10,10,77L,false,false,false,false,Collections.singleton("forest"));
        SituationInstance ambient=new SituationIntelligencePipeline().composeExecutable(ambientContext,memory,registry(),SobrenaturalDirector.getActionCatalog(),"");
        SituationExecutionService.Result ar=service().executeAndPersist(ambient,Collections.<String,ActionExecutionSpec>emptyMap(),context(world),memory,tick+2,saved,new SituationPersistenceCoordinator());
        log("AMBIENT_EVENT="+ambient.getGoal()+" state="+ar.getInstance().getState()+" action="+ar.getOutcome().getStatus()+" reason="+ar.getOutcome().getReason()); require(ar.getOutcome().getStatus()==ActionExecutionStatus.COMPLETED,"ambient action execution");
        require(ambient.getGoal()==SituationGoal.AMBIENT_EVENT,"ambient goal selection"); new NarrativeThreadManager().resolve(saved,"thread-narrative-e2e",tick+3,"E2E_RESOLVED"); log("THREAD_RESOLUTION=PASS");
    }
    private DirectorProviderRegistry registry(){return SobrenaturalDirector.getProviderRegistry();}
    private SituationExecutionService service(){return new SituationExecutionService(SobrenaturalDirector.getActionPlanExecutor(),registry());}
    private Map<String,ActionExecutionSpec> specs(SituationInstance s,WorldServer w,DimensionRef d,long tick){Map<String,ActionExecutionSpec> out=new LinkedHashMap<String,ActionExecutionSpec>();String key=s.getId();int[] p=placement(w);int x=p[0],y=p[1],z=p[2];log("PLACEMENT_FIXTURE="+x+":"+y+":"+z+" air="+w.isAirBlock(x,y,z)+" belowSolid="+w.getBlock(x,y-1,z).getMaterial().isSolid());for(ActionPlanNode n:s.getActionPlan().getNodes()){if("PLACE_STRUCTURE".equals(n.getActionId())){DirectorOwnedCompositionPlan c=new DirectorOwnedCompositionPlan(key+"-composition","narrative-e2e","GraveStone","2.13.0",d.getDimensionId(),x,y,z,Collections.singletonList(new CompositionBlockOperation(key+"-block",0,0,0,new ExternalBlockReference("GraveStone","GSBoneBlock",0))));out.put(n.getNodeId(),new ActionExecutionSpec(null,null,c,lease(key+"-structure",tick),ControlAuthority.FULL_DIRECTOR_CONTROL,key+"-structure","Narrative Structure"));}else if("SPAWN_ACTOR".equals(n.getActionId()))out.put(n.getNodeId(),new ActionExecutionSpec(new EntityRef(key+"-actor",d,EntityOwnership.DIRECTOR_SPAWNED),new WorldPositionRef(d,x,y,z),null,lease(key+"-actor",tick),ControlAuthority.FULL_DIRECTOR_CONTROL,key+"-actor","Narrative Witness"));}return out;}
    private int[] placement(WorldServer w){for(int x=-32;x<=32;x++)for(int z=-32;z<=32;z++)for(int y=2;y<200;y++)if(w.isAirBlock(x,y,z)&&w.getBlock(x,y-1,z).getMaterial().isSolid())return new int[]{x,y,z};throw new IllegalStateException("no safe placement position");}
    private ControlLease lease(String id,long tick){return new ControlLease("narrative-lease-"+id,"narrative-e2e",Math.max(0,tick-1),tick+2000,RestorationPolicy.RESTORABLE,true);}
    private ControlExecutionContext context(WorldServer w){return new ControlExecutionContext(w,saved(w));}
    private DirectorWorldSavedData saved(WorldServer w){DirectorWorldSavedData d=(DirectorWorldSavedData)w.perWorldStorage.loadData(DirectorWorldSavedData.class,DirectorPersistenceMetadata.DATA_NAME);if(d==null){d=new DirectorWorldSavedData(DirectorPersistenceMetadata.DATA_NAME);w.perWorldStorage.setData(DirectorPersistenceMetadata.DATA_NAME,d);}return d;}
    private void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
    private String journal(SituationInstance s){ActionPlanJournal j=SobrenaturalDirector.getActionPlanExecutor().journal(s.getActionPlan().getPlanId());if(j==null)return "NONE";StringBuilder b=new StringBuilder();for(ActionPlanJournal.Entry e:j.getEntries().values())b.append(e.getStepId()).append('=').append(e.getResult()).append(':').append(e.getReason()).append(';');return b.toString();}
    private void log(String v){System.out.println("[NARRATIVE-SITUATION-PROBE] "+v);}
}
