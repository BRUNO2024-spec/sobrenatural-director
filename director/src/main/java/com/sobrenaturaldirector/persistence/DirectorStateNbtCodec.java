package com.sobrenaturaldirector.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import com.sobrenaturaldirector.domain.DirectorWorldState;
import com.sobrenaturaldirector.domain.DirectorWorldState.CleanupTask;
import com.sobrenaturaldirector.domain.DirectorWorldState.ContentUsageRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.CooldownRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.DirectorEventState;
import com.sobrenaturaldirector.domain.DirectorWorldState.DirectorPlanState;
import com.sobrenaturaldirector.domain.DirectorWorldState.DirectorPlayerState;
import com.sobrenaturaldirector.domain.DirectorWorldState.KnownLocation;
import com.sobrenaturaldirector.domain.DirectorWorldState.MemoryRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.MutationRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.NarrativeArcState;
import com.sobrenaturaldirector.domain.DirectorWorldState.ProviderState;
import com.sobrenaturaldirector.domain.DirectorWorldState.ReservedRegion;
import com.sobrenaturaldirector.domain.DirectorWorldState.StructureRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.UniqueClaim;

/** Only persistence translation belongs here; domain classes do not know NBT. */
public final class DirectorStateNbtCodec {
    public static final String SCHEMA = "schemaVersion";
    public static final String SEED = "directorSeed";
    public static final String PAUSED = "paused";
    private static final int COMPOUND = 10;
    private static final int MAX = DirectorWorldState.MAX_RECORDS;

    private DirectorStateNbtCodec() { }

    public static NBTTagCompound encode(DirectorWorldState state) {
        NBTTagCompound root = new NBTTagCompound();
        root.setInteger(SCHEMA, DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION);
        root.setLong(SEED, state.getDirectorSeed());
        root.setBoolean(PAUSED, state.isPaused());
        root.setTag("players", players(state.getPlayers()));
        root.setTag("locations", locations(state.getKnownLocations()));
        root.setTag("reservations", reservations(state.getReservations()));
        root.setTag("memories", memories(state.getMemories()));
        root.setTag("uniqueClaims", claims(state.getUniqueClaims()));
        root.setTag("cooldowns", cooldowns(state.getCooldowns()));
        root.setTag("providers", providers(state.getProviders()));
        root.setTag("contentUsage", usage(state.getContentUsage()));
        root.setTag("cleanupTasks", cleanup(state.getCleanupTasks()));
        root.setTag("arcs", arcs(state.getArcs()));
        root.setTag("plans", plans(state.getPlans()));
        root.setTag("events", events(state.getEvents()));
        root.setTag("structures", structures(state.getStructures()));
        root.setTag("mutations", mutations(state.getMutations()));
        return root;
    }

    public static DirectorWorldState decode(NBTTagCompound root) {
        if (root == null || !root.hasKey(SCHEMA)) throw new PersistenceException("MISSING_SCHEMA");
        if (!root.hasKey(SCHEMA, 3)) throw new PersistenceException("CORRUPT_SCHEMA_TYPE");
        int schema = root.getInteger(SCHEMA);
        if (schema > DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION) throw new PersistenceException("UNSUPPORTED_FUTURE_SCHEMA");
        if (schema < DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION) throw new PersistenceException("UNSUPPORTED_OLD_SCHEMA");
        if (!root.hasKey(SEED, 4) || !root.hasKey(PAUSED, 1)) throw new PersistenceException("CORRUPT_REQUIRED_ROOT");
        return new DirectorWorldState(root.getLong(SEED), root.getBoolean(PAUSED), readPlayers(root,"players"), readLocations(root,"locations"), readReservations(root,"reservations"), readMemories(root,"memories"), readClaims(root,"uniqueClaims"), readCooldowns(root,"cooldowns"), readProviders(root,"providers"), readUsage(root,"contentUsage"), readCleanup(root,"cleanupTasks"), readArcs(root,"arcs"), readPlans(root,"plans"), readEvents(root,"events"), readStructures(root,"structures"), readMutations(root,"mutations"));
    }

