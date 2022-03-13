package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.hooks.HolographicDisplays;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author GeorgeV22
 */
@CommandAlias("hologram|vrhologram|vrh")
public class Holograms extends Command {

    @Default
    @Description("{@@commands.descriptions.hologram}")
    @CommandCompletion("@players")
    @Syntax("<create|remove|update> <hologram>")
    @CommandPermission("voterewards.hologram")
    public void execute(@NotNull CommandSender sender, String[] args) {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
            MinecraftUtils.msg(sender, "&c&l(!) &cHolographicDisplays is not enabled!");
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

            if (HolographicDisplays.hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram already exists!");
                return;
            }

            if (voteRewardPlugin.getConfig().get("Holograms." + args[2]) == null) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
                return;
            }

            HolographicDisplays.show(HolographicDisplays.updateHologram(HolographicDisplays.create(
                                    args[1],
                                    player.getLocation(),
                                    args[2], true),
                            voteRewardPlugin.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HolographicDisplays.getPlaceholderMap()),
                    player);

            HolographicDisplays.getPlaceholderMap().clear();

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " with type " + args[2] + " successfully created!");

        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                MinecraftUtils.msg(player, "&c&l(!) &cUsage: /hologram remove <hologramName>");
                return;
            }

            if (!HolographicDisplays.hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
                return;
            }

            HolographicDisplays.remove(args[1], true);

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully removed!");
        } else if (args[0].equalsIgnoreCase("update")) {
            if (HolographicDisplays.hologramExists(args[1])) {
                Hologram hologram = HolographicDisplays.getHologramMap().get(args[1]);
                HolographicDisplays.updateHologram(hologram, voteRewardPlugin.getConfig().getStringList("Holograms." + args[2]).toArray(new String[0]), HolographicDisplays.getPlaceholderMap());
                HolographicDisplays.getPlaceholderMap().clear();
                HolographicDisplays.hide(hologram, player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(voteRewardPlugin, () -> HolographicDisplays.show(hologram, player), 20);
                MinecraftUtils.msg(player, args[1] + " " + args[2]);
                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully updated!");
            } else {
                MinecraftUtils.msg(player, "&c&l(!) &cHologram doesn't exists!");
            }
        }
    }
}
