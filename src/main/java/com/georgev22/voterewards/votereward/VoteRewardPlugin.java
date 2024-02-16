package com.georgev22.voterewards.votereward;

import com.georgev22.api.libraryloader.exceptions.InvalidDependencyException;
import com.georgev22.api.libraryloader.exceptions.UnknownDependencyException;
import com.georgev22.voterewards.VoteReward;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class VoteRewardPlugin extends JavaPlugin implements VoteRewardImpl {

    @Getter
    private static VoteRewardPlugin instance = null;

    @Getter
    private static VoteReward voteRewardInstance = null;

    private Description description;

    @Override
    public void onLoad() {
        instance = this;
        try {
            this.description = new Description(
                    getDescription().getName(),
                    getDescription().getVersion(),
                    getDescription().getMain(),
                    getDescription().getAuthors()
            );
            voteRewardInstance = new VoteReward(this);
            voteRewardInstance.setPlugin(this);
            voteRewardInstance.onLoad();
        } catch (UnknownDependencyException | InvalidDependencyException e) {
            getLogger().log(Level.SEVERE, "Error: ", e);
        }
    }

    @Override
    public void onEnable() {
        try {
            voteRewardInstance.onEnable();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error: ", e);
        }
    }

    @Override
    public void onDisable() {
        voteRewardInstance.onDisable();
        Bukkit.getScheduler().cancelTasks(this);
    }


    @Override
    public Description getDesc() {
        return description;
    }

    @Override
    public void setEnable(boolean enabled) {
        setEnabled(enabled);
    }

}