    private static NBTTagList list() { return new NBTTagList(); }
    private static void add(NBTTagList list, NBTTagCompound value) { list.appendTag(value); }
    private static NBTTagList players(List<DirectorPlayerState> values){NBTTagList out=list();for(DirectorPlayerState x:values){NBTTagCompound n=new NBTTagCompound();n.setLong("uuidMost",x.playerId.getMostSignificantBits());n.setLong("uuidLeast",x.playerId.getLeastSignificantBits());n.setString("knowledgeState",x.knowledgeState);n.setInteger("recentStress",x.recentStress);add(out,n);}return out;}
    private static NBTTagList locations(List<KnownLocation> values){NBTTagList out=list();for(KnownLocation x:sortedLocations(values)){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.locationId);n.setInteger("dimension",x.dimension);n.setInteger("centerX",x.centerX);n.setInteger("centerY",x.centerY);n.setInteger("centerZ",x.centerZ);n.setLong("firstSeenTick",x.firstSeenTick);n.setLong("lastSeenTick",x.lastSeenTick);n.setInteger("visitCount",x.visitCount);n.setString("knowledgeState",x.knowledgeState);n.setBoolean("directorModified",x.directorModified);n.setTag("tags",strings(x.tags));add(out,n);}return out;}
    private static NBTTagList reservations(List<ReservedRegion> values){NBTTagList out=list();for(ReservedRegion x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.reservationId);n.setString("ownerId",x.ownerId);n.setInteger("dimension",x.dimension);n.setInteger("minX",x.minX);n.setInteger("minY",x.minY);n.setInteger("minZ",x.minZ);n.setInteger("maxX",x.maxX);n.setInteger("maxY",x.maxY);n.setInteger("maxZ",x.maxZ);n.setString("reason",x.reason);n.setLong("createdTick",x.createdTick);n.setLong("expiryTick",x.expiryTick);add(out,n);}return out;}
    private static NBTTagList memories(List<MemoryRecord> values){NBTTagList out=list();for(MemoryRecord x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.memoryId);n.setString("category",x.category);n.setString("retention",x.retention);n.setLong("createdTick",x.createdTick);n.setLong("expiryTick",x.expiryTick);add(out,n);}return out;}
    private static NBTTagList claims(List<UniqueClaim> values){NBTTagList out=list();for(UniqueClaim x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.claimId);n.setString("resourceKey",x.resourceKey);n.setString("ownerId",x.ownerId);n.setString("state",x.state);n.setLong("createdTick",x.createdTick);add(out,n);}return out;}
    private static NBTTagList cooldowns(List<CooldownRecord> values){NBTTagList out=list();for(CooldownRecord x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.cooldownId);n.setString("scope",x.scope);n.setLong("startTick",x.startTick);n.setLong("endTick",x.endTick);add(out,n);}return out;}
    private static NBTTagList providers(List<ProviderState> values){NBTTagList out=list();for(ProviderState x:values){NBTTagCompound n=new NBTTagCompound();n.setString("key",x.providerKey);n.setString("status",x.status);n.setInteger("failureCount",x.failureCount);add(out,n);}return out;}
    private static NBTTagList usage(List<ContentUsageRecord> values){NBTTagList out=list();for(ContentUsageRecord x:values){NBTTagCompound n=new NBTTagCompound();n.setString("key",x.contentKey);n.setInteger("usageCount",x.usageCount);n.setLong("lastUsedTick",x.lastUsedTick);add(out,n);}return out;}
    private static NBTTagList cleanup(List<CleanupTask> values){NBTTagList out=list();for(CleanupTask x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.taskId);n.setString("ownerId",x.ownerId);n.setInteger("retries",x.retries);n.setLong("availableAt",x.availableAt);add(out,n);}return out;}
    private static NBTTagList arcs(List<NarrativeArcState> values){NBTTagList out=list();for(NarrativeArcState x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.arcId);n.setString("theme",x.theme);n.setString("lifecycle",x.lifecycle);n.setInteger("progress",x.progress);add(out,n);}return out;}
    private static NBTTagList plans(List<DirectorPlanState> values){NBTTagList out=list();for(DirectorPlanState x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.planId);n.setString("ownerId",x.ownerId);n.setString("lifecycle",x.lifecycle);add(out,n);}return out;}
    private static NBTTagList events(List<DirectorEventState> values){NBTTagList out=list();for(DirectorEventState x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.eventId);n.setString("origin",x.origin);n.setString("lifecycle",x.lifecycle);add(out,n);}return out;}
    private static NBTTagList structures(List<StructureRecord> values){NBTTagList out=list();for(StructureRecord x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.structureId);n.setString("locationId",x.locationId);n.setString("lifecycle",x.lifecycle);add(out,n);}return out;}
    private static NBTTagList mutations(List<MutationRecord> values){NBTTagList out=list();for(MutationRecord x:values){NBTTagCompound n=new NBTTagCompound();n.setString("id",x.mutationId);n.setString("rollbackClass",x.rollbackClass);n.setLong("createdTick",x.createdTick);add(out,n);}return out;}
    private static NBTTagList strings(List<String> values){NBTTagList out=list();for(String value:values){NBTTagCompound n=new NBTTagCompound();n.setString("value",value);add(out,n);}return out;}
    private static NBTTagList requireList(NBTTagCompound root,String key){if(!root.hasKey(key,9))throw new PersistenceException("CORRUPT_LIST_"+key);NBTTagList value=root.getTagList(key,COMPOUND);if(value.tagCount()>MAX)throw new PersistenceException("LIMIT_LIST_"+key);return value;}
    private static String string(NBTTagCompound n,String key){if(!n.hasKey(key,8)||n.getString(key).length()>DirectorWorldState.MAX_STRING)throw new PersistenceException("CORRUPT_STRING_"+key);return n.getString(key);}
    private static List<String> readStrings(NBTTagCompound n,String key){NBTTagList list=requireList(n,key);List<String> out=new ArrayList<String>();for(int i=0;i<list.tagCount();i++)out.add(string(list.getCompoundTagAt(i),"value"));return out;}
    private static List<DirectorPlayerState> readPlayers(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<DirectorPlayerState> o=new ArrayList<DirectorPlayerState>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);if(!n.hasKey("uuidMost",4)||!n.hasKey("uuidLeast",4)||!n.hasKey("recentStress",3))throw new PersistenceException("CORRUPT_PLAYER");o.add(new DirectorPlayerState(new UUID(n.getLong("uuidMost"),n.getLong("uuidLeast")),string(n,"knowledgeState"),n.getInteger("recentStress")));}return o;}
    private static List<KnownLocation> readLocations(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<KnownLocation> o=new ArrayList<KnownLocation>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new KnownLocation(string(n,"id"),n.getInteger("dimension"),n.getInteger("centerX"),n.getInteger("centerY"),n.getInteger("centerZ"),n.getLong("firstSeenTick"),n.getLong("lastSeenTick"),n.getInteger("visitCount"),string(n,"knowledgeState"),n.getBoolean("directorModified"),readStrings(n,"tags")));}return o;}
    private static List<ReservedRegion> readReservations(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<ReservedRegion> o=new ArrayList<ReservedRegion>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new ReservedRegion(string(n,"id"),string(n,"ownerId"),n.getInteger("dimension"),n.getInteger("minX"),n.getInteger("minY"),n.getInteger("minZ"),n.getInteger("maxX"),n.getInteger("maxY"),n.getInteger("maxZ"),string(n,"reason"),n.getLong("createdTick"),n.getLong("expiryTick")));}return o;}
    private static List<MemoryRecord> readMemories(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<MemoryRecord> o=new ArrayList<MemoryRecord>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new MemoryRecord(string(n,"id"),string(n,"category"),string(n,"retention"),n.getLong("createdTick"),n.getLong("expiryTick")));}return o;}
    private static List<UniqueClaim> readClaims(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<UniqueClaim> o=new ArrayList<UniqueClaim>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new UniqueClaim(string(n,"id"),string(n,"resourceKey"),string(n,"ownerId"),n.getLong("createdTick"),string(n,"state")));}return o;}
    private static List<CooldownRecord> readCooldowns(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<CooldownRecord> o=new ArrayList<CooldownRecord>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new CooldownRecord(string(n,"id"),string(n,"scope"),n.getLong("startTick"),n.getLong("endTick")));}return o;}
    private static List<ProviderState> readProviders(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<ProviderState> o=new ArrayList<ProviderState>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new ProviderState(string(n,"key"),string(n,"status"),n.getInteger("failureCount")));}return o;}
    private static List<ContentUsageRecord> readUsage(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<ContentUsageRecord> o=new ArrayList<ContentUsageRecord>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new ContentUsageRecord(string(n,"key"),n.getInteger("usageCount"),n.getLong("lastUsedTick")));}return o;}
    private static List<CleanupTask> readCleanup(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<CleanupTask> o=new ArrayList<CleanupTask>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new CleanupTask(string(n,"id"),string(n,"ownerId"),n.getInteger("retries"),n.getLong("availableAt")));}return o;}
    private static List<NarrativeArcState> readArcs(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<NarrativeArcState> o=new ArrayList<NarrativeArcState>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new NarrativeArcState(string(n,"id"),string(n,"theme"),string(n,"lifecycle"),n.getInteger("progress")));}return o;}
    private static List<DirectorPlanState> readPlans(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<DirectorPlanState> o=new ArrayList<DirectorPlanState>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new DirectorPlanState(string(n,"id"),string(n,"ownerId"),string(n,"lifecycle")));}return o;}
    private static List<DirectorEventState> readEvents(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<DirectorEventState> o=new ArrayList<DirectorEventState>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new DirectorEventState(string(n,"id"),string(n,"origin"),string(n,"lifecycle")));}return o;}
    private static List<StructureRecord> readStructures(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<StructureRecord> o=new ArrayList<StructureRecord>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new StructureRecord(string(n,"id"),string(n,"locationId"),string(n,"lifecycle")));}return o;}
    private static List<MutationRecord> readMutations(NBTTagCompound r,String k){NBTTagList l=requireList(r,k);List<MutationRecord> o=new ArrayList<MutationRecord>();for(int i=0;i<l.tagCount();i++){NBTTagCompound n=l.getCompoundTagAt(i);o.add(new MutationRecord(string(n,"id"),string(n,"rollbackClass"),n.getLong("createdTick")));}return o;}
    private static List<KnownLocation> sortedLocations(List<KnownLocation> values){List<KnownLocation> out=new ArrayList<KnownLocation>(values);Collections.sort(out,new Comparator<KnownLocation>(){public int compare(KnownLocation a,KnownLocation b){return a.locationId.compareTo(b.locationId);}});return out;}

    public static final class PersistenceException extends RuntimeException { private static final long serialVersionUID = 1L; public PersistenceException(String message){super(message);} }
}
