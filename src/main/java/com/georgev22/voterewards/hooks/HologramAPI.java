package com.georgev22.voterewards.hooks;

import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.MinecraftUtils;
import com.georgev22.library.utilities.Utils;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.interfaces.Holograms;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.github.unldenis.hologram.Hologram;
import com.github.unldenis.hologram.HologramPool;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author GeorgeV22
 */
public class HologramAPI implements Holograms {

    private final FileManager fileManager = FileManager.getInstance();
    private final CFG dataCFG = fileManager.getData();
    private final FileConfiguration data = dataCFG.getFileConfiguration();
    private final VoteReward voteReward = VoteReward.getInstance();
    private final ObjectMap<String, Object> hologramMap = new ConcurrentObjectMap<>();
    private final HologramPool hologramPool = new HologramPool(voteReward.getPlugin(), 70, 0, 0);

    /**
     * Create a hologram
     *
     * @param name     Hologram name.
     * @param location Hologram location.
     * @param type     Hologram type.
     * @param save     Save the hologram in the file.
     * @return {@link Hologram} instance.
     */
    @NotNull
    public Hologram create(String name, MinecraftUtils.SerializableLocation location, String type, boolean save) {
        Hologram hologram = getHologramMap().get(name) != null ? (Hologram) getHologramMap().get(name) : null;
        if (hologram == null) {
            Hologram.Builder builder = Hologram.builder().location(location.getLocation());

            for (String line : fileManager.getConfig().getFileConfiguration().getStringList("Holograms." + type)) {
                builder.addLine(MinecraftUtils.colorize(line), false);
            }
            hologram = builder.build(hologramPool);
        }

        if (save) {
            data.set("Holograms." + name + ".location", location);
            data.set("Holograms." + name + ".type", type);
            dataCFG.saveFile();
        }
        getHologramMap().append(name, hologram);
        return hologram;
    }

    /**
     * Remove a hologram.
     *
     * @param name Hologram name.
     * @param save Save the changes in file.
     */
    public void remove(String name, boolean save) {
        Hologram hologram = (Hologram) getHologramMap().remove(name);

        hologramPool.remove(hologram);

        if (save) {
            data.set("Holograms." + name, null);
            dataCFG.saveFile();
        }
    }

    /**
     * Show a hologram to a specific player.
     *
     * @param name   Hologram name.
     * @param player Player to show the hologram.
     */
    public void show(String name, Player player) {
        Hologram hologram = (Hologram) getHologramMap().get(name);

        if (hologram == null) {
            MinecraftUtils.msg(player, "Hologram " + name + " doesn't exist");
            return;
        }
        hologram.show(player);
    }

    /**
     * Hide a hologram from a specific player.
     *
     * @param name   Hologram name.
     * @param player Player to hide the hologram.
     */
    public void hide(String name, Player player) {
        Hologram hologram = (Hologram) getHologramMap().get(name);

        if (hologram == null) {
            MinecraftUtils.msg(player, "Hologram " + name + " doesn't exist");
            return;
        }

        hologram.hide(player);
    }

    /**
     * Show a hologram from a specific player.
     *
     * @param hologram Hologram instance.
     * @param player   Player to hide the hologram.
     */
    @Override
    public void show(@NotNull Object hologram, Player player) {
        ((Hologram) hologram).show(player);
    }

    /**
     * Hide a hologram from a specific player.
     *
     * @param hologram Hologram instance.
     * @param player   Player to hide the hologram.
     */
    @Override
    public void hide(@NotNull Object hologram, Player player) {
        ((Hologram) hologram).hide(player);
    }

    /**
     * Return all holograms in a collection.
     *
     * @return all holograms in a collection.
     */
    @NotNull
    public Collection<Object> getHolograms() {
        return getHologramMap().values();
    }

    /**
     * Return a {@link Hologram} from hologram name.
     *
     * @param name Hologram name
     * @return a {@link Hologram} from hologram name.
     */
    public Hologram getHologram(String name) {
        return (Hologram) getHologramMap().get(name);
    }

    /**
     * Check if a hologram exists
     *
     * @param name Hologram name.
     * @return if the hologram exists
     */
    public boolean hologramExists(String name) {
        return getHologramMap().get(name) != null;
    }

    /**
     * Update the lines in a specific hologram
     *
     * @param hologram     {@link Object} instance to change the lines.
     * @param lines        The new lines.
     * @param placeholders The placeholders.
     * @return the updated {@link Object} instance.
     */
    @Override
    public Object updateHologram(Object hologram, @NotNull List<String> lines, ObjectMap<String, String> placeholders) {
        int i = 0;
        for (final String key : lines) {
            for (String placeholder : placeholders.keySet()) {
                if (key.contains(placeholder)) {
                    Hologram hologram1 = (Hologram) hologram;
                    hologram1.setLine(i, Utils.placeHolder(MinecraftUtils.colorize(key), placeholders, true));
                    break;
                }
            }
            ++i;
        }
        return hologram;
    }

    /**
     * Update all {@link Hologram} instances.
     */
    public void updateAll() {
        if (data.get("Holograms") == null)
            return;
        for (String hologramName : Objects.requireNonNull(data.getConfigurationSection("Holograms")).getKeys(false)) {
            hologramPool.remove(getHologram(hologramName));
            Hologram.Builder builder = new Hologram.Builder().location(getHologram(hologramName).getLocation());
            int i = 0;
            for (String line : voteReward.getConfig().getStringList("Holograms." + data.getString("Holograms." + hologramName + ".type"))) {
                builder.addLine(MinecraftUtils.colorize(Utils.placeHolder(line, getPlaceholderMap(), true)), false);
            }
            getHologramMap().append(hologramName, builder.build(hologramPool));
            getPlaceholderMap().clear();
        }
    }

    /**
     * @return A map with all the holograms.
     */
    public ObjectMap<String, Object> getHologramMap() {
        return hologramMap;
    }

    /**
     * A map with all hologram placeholders
     *
     * @return a map with all hologram placeholders
     */
    public ObjectMap<String, String> getPlaceholderMap() {
        return VoteUtils.getPlaceholdersMap(data, fileManager);
    }

    private boolean isHooked = false;


    public void setHook(boolean isHooked) {
        this.isHooked = isHooked;
    }

    public boolean isHooked() {
        return isHooked;
    }
}
