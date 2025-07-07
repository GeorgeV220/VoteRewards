package com.georgev22.voterewards.command.commands;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.CommandCompletion;
import com.georgev22.voterewards.command.annotation.Permission;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@CommandAlias({"fakevote", "vrfake", "vfake"})
@CommandCompletion("@players")
@Permission("voterewards.fakevote")
public class FakeVoteCommand extends VoteRewardsBaseCommand {

    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        if (args.length == 0) {
            if (!commandIssuer.isPlayer()) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cNot enough arguments");
                return;
            }
            process(((Player) commandIssuer.getIssuer()).getName(), "fakeVote");
        } else if (args.length == 1) {
            process(args[0], "fakeVote");
        } else {
            process(args[0], args[1]);
        }
    }

    private void process(String userName, String serviceName) {
        Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(
                new Vote(serviceName, userName, "localhost", String.valueOf(Instant.now().toEpochMilli()))
        ));
    }
}
