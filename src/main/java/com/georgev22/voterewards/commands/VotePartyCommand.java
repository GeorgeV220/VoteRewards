package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.api.utilities.Utils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("voteparty|vvp|vp|vrvp")
public class VotePartyCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.voteparty.basic}")
    @CommandCompletion("claim|start|give")
    @CommandPermission("voterewards.voteparty")
    public void execute(@NotNull final CommandSender sender, final String @NotNull [] args) {
        placeholders
                .append("%votes%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                        - fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                .append("%current%", String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()));
        MessagesUtil.VOTEPARTY.msg(sender, placeholders, true);
        placeholders.clear();
    }

    @Subcommand("start")
    @CommandPermission("voterewards.voteparty.start")
    @Description("{@@commands.descriptions.voteparty.start}")
    @CommandAlias("vpstart")
    public void start() {
        new VotePartyUtils(null).run(true);
    }

    @Subcommand("give")
    @CommandPermission("voterewards.voteparty.give")
    @Description("{@@commands.descriptions.voteparty.give}")
    @CommandCompletion("@players @range:1000")
    @CommandAlias("vpgive")
    public void give(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length < 1) {
                MinecraftUtils.msg(sender, "&c&l(!) &c/vp give <player>");
                return;
            }
            final Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessagesUtil.OFFLINE_PLAYER.msg(sender);
                return;
            }
            if (args.length == 1) {
                target.getInventory().addItem(VotePartyUtils.crate(1));
                placeholders.append("%amount%", "1");
                MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
                placeholders.clear();
                return;
            }
            if (!Utils.isInt(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cEnter a valid number");
                return;
            }
            return;
        }
        if (args.length == 0) {
            ((Player) sender).getInventory().addItem(VotePartyUtils.crate(1));
            placeholders.append("%amount%", "1");
            MessagesUtil.VOTEPARTY_GIVE.msg(sender, placeholders, true);
            placeholders.clear();
            return;
        }
        final Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            MessagesUtil.OFFLINE_PLAYER.msg(sender);
            return;
        }
        if (args.length == 1) {
            target.getInventory().addItem(VotePartyUtils.crate(1));
            placeholders.append("%amount%", "1");
            MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
            placeholders.clear();
            return;
        }
        if (!Utils.isInt(args[1])) {
            MinecraftUtils.msg(sender, "&c&l(!) &cEnter a valid number");
            return;
        }
        ((Player) sender).getInventory().addItem(VotePartyUtils.crate(Integer.parseInt(args[1])));
        placeholders.append("%amount%", args[1]);
        MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
        placeholders.clear();
    }

    @Subcommand("claim")
    @CommandPermission("voterewards.voteparty.claim")
    @Description("{@@commands.descriptions.voteparty.claim}")
    @CommandAlias("vpclaim")
    public void claim(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return;
        }
        final UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
        if (userVoteData.getVoteParty() > 0) {
            player.getInventory()
                    .addItem(VotePartyUtils.crate(userVoteData.getVoteParty()));
            placeholders.append("%crates%", String.valueOf(userVoteData.getVoteParty()));
            userVoteData.setVoteParties(0);
            MessagesUtil.VOTEPARTY_CLAIM.msg(player, placeholders, true);
            placeholders.clear();
        } else {
            MessagesUtil.VOTEPARTY_NOTHING_TO_CLAIM.msg(player);
        }
    }
}
