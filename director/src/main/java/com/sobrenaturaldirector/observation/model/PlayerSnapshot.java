package com.sobrenaturaldirector.observation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PlayerSnapshot implements Comparable<PlayerSnapshot> {
    private final UUID playerId;
    private final String displayName;
    private final int dimension;
    private final double x,y,z;
    private final float health,maxHealth;
    private final int foodLevel, armorValue, experienceLevel;
    private final boolean sleeping, alive;
    private final EnvironmentSnapshot environment;
    private final List<InventorySlotSnapshot> inventory;

    public PlayerSnapshot(UUID playerId, String displayName, int dimension, double x, double y, double z, float health, float maxHealth, int foodLevel, int armorValue, int experienceLevel, boolean sleeping, boolean alive, EnvironmentSnapshot environment, List<InventorySlotSnapshot> inventory) {
        if (playerId == null || displayName == null || displayName.length() > 128 || !finite(x) || !finite(y) || !finite(z) || Float.isNaN(health) || Float.isInfinite(health) || Float.isNaN(maxHealth) || Float.isInfinite(maxHealth) || health < 0 || maxHealth < 0 || foodLevel < 0 || armorValue < 0 || experienceLevel < 0 || environment == null || inventory == null || inventory.size() > 128) throw new IllegalArgumentException("invalid player snapshot");
        this.playerId=playerId;this.displayName=displayName;this.dimension=dimension;this.x=x;this.y=y;this.z=z;this.health=health;this.maxHealth=maxHealth;this.foodLevel=foodLevel;this.armorValue=armorValue;this.experienceLevel=experienceLevel;this.sleeping=sleeping;this.alive=alive;this.environment=environment;
        List<InventorySlotSnapshot> copy=new ArrayList<InventorySlotSnapshot>(inventory);Collections.sort(copy);this.inventory=Collections.unmodifiableList(copy);
    }
    private static boolean finite(double value){return !Double.isNaN(value)&&!Double.isInfinite(value);}
    public UUID getPlayerId(){return playerId;} public String getDisplayName(){return displayName;} public int getDimension(){return dimension;} public double getX(){return x;} public double getY(){return y;} public double getZ(){return z;} public float getHealth(){return health;} public float getMaxHealth(){return maxHealth;} public int getFoodLevel(){return foodLevel;} public int getArmorValue(){return armorValue;} public int getExperienceLevel(){return experienceLevel;} public boolean isSleeping(){return sleeping;} public boolean isAlive(){return alive;} public EnvironmentSnapshot getEnvironment(){return environment;} public List<InventorySlotSnapshot> getInventory(){return inventory;}
    @Override public int compareTo(PlayerSnapshot other){return playerId.compareTo(other.playerId);}
    @Override public boolean equals(Object o){if(!(o instanceof PlayerSnapshot))return false;PlayerSnapshot p=(PlayerSnapshot)o;return playerId.equals(p.playerId)&&displayName.equals(p.displayName)&&dimension==p.dimension&&Double.compare(x,p.x)==0&&Double.compare(y,p.y)==0&&Double.compare(z,p.z)==0&&Float.compare(health,p.health)==0&&Float.compare(maxHealth,p.maxHealth)==0&&foodLevel==p.foodLevel&&armorValue==p.armorValue&&experienceLevel==p.experienceLevel&&sleeping==p.sleeping&&alive==p.alive&&environment.equals(p.environment)&&inventory.equals(p.inventory);}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{playerId,displayName,dimension,x,y,z,health,maxHealth,foodLevel,armorValue,experienceLevel,sleeping,alive,environment,inventory});}
}
