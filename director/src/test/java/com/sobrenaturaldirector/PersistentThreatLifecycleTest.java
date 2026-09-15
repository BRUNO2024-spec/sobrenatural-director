package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.util.UUID;
import org.junit.Test;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.threat.PersistentNarrativeThreat;
import com.sobrenaturaldirector.threat.ThreatLifecycleReconciler;
import com.sobrenaturaldirector.threat.ThreatLifecycleState;
import com.sobrenaturaldirector.threat.ThreatPhysicalBinding;
import com.sobrenaturaldirector.threat.ThreatPhysicalObservation;
import net.minecraft.nbt.NBTTagCompound;

public final class PersistentThreatLifecycleTest {
    private static final ProviderId PROVIDER = new ProviderId("slenderman");
    private static final String ID = "narrative-threat:request-1";

    private static PersistentNarrativeThreat threat(ThreatLifecycleState state) {
        return new PersistentNarrativeThreat(ID, "request-1", PROVIDER, "slenderman", "DIRECTOR_THREAT", state, 0, 10, 64, 10, 1L, 1L, 0L);
    }

    @Test public void narrativeIdentityIsIndependentFromPhysicalUuid() {
        UUID physical = UUID.randomUUID();
        assertNotEquals(ID, physical.toString());
        ThreatPhysicalBinding binding = new ThreatPhysicalBinding(ID, PROVIDER, physical, "MATERIALIZED", 0, 10, 64, 10, 1, 2L);
        assertEquals(ID, binding.getThreatId());
        assertEquals(physical, binding.getEntityUuid());
    }

    @Test public void narrativeAndBindingRoundTripThroughWorldSavedData() {
        DirectorWorldSavedData source = new DirectorWorldSavedData();
        UUID physical = UUID.randomUUID();
        source.recordNarrativeThreat(threat(ThreatLifecycleState.MATERIALIZED));
        source.recordThreatBinding(new ThreatPhysicalBinding(ID, PROVIDER, physical, "MATERIALIZED", 0, 10, 64, 10, 1, 2L));
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("schemaVersion", 1);
        nbt.setLong("directorSeed", 0L);
        NBTTagCompound state = com.sobrenaturaldirector.persistence.DirectorStateNbtCodec.encode(com.sobrenaturaldirector.domain.DirectorWorldState.empty(0L));
        for (Object keyObject : state.func_150296_c()) { String key = (String) keyObject; nbt.setTag(key, state.getTag(key).copy()); }
        for (com.sobrenaturaldirector.threat.PersistentNarrativeThreat value : source.getNarrativeThreats()) { net.minecraft.nbt.NBTTagList list = nbt.hasKey("narrativeThreats", 9) ? nbt.getTagList("narrativeThreats", 10) : new net.minecraft.nbt.NBTTagList(); list.appendTag(value.toNbt()); nbt.setTag("narrativeThreats", list); }
        net.minecraft.nbt.NBTTagList bindings = new net.minecraft.nbt.NBTTagList(); bindings.appendTag(source.getThreatBindings().get(0).toNbt()); nbt.setTag("threatBindings", bindings);
        DirectorWorldSavedData restored = new DirectorWorldSavedData(); restored.readFromNBT(nbt);
        assertEquals(ID, restored.getNarrativeThreat(ID).getId());
        assertEquals(physical, restored.getThreatBinding(ID).getEntityUuid());
    }

    @Test public void confirmedOutcomeMarkerRoundTripsAcrossFreshState() {
        DirectorWorldSavedData source = new DirectorWorldSavedData();
        source.recordNarrativeThreat(threat(ThreatLifecycleState.MATERIALIZED).withOutcomeConfirmed(true, 11L));
        NBTTagCompound nbt = new NBTTagCompound(); nbt.setInteger("schemaVersion", 1); nbt.setLong("directorSeed", 0L);
        NBTTagCompound encoded = com.sobrenaturaldirector.persistence.DirectorStateNbtCodec.encode(com.sobrenaturaldirector.domain.DirectorWorldState.empty(0L));
        for (Object keyObject : encoded.func_150296_c()) { String key = (String) keyObject; nbt.setTag(key, encoded.getTag(key).copy()); }
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList(); list.appendTag(source.getNarrativeThreats().get(0).toNbt()); nbt.setTag("narrativeThreats", list);
        DirectorWorldSavedData restored = new DirectorWorldSavedData(); restored.readFromNBT(nbt);
        assertEquals(true, restored.getNarrativeThreat(ID).isOutcomeConfirmed());
    }

