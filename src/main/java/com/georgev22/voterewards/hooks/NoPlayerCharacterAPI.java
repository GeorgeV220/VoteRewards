package com.georgev22.voterewards.hooks;

import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.MinecraftUtils;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.profile.Profile;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
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

public class NoPlayerCharacterAPI {

    private final static FileManager fileManager = FileManager.getInstance();
    private final static CFG dataCFG = fileManager.getData();
    private final static FileConfiguration data = dataCFG.getFileConfiguration();
    private final static VoteReward main = VoteReward.getInstance();
    private static final ObjectMap<String, ObjectMap.Pair<NPC, Integer>> npcMap = new ConcurrentObjectMap<>();
    private static final Random random = new Random();
    private static final NPCPool npcPool = NPCPool.builder(main.getPlugin())
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
    public @NotNull Profile createProfile(int position) {
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

    public Profile.@Nullable Property getSkin(String skinName) {
        if (cachedSkins.containsKey(skinName)) {
            return new Profile.Property("textures", cachedSkins.get(skinName).get("value").getAsString(), cachedSkins.get(skinName).get("signature").getAsString());
        } else {
            InputStreamReader reader_1;
            try {
                URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + skinName);
                InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
                String uuid = JsonParser.parseReader(reader_0).getAsJsonObject().get("id").getAsString();

                URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                reader_1 = new InputStreamReader(url_1.openStream());
                JsonObject textureProperty = JsonParser.parseReader(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
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
    public @NotNull NPC create(String name, int position, MinecraftUtils.SerializableLocation location, boolean save) {
        NPC npc = getNPCMap().get(name) != null ? getNPCMap().get(name).key() : null;
        if (npc == null) {
            NPC.Builder builder = NPC.builder().location(location.getLocation()).lookAtPlayer(true).imitatePlayer(false).profile(save ? createProfile(position) : new Profile("Notch"));
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

    public void remove(String name, boolean save) {
        NPC npc = getNPCMap().get(name) != null ? getNPCMap().get(name).key() : null;
        if (npc == null) {
            return;
        }

        npcPool.removeNPC(getNPCMap().get(name).key().getEntityId());

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
    public void show(@NotNull NPC npc, Player player) {
        npc.removeExcludedPlayer(player);
    }

    /**
     * Hide a NPC from a specific player.
     *
     * @param npc    NPC instance.
     * @param player Player to hide the NPC.
     */
    public void hide(@NotNull NPC npc, Player player) {
        npc.addExcludedPlayer(player);
    }

    /**
     * Return a {@link NPC} from hologram name.
     *
     * @param name NPC name
     * @return a {@link NPC} from npc name.
     */
    public NPC getNPC(String name) {
        return getNPCMap().get(name).key();
    }

    /**
     * Check if a npc exists
     *
     * @param name NPC name.
     * @return if the npc exists
     */
    public boolean npcExists(String name) {
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
    public @NotNull NPC updateNPC(@NotNull String npcName, @NotNull MinecraftUtils.SerializableLocation location, int position, boolean save) {
        if (npcExists(npcName))
            remove(npcName, false);
        NPC.Builder builder = NPC.builder().profile(createProfile(position)).imitatePlayer(false).lookAtPlayer(true).location(location.getLocation());
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
    public void updateAll() {
        if (data.get("NPCs") == null)
            return;
        for (String npcName : Objects.requireNonNull(data.getConfigurationSection("NPCs")).getKeys(false)) {
            updateNPC(npcName, data.getSerializable("NPCs." + npcName + ".location", MinecraftUtils.SerializableLocation.class), data.getInt("NPCs." + npcName + ".position"), false);
        }
    }

    /**
     * Return all NPCs in a collection.
     *
     * @return all NPCs in a collection.
     */
    public ObjectMap<String, ObjectMap.Pair<NPC, Integer>> getNPCMap() {
        return npcMap;
    }

    /**
     * Return all NPCs in a collection.
     *
     * @return all NPCs in a collection.
     */
    public @NotNull Collection<ObjectMap.Pair<NPC, Integer>> getNPCs() {
        return getNPCMap().values();
    }

    //IGNORE
    private boolean a = false;


    public void setHook(boolean b) {
        a = b;
    }

    public boolean isHooked() {
        return a;
    }
    //
}
