package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.utilities.EntityManager;
import com.georgev22.voterewards.VoteReward;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.UUID;

public class User extends EntityManager.Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    public User(UUID uuid) {
        super(uuid);
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

}
