package com.sobrenaturaldirector.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Immutable, world-scoped data only. No lifecycle execution belongs here. */
public final class DirectorWorldState {
    public static final int MAX_RECORDS = 1024;
    public static final int MAX_TAGS = 64;
    public static final int MAX_STRING = 128;

    private final long directorSeed;
    private final boolean paused;
    private final List<DirectorPlayerState> players;
    private final List<KnownLocation> knownLocations;
    private final List<ReservedRegion> reservations;
    private final List<MemoryRecord> memories;
    private final List<UniqueClaim> uniqueClaims;
    private final List<CooldownRecord> cooldowns;
    private final List<ProviderState> providers;
    private final List<ContentUsageRecord> contentUsage;
    private final List<CleanupTask> cleanupTasks;
    private final List<NarrativeArcState> arcs;
    private final List<DirectorPlanState> plans;
    private final List<DirectorEventState> events;
    private final List<StructureRecord> structures;
    private final List<MutationRecord> mutations;

    public DirectorWorldState(long directorSeed, boolean paused,
            List<DirectorPlayerState> players, List<KnownLocation> knownLocations,
            List<ReservedRegion> reservations, List<MemoryRecord> memories,
            List<UniqueClaim> uniqueClaims, List<CooldownRecord> cooldowns,
            List<ProviderState> providers, List<ContentUsageRecord> contentUsage,
            List<CleanupTask> cleanupTasks, List<NarrativeArcState> arcs,
            List<DirectorPlanState> plans, List<DirectorEventState> events,
            List<StructureRecord> structures, List<MutationRecord> mutations) {
        this.directorSeed = directorSeed;
        this.paused = paused;
        this.players = copy(players, "players");
        this.knownLocations = copy(knownLocations, "knownLocations");
        this.reservations = copy(reservations, "reservations");
        this.memories = copy(memories, "memories");
        this.uniqueClaims = copy(uniqueClaims, "uniqueClaims");
        this.cooldowns = copy(cooldowns, "cooldowns");
        this.providers = copy(providers, "providers");
        this.contentUsage = copy(contentUsage, "contentUsage");
        this.cleanupTasks = copy(cleanupTasks, "cleanupTasks");
        this.arcs = copy(arcs, "arcs");
        this.plans = copy(plans, "plans");
        this.events = copy(events, "events");
        this.structures = copy(structures, "structures");
        this.mutations = copy(mutations, "mutations");
        validateUniqueIds();
    }

    public static DirectorWorldState empty(long seed) {
        return new DirectorWorldState(seed, false, Collections.<DirectorPlayerState>emptyList(),
                Collections.<KnownLocation>emptyList(), Collections.<ReservedRegion>emptyList(),
                Collections.<MemoryRecord>emptyList(), Collections.<UniqueClaim>emptyList(),
                Collections.<CooldownRecord>emptyList(), Collections.<ProviderState>emptyList(),
                Collections.<ContentUsageRecord>emptyList(), Collections.<CleanupTask>emptyList(),
                Collections.<NarrativeArcState>emptyList(), Collections.<DirectorPlanState>emptyList(),
                Collections.<DirectorEventState>emptyList(), Collections.<StructureRecord>emptyList(),
                Collections.<MutationRecord>emptyList());
    }

    private static <T> List<T> copy(List<T> values, String field) {
        if (values == null || values.size() > MAX_RECORDS) throw new DomainValidationException(field + " exceeds record limit");
        return Collections.unmodifiableList(new ArrayList<T>(values));
    }

    private void validateUniqueIds() {
        Set<String> ids = new LinkedHashSet<String>();
        for (DirectorPlayerState value : players) add(ids, value.playerId.toString(), "player");
        for (KnownLocation value : knownLocations) add(ids, value.locationId, "location");
        for (ReservedRegion value : reservations) add(ids, value.reservationId, "reservation");
        for (MemoryRecord value : memories) add(ids, value.memoryId, "memory");
        for (UniqueClaim value : uniqueClaims) add(ids, value.claimId, "claim");
        for (CooldownRecord value : cooldowns) add(ids, value.cooldownId, "cooldown");
        for (CleanupTask value : cleanupTasks) add(ids, value.taskId, "cleanup");
        for (NarrativeArcState value : arcs) add(ids, value.arcId, "arc");
        for (DirectorPlanState value : plans) add(ids, value.planId, "plan");
        for (DirectorEventState value : events) add(ids, value.eventId, "event");
        for (StructureRecord value : structures) add(ids, value.structureId, "structure");
        for (MutationRecord value : mutations) add(ids, value.mutationId, "mutation");
    }

    private static void add(Set<String> ids, String value, String kind) {
        if (!ids.add(value)) throw new DomainValidationException("duplicate " + kind + " id: " + value);
    }

