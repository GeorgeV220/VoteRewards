package com.georgev22.voterewards.command.commands;

import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventories.VoteTopInventory;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.jetbrains.annotations.NotNull;

@CommandAlias({"votetop", "vtop", "vrtop", "vvtop"})
public class VoteTopCommand extends VoteRewardsBaseCommand {
    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        if (!commandIssuer.isPlayer()) {
            sendMsg(commandIssuer);
        } else {
            if (OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue() && OptionsUtil.VOTETOP_GUI.getBooleanValue()) {
                new VoteTopInventory().openTopPlayersInventory(commandIssuer.getIssuer(), !OptionsUtil.VOTETOP_GUI_TYPE.getStringValue().equalsIgnoreCase("monthly"));
            } else {
                sendMsg(commandIssuer);
            }
        }
    }

    private void sendMsg(CommandIssuer commandIssuer) {

        if (OptionsUtil.VOTETOP_HEADER.getBooleanValue())
            MessagesUtil.VOTE_TOP_HEADER.msg(commandIssuer.getIssuer());

        VoteUtils.getTopPlayers(OptionsUtil.VOTETOP_VOTERS.getIntValue()).forEach((key, value) -> {
            placeholders.append("%name%", key).append("%votes%", String.valueOf(value));

            MessagesUtil.VOTE_TOP_BODY.msg(commandIssuer.getIssuer(), placeholders, true);
        });

        if (OptionsUtil.VOTETOP_LINE.getBooleanValue())
            MessagesUtil.VOTE_TOP_LINE.msg(commandIssuer.getIssuer());

        if (OptionsUtil.VOTETOP_ALL_TIME_ENABLED.getBooleanValue())
            VoteUtils.getAllTimeTopPlayers(OptionsUtil.VOTETOP_ALL_TIME_VOTERS.getIntValue()).forEach((key, value) -> {
                placeholders.append("%name%", key).append("%votes%", String.valueOf(value));

                MessagesUtil.VOTE_TOP_BODY.msg(commandIssuer.getIssuer(), placeholders, true);
            });
        if (OptionsUtil.VOTETOP_FOOTER.getBooleanValue())
            MessagesUtil.VOTE_TOP_FOOTER.msg(commandIssuer.getIssuer());
        placeholders.clear();
    }
}
