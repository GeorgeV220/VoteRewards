package com.georgev22.voterewards.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import org.jetbrains.annotations.NotNull;

public abstract class Command extends BaseCommand {

    protected final ObjectMap<String, String> placeholders = new HashObjectMap<>();
    protected final FileManager fm = FileManager.getInstance();
    protected VoteReward voteReward = VoteReward.getInstance();

    public abstract void execute(@NotNull CommandIssuer commandIssuer, String[] args);

    @HelpCommand
    @Subcommand("help")
    public void onHelp(@NotNull CommandIssuer commandIssuer, @NotNull CommandHelp commandHelp, final String @NotNull [] args) {
        commandHelp.showHelp();
    }

}
