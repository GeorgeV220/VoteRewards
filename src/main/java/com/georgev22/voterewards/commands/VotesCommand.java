package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.player.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

@CommandAlias("votes|vrvotes|vvotes")
public class VotesCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.votes}")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @CommandPermission("voterewards.votes")
    public void execute(@NotNull CommandIssuer commandIssuer, final String @NotNull [] args) {
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
