package com.georgev22.voterewards.hooks;

import com.georgev22.api.maps.HashObjectMap;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.minecraft.configmanager.CFG;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.profile.Profile;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class NPCAPI {

    private final static FileManager fileManager = FileManager.getInstance();
    private final static CFG dataCFG = fileManager.getData();
    private final static FileConfiguration data = dataCFG.getFileConfiguration();
    private final static VoteRewardPlugin mainPlugin = VoteRewardPlugin.getInstance();
    private static final ObjectMap<String, ObjectMap.Pair<NPC, Integer>> npcMap = new HashObjectMap<>();
    private static final Random random = new Random();
    private static final NPCPool npcPool = NPCPool.builder(mainPlugin)
            .spawnDistance(50)
            .actionDistance(20)
            .tabListRemoveTicks(30)
            .build();
    private static final ObjectMap<String, JsonObject> cachedSkins = new HashObjectMap<>();

    /**
     * Creates a profile for NPCs.
     *
     * @return The new profile
     */
    public static @NotNull Profile createProfile(int position) {
        Profile profile = new Profile(Bukkit.getOfflinePlayerIfCached(VoteUtils.getTopPlayer(position)) != null ? Objects.requireNonNull(Bukkit.getOfflinePlayerIfCached(VoteUtils.getTopPlayer(position))).getUniqueId() : UUID.fromString("a4f5cd7f-362f-4044-931e-7128b4e6bad9"));
        profile.complete();

        profile.setName(VoteUtils.getTopPlayer(position));

        if (!Bukkit.getOnlineMode()) {
            Collection<Profile.Property> properties = Lists.newArrayList();

            properties.add(getSkin(VoteUtils.getTopPlayer(position)));

            profile.setProperties(properties);
        }

        profile.setUniqueId(new UUID(random.nextLong(), 0));

        return profile;
    }

    public static Profile.@Nullable Property getSkin(String skinName) {
        if (cachedSkins.containsKey(skinName)) {
            return new Profile.Property("textures", cachedSkins.get(skinName).get("value").getAsString(), cachedSkins.get(skinName).get("signature").getAsString());
        } else {
            InputStreamReader reader_1;
            try {
                URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + skinName);
                InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
                String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

                URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                reader_1 = new InputStreamReader(url_1.openStream());
                JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
                String texture = textureProperty.get("value").getAsString();
                String signature = textureProperty.get("signature").getAsString();
                cachedSkins.put(skinName, textureProperty);
                return new Profile.Property("textures", texture, signature);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Creates a new NPC
     *
     * @param name     NPC name
     * @param position VoteTop player position
     * @param location NPC location
     * @param save     Save the NPC
     * @return a new created NPC
     */
    public static @NotNull NPC create(String name, int position, Location location, boolean save) {
        NPC npc = getNPCMap().get(name) != null ? getNPCMap().get(name).getKey() : null;
        if (npc == null) {
            NPC.Builder builder = NPC.builder().location(location).lookAtPlayer(true).imitatePlayer(false).profile(save ? createProfile(position) : new Profile("Notch"));
            npc = builder.build(npcPool);
        }

        if (save) {
            data.set("NPCs." + name + ".location", location);
            data.set("NPCs." + name + ".position", position);
            dataCFG.saveFile();
        }
        getNPCMap().append(name, ObjectMap.Pair.create(npc, position));
        return npc;
    }

    public static void remove(String name, boolean save) {
        NPC npc = getNPCMap().get(name) != null ? getNPCMap().get(name).getKey() : null;
        if (npc == null) {
            return;
        }

        npcPool.removeNPC(getNPCMap().get(name).getKey().getEntityId());

        if (save) {
            data.set("NPCs." + name, null);
            dataCFG.saveFile();
        }
        getNPCMap().remove(name);
    }

    /**
     * Show a npc from a specific player.
     *
     * @param npc    NPC instance.
     * @param player Player to hide the NPC.
     */
    public static void show(@NotNull NPC npc, Player player) {
        npc.removeExcludedPlayer(player);
    }

    /**
     * Hide a NPC from a specific player.
     *
     * @param npc    NPC instance.
     * @param player Player to hide the NPC.
     */
    public static void hide(@NotNull NPC npc, Player player) {
        npc.addExcludedPlayer(player);
    }

    /**
     * Return a {@link NPC} from hologram name.
     *
     * @param name NPC name
     * @return a {@link NPC} from npc name.
     */
    public static NPC getNPC(String name) {
        return getNPCMap().get(name).getKey();
    }

    /**
     * Check if a npc exists
     *
     * @param name NPC name.
     * @return if the npc exists
     */
    public static boolean npcExists(String name) {
        return getNPCMap().get(name) != null;
    }

    /**
     * Update a specific NPC
     *
     * @param npcName  NPC name to update.
     * @param location The new location.
     * @param position The position.
     * @param save     Save the new changes to the config.
     * @return the updated {@link NPC} instance.
     */
    public static @NotNull NPC updateNPC(@NotNull String npcName, @NotNull Location location, int position, boolean save) {
        if (npcExists(npcName))
            remove(npcName, false);
        NPC.Builder builder = NPC.builder().profile(createProfile(position)).imitatePlayer(false).lookAtPlayer(true).location(location);
        getNPCMap().append(npcName, ObjectMap.Pair.create(builder.build(npcPool), position));
        if (save) {
            data.set("NPCs." + npcName + ".location", location);
            data.set("NPCs." + npcName + ".position", position);
            dataCFG.saveFile();
        }
        return getNPC(npcName);
    }

    /**
     * Update all {@link NPC} instances without saving.
     */
    public static void updateAll() {
        if (data.get("NPCs") == null)
            return;
        for (String npcName : Objects.requireNonNull(data.getConfigurationSection("NPCs")).getKeys(false)) {
            updateNPC(npcName, Objects.requireNonNull(data.getLocation("NPCs." + npcName + ".location")), data.getInt("NPCs." + npcName + ".position"), false);
        }
    }

    /**
     * Return all NPCs in a collection.
     *
     * @return all NPCs in a collection.
     */
    public static ObjectMap<String, ObjectMap.Pair<NPC, Integer>> getNPCMap() {
        return npcMap;
    }

    /**
     * Return all NPCs in a collection.
     *
     * @return all NPCs in a collection.
     */
    public static @NotNull Collection<ObjectMap.Pair<NPC, Integer>> getNPCs() {
        return getNPCMap().values();
    }

    //IGNORE
    private static boolean a = false;


    public static void setHook(boolean b) {
        a = b;
    }

    public static boolean isHooked() {
        return a;
    }
    //
}
