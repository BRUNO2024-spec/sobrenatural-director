package com.sobrenaturaldirector.observation.minecraft;

import com.mojang.authlib.GameProfile;
import com.sobrenaturaldirector.observation.model.EnvironmentSnapshot;
import com.sobrenaturaldirector.observation.model.InventorySlotSnapshot;
import com.sobrenaturaldirector.observation.model.ObservationDiagnostics;
import com.sobrenaturaldirector.observation.model.ObservationFrame;
import com.sobrenaturaldirector.observation.model.ObservationStatus;
import com.sobrenaturaldirector.observation.model.PlayerSnapshot;
import com.sobrenaturaldirector.observation.model.WorldSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FoodStats;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;

/** Minecraft boundary. It converts values and retains no live game object. */
public final class MinecraftObservationCapture {
    public static final int MAX_INVENTORY_SLOTS = 128;

    public ObservationFrame capture(MinecraftServer server, long serverTick, long sequence) {
        List<WorldSnapshot> worlds = new ArrayList<WorldSnapshot>();
        List<PlayerSnapshot> players = new ArrayList<PlayerSnapshot>();
        List<String> warnings = new ArrayList<String>();
        int attempted = 0;
        int succeeded = 0;
        if (server == null || server.worldServers == null) {
            warnings.add("server_not_ready");
        } else {
            for (WorldServer world : server.worldServers) {
                if (world == null || world.isRemote) continue;
                attempted++;
                try {
                    worlds.add(new WorldSnapshot(world.provider.dimensionId, nonNegative(world.getTotalWorldTime()), world.getWorldTime(), world.difficultySetting == null ? -1 : world.difficultySetting.getDifficultyId(), world.isRaining(), world.isThundering(), world.playerEntities == null ? 0 : world.playerEntities.size()));
                    succeeded++;
                    if (world.playerEntities != null) {
                        for (Object value : world.playerEntities) {
                            if (!(value instanceof EntityPlayer)) continue;
                            try { players.add(player((EntityPlayer) value, world)); }
                            catch (RuntimeException exception) { if (warnings.size() < ObservationDiagnostics.MAX_WARNINGS) warnings.add("player_capture_failed"); }
                        }
                    }
                } catch (RuntimeException exception) {
                    if (warnings.size() < ObservationDiagnostics.MAX_WARNINGS) warnings.add("world_capture_failed");
                }
            }
        }
        ObservationStatus status = server == null ? ObservationStatus.FAILED : (attempted == 0 ? ObservationStatus.COMPLETE : (succeeded == attempted ? ObservationStatus.COMPLETE : (succeeded == 0 ? ObservationStatus.FAILED : ObservationStatus.PARTIAL)));
        return new ObservationFrame("capture:" + sequence + ":" + serverTick, serverTick, sequence, worlds, players, new ObservationDiagnostics(status, attempted, succeeded, warnings));
    }

    private static PlayerSnapshot player(EntityPlayer player, WorldServer world) {
        GameProfile profile = player.getGameProfile();
        UUID id = profile == null ? player.getUniqueID() : profile.getId();
        String name = profile == null || profile.getName() == null ? "" : profile.getName();
        int px = (int) Math.floor(player.posX);
        int pz = (int) Math.floor(player.posZ);
        BiomeGenBase biome = world.getBiomeGenForCoords(px, pz);
        EnvironmentSnapshot environment = new EnvironmentSnapshot(biome == null ? -1 : biome.biomeID, biome == null || biome.biomeName == null ? "UNKNOWN" : biome.biomeName, world.getBlockLightValue(px, (int) Math.floor(player.posY), pz), world.canBlockSeeTheSky(px, (int) Math.floor(player.posY), pz), player.posY);
        FoodStats food = player.getFoodStats();
        return new PlayerSnapshot(id, name, player.dimension, player.posX, player.posY, player.posZ, player.getHealth(), player.getMaxHealth(), food == null ? 0 : food.getFoodLevel(), player.getTotalArmorValue(), player.experienceLevel, player.isPlayerSleeping(), player.isEntityAlive(), environment, inventory(player.inventory));
    }

    private static List<InventorySlotSnapshot> inventory(InventoryPlayer inventory) {
        List<InventorySlotSnapshot> slots = new ArrayList<InventorySlotSnapshot>();
        if (inventory == null) return slots;
        if (inventory.mainInventory != null) for (int i = 0; i < inventory.mainInventory.length && slots.size() < MAX_INVENTORY_SLOTS; i++) add(slots, i, inventory.mainInventory[i], i < 9 ? "HOTBAR" : "MAIN");
        if (inventory.armorInventory != null) for (int i = 0; i < inventory.armorInventory.length && slots.size() < MAX_INVENTORY_SLOTS; i++) add(slots, 100 + i, inventory.armorInventory[i], "ARMOR");
        Collections.sort(slots);
        return slots;
    }

    private static void add(List<InventorySlotSnapshot> slots, int index, ItemStack stack, String kind) {
        if (stack == null) return;
        Item item = stack.getItem();
        String registry = item == null ? InventorySlotSnapshot.UNKNOWN_REGISTRY : Item.itemRegistry.getNameForObject(item);
        slots.add(new InventorySlotSnapshot(index, registry, Math.max(0, stack.stackSize), Math.max(0, stack.getItemDamage()), Math.max(0, stack.getMaxDamage()), stack.isItemStackDamageable(), stack.hasTagCompound(), kind));
    }

    private static long nonNegative(long value) { return value < 0 ? 0 : value; }
}
