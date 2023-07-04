package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventories.VoteTopInventory;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.jetbrains.annotations.NotNull;

@CommandAlias("votetop|vtop|vrtop|vvtop")
public class VoteTopCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.votetop}")
    @CommandPermission("voterewards.votetop")
    public void execute(@NotNull CommandIssuer commandIssuer, final String[] args) {
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
