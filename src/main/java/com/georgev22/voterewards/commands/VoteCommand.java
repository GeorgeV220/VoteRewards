package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventories.VoteInventory;
import org.jetbrains.annotations.NotNull;

@CommandAlias("vote|vrvote|vvote")
public class VoteCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.vote}")
    @Syntax("")
    @CommandPermission("voterewards.vote")
    public void execute(@NotNull final CommandIssuer commandIssuer, final String[] args) {
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