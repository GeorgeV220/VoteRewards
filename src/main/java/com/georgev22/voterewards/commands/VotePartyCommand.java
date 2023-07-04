package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.utilities.Utils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@CommandAlias("voteparty|vvp|vp|vrvp")
public class VotePartyCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.voteparty.basic}")
    @CommandCompletion("claim|start|give")
    @CommandPermission("voterewards.voteparty")
    public void execute(@NotNull final CommandIssuer commandIssuer, final String @NotNull [] args) {
        placeholders
                .append("%votes%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                        - fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                .append("%current%", String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()));
        MessagesUtil.VOTEPARTY.msg(commandIssuer.getIssuer(), placeholders, true);
        placeholders.clear();
    }

    @Subcommand("start")
    @CommandPermission("voterewards.voteparty.start")
    @Description("{@@commands.descriptions.voteparty.start}")
    @CommandAlias("vpstart")
    public void start() {
        VotePartyUtils.voteParty(null, true);
    }

    @Subcommand("give")
    @CommandPermission("voterewards.voteparty.give")
    @Description("{@@commands.descriptions.voteparty.give}")
    @CommandCompletion("@players @range:1000")
    @CommandAlias("vpgive")
    public void give(final @NotNull CommandIssuer commandIssuer, final String[] args) {
        if (!commandIssuer.isPlayer()) {
            if (args.length < 1) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &c/vp give <player>");
                return;
            }
            final Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessagesUtil.OFFLINE_PLAYER.msg(commandIssuer.getIssuer());
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
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cEnter a valid number");
                return;
            }
            return;
        }
        if (args.length == 0) {
            ((Player) commandIssuer.getIssuer()).getInventory().addItem(VotePartyUtils.crate(1));
            placeholders.append("%amount%", "1");
            MessagesUtil.VOTEPARTY_GIVE.msg(commandIssuer.getIssuer(), placeholders, true);
            placeholders.clear();
            return;
        }
        final Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            MessagesUtil.OFFLINE_PLAYER.msg(commandIssuer.getIssuer());
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
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cEnter a valid number");
            return;
        }
        ((Player) commandIssuer.getIssuer()).getInventory().addItem(VotePartyUtils.crate(Integer.parseInt(args[1])));
        placeholders.append("%amount%", args[1]);
        MessagesUtil.VOTEPARTY_GIVE.msg(target, placeholders, true);
        placeholders.clear();
    }

    @Subcommand("claim")
    @CommandPermission("voterewards.voteparty.claim")
    @Description("{@@commands.descriptions.voteparty.claim}")
    @CommandAlias("vpclaim")
    public void claim(final @NotNull CommandIssuer commandIssuer) {
        if (!commandIssuer.isPlayer()) {
            return;
        }
        Player player = commandIssuer.getIssuer();
        voteReward.getPlayerDataManager().getEntity(commandIssuer.getUniqueId()).handle((userData, throwable) -> {
            if (throwable != null) {
                voteReward.getLogger().log(Level.SEVERE, "Error while trying to claim voteparty crates (" + player.getName() + ":", throwable);
                return null;
            }
            return userData;
        }).thenAccept(userData -> {
            if (userData != null) {
                if (userData.voteparty() > 0) {
                    player.getInventory()
                            .addItem(VotePartyUtils.crate(userData.voteparty()));
                    placeholders.append("%crates%", String.valueOf(userData.voteparty()));
                    userData.voteparty(0);
                    MessagesUtil.VOTEPARTY_CLAIM.msg(player, placeholders, true);
                    placeholders.clear();
                    voteReward.getPlayerDataManager().save(userData);
                } else {
                    MessagesUtil.VOTEPARTY_NOTHING_TO_CLAIM.msg(player);
                }
            }
        });
    }
}
