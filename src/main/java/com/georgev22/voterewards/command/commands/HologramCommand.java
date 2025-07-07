package com.georgev22.voterewards.command.commands;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.Permission;
import com.georgev22.voterewards.command.annotation.Subcommand;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias({"vhologram", "vrhologram", "vrh"})
@Permission("voterewards.hologram")
public class HologramCommand extends VoteRewardsBaseCommand {

    public HologramCommand() {
        addSubcommand(new CreateSubCommand());
        addSubcommand(new RemoveSubCommand());
        addSubcommand(new UpdateSubCommand());
    }

    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        if (!voteReward.getHolograms().isHooked()) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cHolograms has not been hooked!");
            return;
        }
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Commands &c&l(!)");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vhologram create");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vhologram remove");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vhologram update");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l==============");
    }

    @Subcommand("create")
    @Permission("voterewards.hologram.create")
    public static class CreateSubCommand extends VoteRewardsBaseCommand {
        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (!commandIssuer.isPlayer()) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(commandIssuer.getIssuer());
                return;
            }
            if (args.length < 2) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cUsage: /hologram create <hologramName> <type>");
                return;
            }

            if (voteReward.getHolograms().hologramExists(args[0])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cHologram already exists!");
                return;
            }

            if (voteReward.getConfig().get("Holograms." + args[1]) == null) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cHologram type doesn't exists!");
                return;
            }

            Player player = commandIssuer.getIssuer();

            voteReward.getHolograms().show(
                    voteReward.getHolograms().updateHologram(
                            voteReward.getHolograms().create(
                                    args[0],
                                    new BukkitMinecraftUtils.SerializableLocation(player.getLocation()),
                                    args[1],
                                    true
                            ),
                            voteReward.getConfig().getStringList("Holograms." + args[1]),
                            voteReward.getHolograms().getPlaceholderMap()),
                    player);

            voteReward.getHolograms().getPlaceholderMap().clear();

            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aHologram " + args[0] + " with type " + args[1] + " successfully created!");
        }
    }

    @Subcommand("remove")
    @Permission("voterewards.hologram.remove")
    public static class RemoveSubCommand extends VoteRewardsBaseCommand {
        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (args.length == 0) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cUsage: /hologram remove <hologramName>");
                return;
            }

            if (!voteReward.getHolograms().hologramExists(args[0])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cHologram doesn't exists!");
                return;
            }

            voteReward.getHolograms().remove(args[0], true);

            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aHologram " + args[0] + " successfully removed!");
        }
    }

    @Subcommand("update")
    @Permission("voterewards.hologram.update")
    public static class UpdateSubCommand extends VoteRewardsBaseCommand {
        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (args.length < 2) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &c/vrh update <hologram> <config lines name>");
                return;
            }
            if (voteReward.getHolograms().hologramExists(args[0])) {
                Object hologram = voteReward.getHolograms().getHologramMap().get(args[0]);
                voteReward.getHolograms().updateHologram(hologram, voteReward.getConfig().getStringList("Holograms." + args[1]), voteReward.getHolograms().getPlaceholderMap());
                voteReward.getHolograms().getPlaceholderMap().clear();
                Bukkit.getOnlinePlayers().forEach(player -> {
                    voteReward.getHolograms().hide(hologram, player);
                    SchedulerManager.getScheduler().runTaskLaterAsynchronously(voteReward.getClass(), () -> voteReward.getHolograms().show(hologram, player), 20);
                });

                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aHologram " + args[0] + " successfully updated!");
            } else {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cHologram doesn't exists!");
            }
        }
    }
}
