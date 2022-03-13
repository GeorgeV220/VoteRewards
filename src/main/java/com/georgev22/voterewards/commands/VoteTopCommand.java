package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.inventories.VoteTopInventory;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("votetop|vtop|vrtop|vvtop")
public class VoteTopCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.votetop}")
    @CommandPermission("voterewards.votetop")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) {
            sendMsg(sender);
        } else {
            if (OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue() && OptionsUtil.VOTETOP_GUI.getBooleanValue()) {
                new VoteTopInventory().openTopPlayersInventory(player, !OptionsUtil.VOTETOP_GUI_TYPE.getStringValue().equalsIgnoreCase("monthly"));
            } else {
                sendMsg(player);
            }
        }
    }

    private void sendMsg(CommandSender sender) {

        if (OptionsUtil.VOTETOP_HEADER.getBooleanValue())
            MessagesUtil.VOTE_TOP_HEADER.msg(sender);

        VoteUtils.getTopPlayers(OptionsUtil.VOTETOP_VOTERS.getIntValue()).forEach((key, value) -> {
            placeholders.append("%name%", key).append("%votes%", String.valueOf(value));

            MessagesUtil.VOTE_TOP_BODY.msg(sender, placeholders, true);
        });

        if (OptionsUtil.VOTETOP_LINE.getBooleanValue())
            MessagesUtil.VOTE_TOP_LINE.msg(sender);

        if (OptionsUtil.VOTETOP_ALL_TIME_ENABLED.getBooleanValue())
            VoteUtils.getAllTimeTopPlayers(OptionsUtil.VOTETOP_ALL_TIME_VOTERS.getIntValue()).forEach((key, value) -> {
                placeholders.append("%name%", key).append("%votes%", String.valueOf(value));

                MessagesUtil.VOTE_TOP_BODY.msg(sender, placeholders, true);
            });
        if (OptionsUtil.VOTETOP_FOOTER.getBooleanValue())
            MessagesUtil.VOTE_TOP_FOOTER.msg(sender);
        placeholders.clear();
    }
}
