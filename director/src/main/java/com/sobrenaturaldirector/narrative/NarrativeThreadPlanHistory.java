package com.sobrenaturaldirector.narrative;

import net.minecraft.nbt.NBTTagCompound;

public final class NarrativeThreadPlanHistory {
    public static final int SCHEMA_VERSION = 1;
    private final String planId, intent, blueprintId, reason;
    private final long tick;
    public NarrativeThreadPlanHistory(String planId,String intent,String blueprintId,long tick,String reason){this.planId=required(planId);this.intent=required(intent);this.blueprintId=required(blueprintId);this.tick=tick;this.reason=reason==null?"":reason;}
    private static String required(String value){if(value==null||value.length()==0||value.length()>128)throw new IllegalArgumentException("invalid thread history");return value;}
    public String getPlanId(){return planId;} public String getIntent(){return intent;} public String getBlueprintId(){return blueprintId;} public long getTick(){return tick;} public String getReason(){return reason;}
    public NBTTagCompound toNbt(){NBTTagCompound n=new NBTTagCompound();n.setInteger("schemaVersion",SCHEMA_VERSION);n.setString("planId",planId);n.setString("intent",intent);n.setString("blueprintId",blueprintId);n.setLong("tick",tick);if(reason.length()>0)n.setString("reason",reason);return n;}
    public static NarrativeThreadPlanHistory fromNbt(NBTTagCompound n){if(n==null||n.getInteger("schemaVersion")!=SCHEMA_VERSION)throw new IllegalArgumentException("invalid thread history schema");return new NarrativeThreadPlanHistory(n.getString("planId"),n.getString("intent"),n.getString("blueprintId"),n.getLong("tick"),n.getString("reason"));}
}
