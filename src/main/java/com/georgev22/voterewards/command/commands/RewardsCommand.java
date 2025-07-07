package com.georgev22.voterewards.command.commands;

import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.Permission;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.jetbrains.annotations.NotNull;

@CommandAlias({"rewards", "vrewards", "vrew"})
@Permission("voterewards.rewards")
public class RewardsCommand extends VoteRewardsBaseCommand {

    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        MessagesUtil.REWARDS.msg(commandIssuer.getIssuer());
    }
}
