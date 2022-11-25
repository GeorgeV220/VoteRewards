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
public class VoteRewardPlugin extends JavaPlugin implements VoteRewardImpl {

    @Getter
    private static VoteRewardPlugin instance = null;

    private static VoteReward voteRewardInstance = null;

    private Description description;

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
        description = new Description(getDescription());
        voteRewardInstance = new VoteReward(this);
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
            getVoteReward().setPlugin(this);
            getVoteReward().onEnable();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error: ", e);
        }
    }

    @Override
    public void onDisable() {
        getVoteReward().onDisable();
        Bukkit.getScheduler().cancelTasks(this);
    }


    public void setEnabled0(boolean enabled) {
        setEnabled(enabled);
    }


    @Override
    public Description getDesc() {
        return description;
    }

    @Override
    public boolean setEnable(boolean enabled) {
        setEnabled(enabled);
        return isEnabled();
    }
}
