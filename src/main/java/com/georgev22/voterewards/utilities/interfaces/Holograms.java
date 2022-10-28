package com.georgev22.voterewards.utilities.interfaces;

import com.georgev22.api.maps.ObjectMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Holograms {

    /**
     * Create a hologram
     *
     * @param name     Hologram name.
     * @param location Hologram location.
     * @param type     Hologram type.
     * @param save     Save the hologram in the file.
     * @return {@link Object instance.
     */
    Object create(String name, Location location, String type, boolean save);

    /**
     * Remove a hologram.
     *
     * @param name Hologram name.
     * @param save Save the changes in file.
     */
    void remove(String name, boolean save);

    /**
     * Show a hologram to a specific player.
     *
     * @param name   Hologram name.
     * @param player Player to show the hologram.
     */
    void show(String name, Player player);

    /**
     * Hide a hologram from a specific player.
     *
     * @param name   Hologram name.
     * @param player Player to hide the hologram.
     */
    void hide(String name, Player player);

    /**
     * Show a hologram from a specific player.
     *
     * @param hologram Hologram instance.
     * @param player   Player to hide the hologram.
     */
    void show(@NotNull Object hologram, Player player);

    /**
     * Hide a hologram from a specific player.
     *
     * @param hologram Hologram instance.
     * @param player   Player to hide the hologram.
     */
    void hide(@NotNull Object hologram, Player player);

    /**
     * Return all holograms in a collection.
     *
     * @return all holograms in a collection.
     */
    @NotNull Collection<Object> getHolograms();

    /**
     * Return a {@link Object from hologram name.
     * <p>
     *
     * @param name Hologram name
     * @return a {@link Object from hologram name.
     */
    Object getHologram(String name);

    /**
     * Check if a hologram exists
     *
     * @param name Hologram name.
     * @return if the hologram exists
     */
    boolean hologramExists(String name);

    /**
     * Update the lines in a specific hologram
     *
     * @param hologram     {@link Object} instance to change the lines.
     * @param lines        The new lines.
     * @param placeholders The placeholders.
     * @return the updated {@link Object} instance.
     */
    @Contract("_, _, _ -> param1")
    Object updateHologram(Object hologram, @NotNull List<String> lines, ObjectMap<String, String> placeholders);

    /**
     * Update all {@link Object instances.
     */
    void updateAll();

    /**
     * @return A map with all the holograms.
     */
    ObjectMap<String, Object> getHologramMap();

    /**
     * A map with all hologram placeholders
     *
     * @return a map with all hologram placeholders
     */
    ObjectMap<String, String> getPlaceholderMap();

    void setHook(boolean b);


    boolean isHooked();

    class HologramsNoop implements Holograms {

        /**
         * Create a hologram
         *
         * @param name     Hologram name.
         * @param location Hologram location.
         * @param type     Hologram type.
         * @param save     Save the hologram in the file.
         * @return {@link Object instance.
         */
        @Override
        public Object create(String name, Location location, String type, boolean save) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Remove a hologram.
         *
         * @param name Hologram name.
         * @param save Save the changes in file.
         */
        @Override
        public void remove(String name, boolean save) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Show a hologram to a specific player.
         *
         * @param name   Hologram name.
         * @param player Player to show the hologram.
         */
        @Override
        public void show(String name, Player player) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Hide a hologram from a specific player.
         *
         * @param name   Hologram name.
         * @param player Player to hide the hologram.
         */
        @Override
        public void hide(String name, Player player) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Show a hologram from a specific player.
         *
         * @param hologram Hologram instance.
         * @param player   Player to hide the hologram.
         */
        @Override
        public void show(@NotNull Object hologram, Player player) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Hide a hologram from a specific player.
         *
         * @param hologram Hologram instance.
         * @param player   Player to hide the hologram.
         */
        @Override
        public void hide(@NotNull Object hologram, Player player) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Return all holograms in a collection.
         *
         * @return all holograms in a collection.
         */
        @Override
        public @NotNull Collection<Object> getHolograms() {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Return a {@link Object from hologram name.
         * <p>
         *
         * @param name Hologram name
         * @return a {@link Object from hologram name.
         */
        @Override
        public Object getHologram(String name) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Check if a hologram exists
         *
         * @param name Hologram name.
         * @return if the hologram exists
         */
        @Override
        public boolean hologramExists(String name) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
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
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * Update all {@link Object instances.
         */
        @Override
        public void updateAll() {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * @return A map with all the holograms.
         */
        @Override
        public ObjectMap<String, Object> getHologramMap() {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        /**
         * A map with all hologram placeholders
         *
         * @return a map with all hologram placeholders
         */
        @Override
        public ObjectMap<String, String> getPlaceholderMap() {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        @Override
        public void setHook(boolean b) {
            throw new UnsupportedOperationException("Holograms are not hooked!");
        }

        @Override
        public boolean isHooked() {
            return false;
        }


    }


}

