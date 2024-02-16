package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.voterewards.VoteReward;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings({"UnusedReturnValue", "unchecked"})
public class User {

    private final UUID entityId;
    @Getter
    private final ConcurrentObjectMap<String, Object> customData;

    public User(UUID uuid) {
        this.entityId = uuid;
        this.customData = new ConcurrentObjectMap<>();
        this.addCustomDataIfNotExists("name", Bukkit.getOfflinePlayer(uuid).getName());
        this.addCustomDataIfNotExists("votes", 0);
        this.addCustomDataIfNotExists("daily", 0);
        this.addCustomDataIfNotExists("last", 0L);
        this.addCustomDataIfNotExists("voteparty", 0);
        this.addCustomDataIfNotExists("totalvotes", 0);
        this.addCustomDataIfNotExists("services", new ArrayList<String>());
        this.addCustomDataIfNotExists("servicesLastVote", new HashObjectMap<String, Long>());
    }

    public String name() {
        return this.getCustomData("name");
    }

    public void name(String name) {
        this.addCustomData("name", name);
    }

    public int votes() {
        return this.getCustomData("votes");
    }

    public void votes(int votes) {
        this.addCustomData("votes", votes);
    }

    public int dailyVotes() {
        return this.getCustomData("daily");
    }

    public void dailyVotes(int dailyVotes) {
        this.addCustomData("daily", dailyVotes);
    }

    public long lastVote() {
        return this.getCustomData("last");
    }

    public void lastVote(long lastVote) {
        this.addCustomData("last", lastVote);
    }

    public int voteparty() {
        return this.getCustomData("voteparty");
    }

    public void voteparty(int voteparty) {
        this.addCustomData("voteparty", voteparty);
    }

    public int totalVotes() {
        return this.getCustomData("totalvotes");
    }

    public void totalVotes(int totalVotes) {
        this.addCustomData("totalvotes", totalVotes);
    }

    public ObjectMap<String, Long> servicesLastVote() {
        return this.getCustomData("servicesLastVote");
    }

    public void servicesLastVote(ObjectMap<String, Long> servicesLastVote) {
        this.addCustomData("servicesLastVote", servicesLastVote);
    }

    public void addServicesLastVote(String serviceName) {
        if (servicesLastVote() != null) {
            servicesLastVote(servicesLastVote().append(serviceName, System.currentTimeMillis()));
        }
    }

    public ArrayList<String> services() {
        return this.getCustomData("services");
    }

    public void addServices(String serviceName) {
        if (services() != null) {
            ArrayList<String> services = services();
            services.add(serviceName);
            services(services);
        }
    }

    public void services(ArrayList<String> services) {
        this.addCustomData("services", services);
    }

    public static ObjectMap<String, String> placeholders(@NotNull User user) {
        return new HashObjectMap<String, String>()
                .append("%player%", user.name())
                .append("%votes%", String.valueOf(user.votes()))
                .append("%totalVotes%", String.valueOf(user.totalVotes()))
                .append("%daily%", String.valueOf(user.dailyVotes()))
                .append("%voteparties%", String.valueOf(user.voteparty()));
    }

    public static void reset(@NotNull User user, boolean allTime) {
        user.addCustomData("name", Bukkit.getOfflinePlayer(user.getId()).getName());
        user.addCustomData("votes", 0);
        user.addCustomData("daily", 0);
        user.addCustomData("last", 0L);
        user.addCustomData("voteparty", 0);
        if (allTime) {
            user.addCustomData("totalvotes", 0);
        }
        user.addCustomData("servicesLastVote", new HashObjectMap<String, Long>());
        user.addCustomData("services", new ArrayList<String>());
        VoteReward.getInstance().getPlayerDataManager().save(user);
    }

    public UUID getId() {
        return this.entityId;
    }

    public User addCustomData(String key, Object value) {
        this.getCustomData().append(key, value);
        return this;
    }

    /**
     * Adds custom data to the User with the specified key and value if the key does not already exist.
     *
     * @param key   the key of the custom data
     * @param value the value of the custom data
     * @return the updated User with the added custom data (if the key did not already exist)
     */
    public User addCustomDataIfNotExists(String key, Object value) {
        this.getCustomData().appendIfTrue(key, value, !this.getCustomData().containsKey(key));
        return this;
    }

    /**
     * Retrieves the value of the custom data associated with the specified key.
     *
     * @param key the key of the custom data
     * @param <T> the type of the value to retrieve
     * @return the value associated with the specified key, or {@code null} if the key does not exist
     */
    public <T> T getCustomData(String key) {
        return (T) getCustomData().get(key);
    }

}
