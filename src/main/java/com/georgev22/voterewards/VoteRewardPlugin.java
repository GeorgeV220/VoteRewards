package com.georgev22.voterewards;

import com.georgev22.api.libraryloader.LibraryLoader;
import com.georgev22.api.libraryloader.annotations.MavenLibrary;
import com.georgev22.api.libraryloader.exceptions.InvalidDependencyException;
import com.georgev22.api.libraryloader.exceptions.UnknownDependencyException;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.logging.Level;

@MavenLibrary(value = "com.github.GeorgeV220:API:v8.1.0:https://jitpack.io/")
public class VoteRewardPlugin extends JavaPlugin {

    @Getter
    private static VoteRewardPlugin instance = null;

    private static VoteReward voteRewardInstance = null;

    public static VoteReward getVoteReward() {
        return voteRewardInstance;
    }

    public VoteRewardPlugin() {
        super();
    }

    protected VoteRewardPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        instance = this;
        voteRewardInstance = new VoteReward(this.getDataFolder(), getLogger(), true);
        try {
            new LibraryLoader(this.getClass(), this.getDataFolder()).loadAll();
            getVoteReward().onLoad();
        } catch (UnknownDependencyException | InvalidDependencyException e) {
            getLogger().log(Level.SEVERE, "Error: ", e);
        }
    }

    @Override
    public void onEnable() {
        try {
            Bukkit.getScheduler().runTaskTimer(this, () -> com.georgev22.library.scheduler.SchedulerManager.getScheduler().mainThreadHeartbeat(Bukkit.getServer().getCurrentTick()), 0L, 1L);
            getVoteReward().setPlugin(this);
            getVoteReward().onEnable();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error: ", e);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        getVoteReward().onDisable();
    }


    public void setEnabled0(boolean enabled) {
        setEnabled(enabled);
    }


}
