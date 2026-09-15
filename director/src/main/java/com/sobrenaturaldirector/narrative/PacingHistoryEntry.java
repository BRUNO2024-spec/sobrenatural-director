package com.sobrenaturaldirector.narrative;

import net.minecraft.nbt.NBTTagCompound;
import com.sobrenaturaldirector.decision.model.Intent;

/** Minimal bounded semantic record. It records planned/observed pressure, not fabricated outcomes. */
public final class PacingHistoryEntry {
    public static final int SCHEMA_VERSION = 1;
    private final String scope, threadId, reason, fingerprint;
    private final long tick;
    private final Intent intent;
    private final NarrativeIntensity intensity;
    private final String result;
    public PacingHistoryEntry(String scope, long tick, String threadId, Intent intent,
            NarrativeIntensity intensity, String result, String reason, String fingerprint) {
        if (scope == null || scope.length() == 0 || tick < 0 || intent == null || intensity == null)
            throw new IllegalArgumentException("invalid pacing history");
        this.scope = text(scope); this.tick = tick; this.threadId = text(threadId);
        this.intent = intent; this.intensity = intensity; this.result = text(result);
        this.reason = text(reason); this.fingerprint = text(fingerprint);
    }
    private static String text(String value) { if (value == null) return ""; if (value.length() > 128) throw new IllegalArgumentException("pacing text too long"); return value; }
    public String getScope(){return scope;} public long getTick(){return tick;} public String getThreadId(){return threadId;}
    public Intent getIntent(){return intent;} public NarrativeIntensity getIntensity(){return intensity;}
    public String getResult(){return result;} public String getReason(){return reason;} public String getFingerprint(){return fingerprint;}
    public NBTTagCompound toNbt(){NBTTagCompound n=new NBTTagCompound();n.setInteger("schemaVersion",SCHEMA_VERSION);n.setString("scope",scope);n.setLong("tick",tick);if(threadId.length()>0)n.setString("threadId",threadId);n.setString("intent",intent.name());n.setString("intensity",intensity.name());n.setString("result",result);n.setString("reason",reason);n.setString("fingerprint",fingerprint);return n;}
    public static PacingHistoryEntry fromNbt(NBTTagCompound n){if(n==null||n.getInteger("schemaVersion")!=SCHEMA_VERSION)throw new IllegalArgumentException("invalid pacing history schema");return new PacingHistoryEntry(n.getString("scope"),n.getLong("tick"),n.getString("threadId"),Intent.valueOf(n.getString("intent")),NarrativeIntensity.valueOf(n.getString("intensity")),n.getString("result"),n.getString("reason"),n.getString("fingerprint"));}
}