    @Test public void confirmedOutcomeMarkerSurvivesDeathAndResolution() {
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        saved.recordNarrativeThreat(threat(ThreatLifecycleState.MATERIALIZED).withOutcomeConfirmed(true, 2L));
        saved.updateNarrativeThreat(saved.getNarrativeThreat(ID).withState(ThreatLifecycleState.FAILED, 3L));
        assertEquals(true, saved.getNarrativeThreat(ID).isOutcomeConfirmed());
        saved.updateNarrativeThreat(saved.getNarrativeThreat(ID).withState(ThreatLifecycleState.RESOLVED, 4L));
        assertEquals(true, saved.getNarrativeThreat(ID).isOutcomeConfirmed());
    }

    @Test public void legacyNarrativeRecordDefaultsToUnconfirmed() {
        PersistentNarrativeThreat legacy = threat(ThreatLifecycleState.MATERIALIZED);
        NBTTagCompound nbt = legacy.toNbt(); nbt.removeTag("outcomeConfirmed");
        assertEquals(false, PersistentNarrativeThreat.fromNbt(nbt).isOutcomeConfirmed());
    }

    @Test public void providerAbsenceSuspendsWithoutClearingConfirmedOutcome() {
        DirectorWorldSavedData saved = new DirectorWorldSavedData();
        saved.recordNarrativeThreat(threat(ThreatLifecycleState.MATERIALIZED).withOutcomeConfirmed(true, 2L));
        assertEquals(ThreatLifecycleState.SUSPENDED, new ThreatLifecycleReconciler().reconcile(saved, ID, ThreatPhysicalObservation.PROVIDER_UNAVAILABLE, 3L));
        assertEquals(true, saved.getNarrativeThreat(ID).isOutcomeConfirmed());
    }

    @Test public void unknownOptionalOutcomeFieldIsIgnored() {
        NBTTagCompound nbt = threat(ThreatLifecycleState.MATERIALIZED).toNbt();
        nbt.setString("futureOutcomeMetadata", "ignored");
        assertEquals(false, PersistentNarrativeThreat.fromNbt(nbt).isOutcomeConfirmed());
    }

    @Test public void reconciliationIsIdempotentAndDoesNotRematerialize() {
        DirectorWorldSavedData saved = new DirectorWorldSavedData(); saved.recordNarrativeThreat(threat(ThreatLifecycleState.MATERIALIZED));
        UUID physical = UUID.randomUUID(); saved.recordThreatBinding(new ThreatPhysicalBinding(ID, PROVIDER, physical, "MATERIALIZED", 0, 10, 64, 10, 1, 2L));
        ThreatLifecycleReconciler reconciler = new ThreatLifecycleReconciler();
        assertEquals(ThreatLifecycleState.MATERIALIZED, reconciler.reconcile(saved, ID, ThreatPhysicalObservation.ENTITY_PRESENT, 3L));
        assertEquals(ThreatLifecycleState.MATERIALIZED, reconciler.reconcile(saved, ID, ThreatPhysicalObservation.ENTITY_PRESENT, 4L));
        assertEquals(physical, saved.getThreatBinding(ID).getEntityUuid());
    }

    @Test public void unloadIsNotResolutionAndDeathIsNotOrdinaryAbsence() {
        DirectorWorldSavedData saved = new DirectorWorldSavedData(); saved.recordNarrativeThreat(threat(ThreatLifecycleState.MATERIALIZED));
        UUID physical = UUID.randomUUID(); saved.recordThreatBinding(new ThreatPhysicalBinding(ID, PROVIDER, physical, "MATERIALIZED", 0, 10, 64, 10, 1, 2L));
        ThreatLifecycleReconciler reconciler = new ThreatLifecycleReconciler();
        assertEquals(ThreatLifecycleState.TEMPORARILY_UNBOUND, reconciler.reconcile(saved, ID, ThreatPhysicalObservation.CHUNK_UNAVAILABLE, 3L));
        assertEquals(ThreatLifecycleState.FAILED, reconciler.reconcile(saved, ID, ThreatPhysicalObservation.ENTITY_DEAD, 4L));
    }
}
