package com.sobrenaturaldirector.observation.model;

public final class InventorySlotSnapshot implements Comparable<InventorySlotSnapshot> {
    public static final String UNKNOWN_REGISTRY = "UNKNOWN";
    private final int slotIndex;
    private final String registryName;
    private final int stackCount;
    private final int itemDamage;
    private final int maxDamage;
    private final boolean damageable;
    private final boolean hasNbt;
    private final String slotKind;

    public InventorySlotSnapshot(int slotIndex, String registryName, int stackCount, int itemDamage, int maxDamage, boolean damageable, boolean hasNbt, String slotKind) {
        if (slotIndex < 0 || stackCount < 0 || itemDamage < 0 || maxDamage < 0) throw new IllegalArgumentException("invalid inventory slot");
        this.slotIndex = slotIndex;
        this.registryName = text(registryName == null || registryName.length() == 0 ? UNKNOWN_REGISTRY : registryName);
        this.stackCount = stackCount; this.itemDamage = itemDamage; this.maxDamage = maxDamage; this.damageable = damageable; this.hasNbt = hasNbt;
        this.slotKind = text(slotKind == null ? "MAIN" : slotKind);
    }
    private static String text(String value) { if (value.length() > 128) throw new IllegalArgumentException("inventory text too long"); return value; }
    public int getSlotIndex() { return slotIndex; }
    public String getRegistryName() { return registryName; }
    public int getStackCount() { return stackCount; }
    public int getItemDamage() { return itemDamage; }
    public int getMaxDamage() { return maxDamage; }
    public boolean isDamageable() { return damageable; }
    public boolean hasNbt() { return hasNbt; }
    public String getSlotKind() { return slotKind; }
    @Override public int compareTo(InventorySlotSnapshot other) { return slotIndex < other.slotIndex ? -1 : (slotIndex == other.slotIndex ? 0 : 1); }
    @Override public boolean equals(Object o) { if (!(o instanceof InventorySlotSnapshot)) return false; InventorySlotSnapshot x=(InventorySlotSnapshot)o; return slotIndex==x.slotIndex&&registryName.equals(x.registryName)&&stackCount==x.stackCount&&itemDamage==x.itemDamage&&maxDamage==x.maxDamage&&damageable==x.damageable&&hasNbt==x.hasNbt&&slotKind.equals(x.slotKind); }
    @Override public int hashCode() { return java.util.Arrays.hashCode(new Object[]{slotIndex,registryName,stackCount,itemDamage,maxDamage,damageable,hasNbt,slotKind}); }
}
