package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("rewards|vrewards|vrew")
public class RewardsCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.rewards}")
    @CommandPermission("voterewards.rewards")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        MessagesUtil.REWARDS.msg(sender);
    }
}
