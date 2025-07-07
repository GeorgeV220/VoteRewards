package com.georgev22.voterewards.command.commands;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.CommandCompletion;
import com.georgev22.voterewards.command.annotation.Permission;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.player.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

@CommandAlias({"votes", "vrvotes", "vvotes"})
@CommandCompletion("@players")
@Permission("voterewards.votes")
public class VotesCommand extends VoteRewardsBaseCommand {
    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        if (args.length == 0) {
            if (!commandIssuer.isPlayer()) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "/votes <player>");
                return;
            }
            voteReward.getPlayerDataManager().getEntity(commandIssuer.getUniqueId()).thenAccept(userData ->
                    MessagesUtil.VOTES.msg(commandIssuer.getIssuer(), User.placeholders(userData), true));

            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        voteReward.getPlayerDataManager().exists(target.getUniqueId()).thenAccept(aBoolean -> {
            if (aBoolean) {
                voteReward.getPlayerDataManager().getEntity(target.getUniqueId()).thenAccept(userData ->
                        MessagesUtil.VOTES.msg(commandIssuer.getIssuer(), User.placeholders(userData), true));
            } else {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Player " + target.getName() + " doesn't exist");
            }
        });
    }
}
