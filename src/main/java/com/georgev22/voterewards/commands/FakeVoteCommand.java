package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.library.minecraft.MinecraftUtils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("fakevote|vrfake|vfake")
public class FakeVoteCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.fakevote}")
    @CommandCompletion("@players")
    @Syntax("<player> [servicename]")
    @CommandPermission("voterewards.fakevote")
    public void execute(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MinecraftUtils.msg(sender, "&c&l(!) &cNot enough arguments");
                return;
            }
            process(sender.getName(), "fakeVote");
        } else if (args.length == 1) {
            process(args[0], "fakeVote");
        } else {
            process(args[0], args[1]);
        }
    }

    private void process(String userName, String serviceName) {
        Vote vote = new Vote();
        vote.setUsername(userName);
        vote.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        vote.setAddress("localhost");
        vote.setServiceName(serviceName);
        Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));

    }
}
