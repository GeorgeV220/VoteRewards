package com.georgev22.voterewards.votereward;

import com.georgev22.library.extensions.Extensions;
import com.georgev22.library.extensions.java.JavaExtension;
import com.georgev22.api.libraryloader.exceptions.InvalidDependencyException;
import com.georgev22.api.libraryloader.exceptions.UnknownDependencyException;
import com.georgev22.voterewards.VoteReward;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class VoteRewardExtension extends JavaExtension implements VoteRewardImpl {

    private static VoteRewardExtension instance;

    private static VoteReward voteRewardInstance;

    private Description description;

    /**
     * Return the VoteRewardExtension instance
     *
     * @return VoteRewardExtension instance
     */
    public static VoteRewardExtension getInstance() {
        return instance;
    }

    public static VoteReward getVoteReward() {
        return voteRewardInstance;
    }

    @Override
    public void onLoad() {
        instance = this;
        description = new Description(getDescription());
        voteRewardInstance = new VoteReward(this);
        try {
            getVoteReward().onLoad();
        } catch (UnknownDependencyException | InvalidDependencyException e) {
            getLogger().log(Level.WARNING, "Error: ", e);
        }
        Extensions.getExtensionManager().getClassInstances().forEach((key, value) -> {
            if (key.equals("plugin") & (Plugin.class.isAssignableFrom(value.getClass()) | JavaPlugin.class.isAssignableFrom(value.getClass()))) {
                JavaPlugin plugin = (JavaPlugin) value;
                getVoteReward().setPlugin(plugin);
                getLogger().info("[VoteRewards] Plugin " + plugin.getName() + " hooked into VoteRewards!");
            }
        });
    }

    @Override
    public void onEnable() {
        try {
            getVoteReward().onEnable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        getVoteReward().onDisable();
    }

    @Override
    public Description getDesc() {
        return description;
    }

    @Override
    public boolean setEnable(boolean enabled) {
        super.setEnabled(enabled);
        return isEnabled();
    }
}