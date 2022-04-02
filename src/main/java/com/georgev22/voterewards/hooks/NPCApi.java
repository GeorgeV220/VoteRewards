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
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class NPCApi {

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

    /**
     * Creates a profile for NPCs.
     *
     * @return The new profile
     */
    public static @NotNull Profile createProfile() {
        Profile profile = new Profile(UUID.fromString("a4f5cd7f-362f-4044-931e-7128b4e6bad9"));
        profile.complete();

        profile.setName("Notch");

        profile.setUniqueId(new UUID(random.nextLong(), 0));

        return profile;
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
            NPC.Builder builder = NPC.builder().location(location).lookAtPlayer(true).profile(createProfile());
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

    public static class Tick extends BukkitRunnable {
        @Override
        public void run() {
            for (Map.Entry<String, ObjectMap.Pair<NPC, Integer>> npcEntry : getNPCMap().entrySet()) {
                NPC npc = npcEntry.getValue().getKey();
                npc.getProfile().setName(VoteUtils.getTopPlayer(npcEntry.getValue().getValue()));
            }
        }
    }

    public static ObjectMap<String, ObjectMap.Pair<NPC, Integer>> getNPCMap() {
        return npcMap;
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
