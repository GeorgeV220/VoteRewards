package com.georgev22.voterewards.command.commands;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.command.BaseCommand;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public abstract class VoteRewardsBaseCommand extends BaseCommand {
    protected final ObjectMap<String, String> placeholders = new HashObjectMap<>();
    protected final FileManager fm = FileManager.getInstance();
    protected VoteReward voteReward = VoteReward.getInstance();

    @Override
    public void addSubcommand(@NotNull BaseCommand subcommand) {
        try {
            super.addSubcommand(subcommand);
        } catch (Exception e) {
            voteReward.getLogger().log(Level.SEVERE, "Failed to register subcommand " + subcommand.getClass().getName(), e);
        }
    }
}
