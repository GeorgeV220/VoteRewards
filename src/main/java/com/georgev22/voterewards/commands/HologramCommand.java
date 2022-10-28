package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author GeorgeV22
 */
@CommandAlias("hologram|vrhologram|vrh")
public class HologramCommand extends Command {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    @Default
    @Description("{@@commands.descriptions.hologram}")
    @CommandCompletion("create|remove|update")
    @Syntax("<create|remove|update> <hologram>")
    @CommandPermission("voterewards.hologram")
    public void execute(@NotNull CommandSender sender, String[] args) {
        if (!voteRewardPlugin.getHolograms().isHooked()) {
            MinecraftUtils.msg(sender, "&c&l(!) &cHolograms has not been hooked!");
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

            if (voteRewardPlugin.getHolograms().hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram already exists!");
                return;
            }

            if (voteRewardPlugin.getConfig().get("Holograms." + args[2]) == null) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram type doesn't exists!");
                return;
            }

            voteRewardPlugin.getHolograms().show(
                    voteRewardPlugin.getHolograms().updateHologram(
                            voteRewardPlugin.getHolograms().create(
                                    args[1],
                                    player.getLocation(),
                                    args[2],
                                    true
                            ),
                            voteRewardPlugin.getConfig().getStringList("Holograms." + args[2]),
                            voteRewardPlugin.getHolograms().getPlaceholderMap()),
                    player);

            voteRewardPlugin.getHolograms().getPlaceholderMap().clear();

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " with type " + args[2] + " successfully created!");

        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                MinecraftUtils.msg(player, "&c&l(!) &cUsage: /hologram remove <hologramName>");
                return;
            }

            if (!voteRewardPlugin.getHolograms().hologramExists(args[1])) {
                MinecraftUtils.msg(sender, "&c&l(!) &cHologram doesn't exists!");
                return;
            }

            voteRewardPlugin.getHolograms().remove(args[1], true);

            MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully removed!");
        } else if (args[0].equalsIgnoreCase("update")) {
            if (args.length < 3) {
                return;
            }
            if (voteRewardPlugin.getHolograms().hologramExists(args[1])) {
                Object hologram = voteRewardPlugin.getHolograms().getHologramMap().get(args[1]);
                voteRewardPlugin.getHolograms().updateHologram(hologram, voteRewardPlugin.getConfig().getStringList("Holograms." + args[2]), voteRewardPlugin.getHolograms().getPlaceholderMap());
                voteRewardPlugin.getHolograms().getPlaceholderMap().clear();
                voteRewardPlugin.getHolograms().hide(hologram, player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(voteRewardPlugin, () -> voteRewardPlugin.getHolograms().show(hologram, player), 20);
                MinecraftUtils.msg(player, args[1] + " " + args[2]);
                MinecraftUtils.msg(sender, "&a&l(!) &aHologram " + args[1] + " successfully updated!");
            } else {
                MinecraftUtils.msg(player, "&c&l(!) &cHologram doesn't exists!");
            }
        }
    }
}
