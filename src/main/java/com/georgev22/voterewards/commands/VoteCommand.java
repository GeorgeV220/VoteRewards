package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventories.VoteInventory;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("vote|vrvote|vvote")
public class VoteCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.vote}")
    @Syntax("")
    @CommandPermission("voterewards.vote")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) {
            BukkitMinecraftUtils.msg(sender, MessagesUtil.ONLY_PLAYER_COMMAND.getMessages()[0]);
            return;
        }

        if (OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            new VoteInventory().openInventory(((Player) sender));
        }
        UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
        placeholders.append("%votes%", String.valueOf(userVoteData.getVotes()));
        MessagesUtil.VOTE_COMMAND.msg(player, placeholders, true);

        if (OptionsUtil.CUMULATIVE.getBooleanValue() && OptionsUtil.CUMULATIVE_MESSAGE.getBooleanValue()) {
            placeholders.append("%votes%", String.valueOf(userVoteData.votesUntilNextCumulativeVote()));
            MessagesUtil.VOTE_COMMAND_CUMULATIVE.msg(player, placeholders, true);
        }
        placeholders.clear();
    }
}