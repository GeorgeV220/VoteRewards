package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author GeorgeV22
 */
@CommandAlias("vhologram|vrhologram|vrh")
public class HologramCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.hologram.default}")
    @Syntax("<create|remove|update> <hologram>")
    @CommandPermission("voterewards.hologram")
    public void execute(@NotNull CommandSender sender, String @NotNull [] args) {
        if (!voteReward.getHolograms().isHooked()) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!) &cHolograms has not been hooked!");
            return;
        }
        BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Commands &c&l(!)");
        BukkitMinecraftUtils.msg(sender, "&6/vhologram create");
        BukkitMinecraftUtils.msg(sender, "&6/vhologram remove");
        BukkitMinecraftUtils.msg(sender, "&6/vhologram update");
        BukkitMinecraftUtils.msg(sender, "&c&l==============");
    }

    @HelpCommand
    @Subcommand("help")
    @CommandAlias("vhologramhelp|vrhhelp")
    @Description("{@@commands.descriptions.hologram.help}")
    @Override
    public void onHelp(final CommandSender sender, @NotNull CommandHelp commandHelp, String @NotNull [] args) {
        BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Commands &c&l(!)");
        BukkitMinecraftUtils.msg(sender, "&6/vhologram create");
        BukkitMinecraftUtils.msg(sender, "&6/vhologram remove");
        BukkitMinecraftUtils.msg(sender, "&6/vhologram update");
        BukkitMinecraftUtils.msg(sender, "&c&l==============");
    }

    @Subcommand("create")
    @CommandAlias("vhologramcreate|vrhcreate")
    @Description("{@@commands.descriptions.hologram.create}")
    @CommandPermission("voterewards.hologram.create")
    public void create(@NotNull CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
            return;
        }
        if (args.length < 2) {
            BukkitMinecraftUtils.msg(player, "&c&l(!) &cUsage: /hologram create <hologramName> <type>");
            return;
        }

        if (voteReward.getHolograms().hologramExists(args[0])) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!) &cHologram already exists!");
            return;
        }

        if (voteReward.getConfig().get("Holograms." + args[1]) == null) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
            return;
        }

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

        BukkitMinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[0] + " with type " + args[1] + " successfully created!");

    }

    @Subcommand("remove")
    @CommandAlias("vhologramremove|vrhremove")
    @Description("{@@commands.descriptions.hologram.remove}")
    @CommandPermission("voterewards.hologram.remove")
    public void remove(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length == 0) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!) &cUsage: /hologram remove <hologramName>");
            return;
        }

        if (!voteReward.getHolograms().hologramExists(args[0])) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
            return;
        }

        voteReward.getHolograms().remove(args[0], true);

        BukkitMinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[0] + " successfully removed!");
    }

    @Subcommand("update")
    @CommandAlias("vhologramupdate|vrhupdate")
    @Description("{@@commands.descriptions.hologram.update}")
    @CommandPermission("voterewards.hologram.update")
    public void update(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length < 2) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!) &c/vrh update <hologram> <config lines name>");
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

            BukkitMinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[0] + " successfully updated!");
        } else {
            BukkitMinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
        }
    }
}