    public long getDirectorSeed() { return directorSeed; }
    public boolean isPaused() { return paused; }
    public List<DirectorPlayerState> getPlayers() { return players; }
    public List<KnownLocation> getKnownLocations() { return knownLocations; }
    public List<ReservedRegion> getReservations() { return reservations; }
    public List<MemoryRecord> getMemories() { return memories; }
    public List<UniqueClaim> getUniqueClaims() { return uniqueClaims; }
    public List<CooldownRecord> getCooldowns() { return cooldowns; }
    public List<ProviderState> getProviders() { return providers; }
    public List<ContentUsageRecord> getContentUsage() { return contentUsage; }
    public List<CleanupTask> getCleanupTasks() { return cleanupTasks; }
    public List<NarrativeArcState> getArcs() { return arcs; }
    public List<DirectorPlanState> getPlans() { return plans; }
    public List<DirectorEventState> getEvents() { return events; }
    public List<StructureRecord> getStructures() { return structures; }
    public List<MutationRecord> getMutations() { return mutations; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof DirectorWorldState)) return false;
        DirectorWorldState value = (DirectorWorldState) other;
        return directorSeed == value.directorSeed && paused == value.paused
                && players.equals(value.players) && knownLocations.equals(value.knownLocations)
                && reservations.equals(value.reservations) && memories.equals(value.memories)
                && uniqueClaims.equals(value.uniqueClaims) && cooldowns.equals(value.cooldowns)
                && providers.equals(value.providers) && contentUsage.equals(value.contentUsage)
                && cleanupTasks.equals(value.cleanupTasks) && arcs.equals(value.arcs)
                && plans.equals(value.plans) && events.equals(value.events)
                && structures.equals(value.structures) && mutations.equals(value.mutations);
    }
    @Override public int hashCode() { return java.util.Arrays.hashCode(new Object[] { directorSeed, paused, players, knownLocations, reservations, memories, uniqueClaims, cooldowns, providers, contentUsage, cleanupTasks, arcs, plans, events, structures, mutations }); }

    public static final class DirectorPlayerState {
        public final UUID playerId;
        public final String knowledgeState;
        public final int recentStress;
        public DirectorPlayerState(UUID playerId, String knowledgeState, int recentStress) {
            if (playerId == null) throw new DomainValidationException("playerId is required");
            this.playerId = playerId; this.knowledgeState = text(knowledgeState, "knowledgeState");
            if (recentStress < 0) throw new DomainValidationException("recentStress must be non-negative"); this.recentStress = recentStress;
        }
        @Override public boolean equals(Object o) { if (!(o instanceof DirectorPlayerState)) return false; DirectorPlayerState x=(DirectorPlayerState)o; return playerId.equals(x.playerId)&&knowledgeState.equals(x.knowledgeState)&&recentStress==x.recentStress; }
        @Override public int hashCode() { return java.util.Arrays.hashCode(new Object[]{playerId,knowledgeState,recentStress}); }
    }
    public static final class KnownLocation {
        public final String locationId, knowledgeState; public final int dimension, centerX, centerY, centerZ; public final long firstSeenTick, lastSeenTick; public final int visitCount; public final boolean directorModified; public final List<String> tags;
        public KnownLocation(String id,int dimension,int x,int y,int z,long first,long last,int visits,String knowledge,boolean modified,List<String> tags) { locationId=id(id,"locationId"); this.dimension=dimension; centerX=x;centerY=y;centerZ=z; if(first<0||last<first||visits<0)throw new DomainValidationException("invalid location counters");firstSeenTick=first;lastSeenTick=last;visitCount=visits;knowledgeState=text(knowledge,"knowledgeState");directorModified=modified;this.tags=tags(tags); }
        @Override public boolean equals(Object o){if(!(o instanceof KnownLocation))return false;KnownLocation x=(KnownLocation)o;return locationId.equals(x.locationId)&&dimension==x.dimension&&centerX==x.centerX&&centerY==x.centerY&&centerZ==x.centerZ&&firstSeenTick==x.firstSeenTick&&lastSeenTick==x.lastSeenTick&&visitCount==x.visitCount&&knowledgeState.equals(x.knowledgeState)&&directorModified==x.directorModified&&tags.equals(x.tags);}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{locationId,dimension,centerX,centerY,centerZ,firstSeenTick,lastSeenTick,visitCount,knowledgeState,directorModified,tags});}
    }
    public static final class ReservedRegion {
        public final String reservationId, ownerId, reason; public final int dimension,minX,minY,minZ,maxX,maxY,maxZ; public final long createdTick,expiryTick;
        public ReservedRegion(String id,String owner,int dim,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,String reason,long created,long expiry){reservationId=id(id,"reservationId");ownerId=id(owner,"ownerId");this.dimension=dim;this.minX=minX;this.minY=minY;this.minZ=minZ;this.maxX=maxX;this.maxY=maxY;this.maxZ=maxZ;if(minX>maxX||minY>maxY||minZ>maxZ||created<0||expiry<created)throw new DomainValidationException("invalid reservation bounds");this.reason=text(reason,"reason");createdTick=created;expiryTick=expiry;}
        @Override public boolean equals(Object o){if(!(o instanceof ReservedRegion))return false;ReservedRegion x=(ReservedRegion)o;return reservationId.equals(x.reservationId)&&ownerId.equals(x.ownerId)&&dimension==x.dimension&&minX==x.minX&&minY==x.minY&&minZ==x.minZ&&maxX==x.maxX&&maxY==x.maxY&&maxZ==x.maxZ&&reason.equals(x.reason)&&createdTick==x.createdTick&&expiryTick==x.expiryTick;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{reservationId,ownerId,dimension,minX,minY,minZ,maxX,maxY,maxZ,reason,createdTick,expiryTick});}
    }
    public static final class MemoryRecord { public final String memoryId,category,retention;public final long createdTick,expiryTick;public MemoryRecord(String id,String cat,String keep,long created,long expiry){memoryId=id(id,"memoryId");category=text(cat,"category");retention=text(keep,"retention");if(created<0||expiry< -1)throw new DomainValidationException("invalid memory ticks");createdTick=created;expiryTick=expiry;}@Override public boolean equals(Object o){if(!(o instanceof MemoryRecord))return false;MemoryRecord x=(MemoryRecord)o;return memoryId.equals(x.memoryId)&&category.equals(x.category)&&retention.equals(x.retention)&&createdTick==x.createdTick&&expiryTick==x.expiryTick;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{memoryId,category,retention,createdTick,expiryTick});} }
    public static final class UniqueClaim { public final String claimId,resourceKey,ownerId,state;public final long createdTick;public UniqueClaim(String id,String key,String owner,long created,String state){claimId=id(id,"claimId");resourceKey=id(key,"resourceKey");ownerId=id(owner,"ownerId");this.state=text(state,"state");if(created<0)throw new DomainValidationException("createdTick must be non-negative");createdTick=created;}@Override public boolean equals(Object o){if(!(o instanceof UniqueClaim))return false;UniqueClaim x=(UniqueClaim)o;return claimId.equals(x.claimId)&&resourceKey.equals(x.resourceKey)&&ownerId.equals(x.ownerId)&&state.equals(x.state)&&createdTick==x.createdTick;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{claimId,resourceKey,ownerId,state,createdTick});} }
    public static final class CooldownRecord { public final String cooldownId,scope;public final long startTick,endTick;public CooldownRecord(String id,String scope,long start,long end){cooldownId=id(id,"cooldownId");this.scope=text(scope,"scope");if(start<0||end<start)throw new DomainValidationException("invalid cooldown ticks");startTick=start;endTick=end;}@Override public boolean equals(Object o){if(!(o instanceof CooldownRecord))return false;CooldownRecord x=(CooldownRecord)o;return cooldownId.equals(x.cooldownId)&&scope.equals(x.scope)&&startTick==x.startTick&&endTick==x.endTick;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{cooldownId,scope,startTick,endTick});} }
    public static final class ProviderState { public final String providerKey,status;public final int failureCount;public ProviderState(String key,String status,int failures){providerKey=id(key,"providerKey");this.status=text(status,"status");if(failures<0)throw new DomainValidationException("failureCount must be non-negative");failureCount=failures;}@Override public boolean equals(Object o){if(!(o instanceof ProviderState))return false;ProviderState x=(ProviderState)o;return providerKey.equals(x.providerKey)&&status.equals(x.status)&&failureCount==x.failureCount;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{providerKey,status,failureCount});} }
    public static final class ContentUsageRecord { public final String contentKey;public final int usageCount;public final long lastUsedTick;public ContentUsageRecord(String key,int count,long last){contentKey=id(key,"contentKey");if(count<0||last<0)throw new DomainValidationException("invalid content usage");usageCount=count;lastUsedTick=last;}@Override public boolean equals(Object o){if(!(o instanceof ContentUsageRecord))return false;ContentUsageRecord x=(ContentUsageRecord)o;return contentKey.equals(x.contentKey)&&usageCount==x.usageCount&&lastUsedTick==x.lastUsedTick;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{contentKey,usageCount,lastUsedTick});} }
    public static final class CleanupTask { public final String taskId,ownerId;public final int retries;public final long availableAt;public CleanupTask(String id,String owner,int retries,long available){taskId=id(id,"taskId");ownerId=id(owner,"ownerId");if(retries<0||available<0)throw new DomainValidationException("invalid cleanup task");this.retries=retries;availableAt=available;}@Override public boolean equals(Object o){if(!(o instanceof CleanupTask))return false;CleanupTask x=(CleanupTask)o;return taskId.equals(x.taskId)&&ownerId.equals(x.ownerId)&&retries==x.retries&&availableAt==x.availableAt;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{taskId,ownerId,retries,availableAt});} }
    public static final class NarrativeArcState { public final String arcId,theme,lifecycle;public final int progress;public NarrativeArcState(String id,String theme,String life,int progress){arcId=id(id,"arcId");this.theme=text(theme,"theme");lifecycle=choice(life,"lifecycle",new String[]{"DORMANT","ACTIVE","SUSPENDED","RESOLVED","ABANDONED"});if(progress<0)throw new DomainValidationException("progress must be non-negative");this.progress=progress;}@Override public boolean equals(Object o){if(!(o instanceof NarrativeArcState))return false;NarrativeArcState x=(NarrativeArcState)o;return arcId.equals(x.arcId)&&theme.equals(x.theme)&&lifecycle.equals(x.lifecycle)&&progress==x.progress;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{arcId,theme,lifecycle,progress});} }
    public static final class DirectorPlanState { public final String planId,ownerId,lifecycle;public DirectorPlanState(String id,String owner,String life){planId=id(id,"planId");ownerId=id(owner,"ownerId");lifecycle=choice(life,"lifecycle",new String[]{"IDEA","TENTATIVE","RESERVED","PREPARED","COMMITTED","ACTIVE","COMPLETE","ABORTED","SUSPENDED"});}@Override public boolean equals(Object o){if(!(o instanceof DirectorPlanState))return false;DirectorPlanState x=(DirectorPlanState)o;return planId.equals(x.planId)&&ownerId.equals(x.ownerId)&&lifecycle.equals(x.lifecycle);}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{planId,ownerId,lifecycle});} }
    public static final class DirectorEventState { public final String eventId,origin,lifecycle;public DirectorEventState(String id,String origin,String life){eventId=id(id,"eventId");this.origin=text(origin,"origin");lifecycle=choice(life,"lifecycle",new String[]{"PRECONDITION","PREPARE","ARM","REVEAL","ACTIVE","RESOLVE","CLEANUP","COOLDOWN"});}@Override public boolean equals(Object o){if(!(o instanceof DirectorEventState))return false;DirectorEventState x=(DirectorEventState)o;return eventId.equals(x.eventId)&&origin.equals(x.origin)&&lifecycle.equals(x.lifecycle);}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{eventId,origin,lifecycle});} }
    public static final class StructureRecord { public final String structureId,locationId,lifecycle;public StructureRecord(String id,String location,String life){structureId=id(id,"structureId");locationId=id(location,"locationId");lifecycle=choice(life,"lifecycle",new String[]{"PLANNED","RESERVED","BUILDING","COMMITTED","FAILED","ROLLBACK_PENDING","CLEANED"});}@Override public boolean equals(Object o){if(!(o instanceof StructureRecord))return false;StructureRecord x=(StructureRecord)o;return structureId.equals(x.structureId)&&locationId.equals(x.locationId)&&lifecycle.equals(x.lifecycle);}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{structureId,locationId,lifecycle});} }
    public static final class MutationRecord { public final String mutationId,rollbackClass;public final long createdTick;public MutationRecord(String id,String rollback,long created){mutationId=id(id,"mutationId");rollbackClass=text(rollback,"rollbackClass");if(created<0)throw new DomainValidationException("createdTick must be non-negative");createdTick=created;}@Override public boolean equals(Object o){if(!(o instanceof MutationRecord))return false;MutationRecord x=(MutationRecord)o;return mutationId.equals(x.mutationId)&&rollbackClass.equals(x.rollbackClass)&&createdTick==x.createdTick;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{mutationId,rollbackClass,createdTick});} }

    private static String id(String value,String field){return StableId.require(value,field);}
    private static String text(String value,String field){if(value==null||value.length()==0||value.length()>MAX_STRING)throw new DomainValidationException(field+" exceeds text limit");return value;}
    private static String choice(String value,String field,String[] allowed){text(value,field);for(String candidate:allowed)if(candidate.equals(value))return value;throw new DomainValidationException("invalid "+field+": "+value);}
    private static List<String> tags(List<String> values){if(values==null||values.size()>MAX_TAGS)throw new DomainValidationException("tags exceed limit");List<String> copy=new ArrayList<String>();for(String value:values)copy.add(text(value,"tag"));return Collections.unmodifiableList(copy);}
}
