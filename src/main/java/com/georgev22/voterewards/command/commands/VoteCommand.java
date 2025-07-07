package com.georgev22.voterewards.command.commands;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.Permission;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventories.VoteInventory;
import org.jetbrains.annotations.NotNull;

@CommandAlias({"vote", "vrvote", "vvote"})
@Permission("voterewards.vote")
public class VoteCommand extends VoteRewardsBaseCommand {
    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        if (!commandIssuer.isPlayer()) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), MessagesUtil.ONLY_PLAYER_COMMAND.getMessages()[0]);
            return;
        }

        if (OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            new VoteInventory().openInventory(commandIssuer.getIssuer());
        }
        voteReward.getPlayerDataManager().getEntity(commandIssuer.getUniqueId()).thenAccept(userData -> {
            placeholders.append("%votes%", String.valueOf(userData.votes()));
            MessagesUtil.VOTE_COMMAND.msg(commandIssuer.getIssuer(), placeholders, true);

            if (OptionsUtil.CUMULATIVE.getBooleanValue() && OptionsUtil.CUMULATIVE_MESSAGE.getBooleanValue()) {
                placeholders.append("%votes%", String.valueOf(voteReward.getPlayerDataManager().votesUntilNextCumulativeVote(userData)));
                MessagesUtil.VOTE_COMMAND_CUMULATIVE.msg(commandIssuer.getIssuer(), placeholders, true);
            }
            placeholders.clear();
        });
    }
}
