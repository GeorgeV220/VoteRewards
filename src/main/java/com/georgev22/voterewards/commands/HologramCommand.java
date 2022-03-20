package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.hooks.HologramAPI;
import com.github.unldenis.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author GeorgeV22
 */
@CommandAlias("hologram|vrhologram|vrh")
public class HologramCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.hologram}")
    @CommandCompletion("create|remove|update")
    @Syntax("<create|remove|update> <hologram>")
    @CommandPermission("voterewards.hologram")
    public void execute(@NotNull CommandSender sender, String[] args) {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            MinecraftUtils.msg(sender, "&c&l(!) &cProtocolLib is not enabled!");
            return;
        }

        if (args.length == 0) {
            MinecraftUtils.msg(sender, "&c&l(!) &cNot enough arguments!");
            return;
        }

        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                MinecraftUtils.msg(player, "&c&l(!) &cUsage: /hologram create <hologramName> <type>");
                return;
            }

            if (HologramAPI.hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram already exists!");
                return;
            }

            if (voteRewardPlugin.getConfig().get("Holograms." + args[2]) == null) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
                return;
            }

            HologramAPI.show(HologramAPI.updateHologram(HologramAPI.create(
                                    args[1],
                                    player.getLocation(),
                                    args[2], true),
                            voteRewardPlugin.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HologramAPI.getPlaceholderMap()),
                    player);

            HologramAPI.getPlaceholderMap().clear();

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " with type " + args[2] + " successfully created!");

        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                MinecraftUtils.msg(player, "&c&l(!) &cUsage: /hologram remove <hologramName>");
                return;
            }

            if (!HologramAPI.hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
                return;
            }

            HologramAPI.remove(args[1], true);

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully removed!");
        } else if (args[0].equalsIgnoreCase("update")) {
            if (args.length < 2) {
                return;
            }
            if (HologramAPI.hologramExists(args[1])) {
                Hologram hologram = HologramAPI.getHologramMap().get(args[1]);
                HologramAPI.updateHologram(hologram, voteRewardPlugin.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HologramAPI.getPlaceholderMap());
                HologramAPI.getPlaceholderMap().clear();
                HologramAPI.hide(hologram, player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(voteRewardPlugin, () -> HologramAPI.show(hologram, player), 20);
                MinecraftUtils.msg(player, args[1] + " " + args[2]);
                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully updated!");
            } else {
                MinecraftUtils.msg(player, "&c&l(!) &cHologram doesn't exists!");
            }
        }
    }
}
