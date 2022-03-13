package com.georgev22.voterewards.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.MessageType;
import co.aikar.commands.annotation.*;
import co.aikar.locales.MessageKey;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("fakevote|vrfake|vfake")
public class FakeVoteCommand extends Command {

    static MessageKey key(String key) {
        return MessageKey.of("commands." + key);
    }

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
        return;
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
