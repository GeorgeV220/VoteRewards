package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("votes|vrvotes|vvotes")
public class VotesCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.votes}")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @CommandPermission("voterewards.votes")
    public void execute(@NotNull final CommandSender sender, final String @NotNull [] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MinecraftUtils.msg(sender, "/votes <player>");
                return;
            }
            UserVoteData userVoteData = UserVoteData.getUser((OfflinePlayer) sender);
            MessagesUtil.VOTES.msg(sender, userVoteData.user().placeholders(), true);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserVoteData userVoteData = UserVoteData.getUser(target);
        MessagesUtil.VOTES.msg(sender, userVoteData.user().placeholders(), true);
    }
}
