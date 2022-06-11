package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.hooks.NPCAPI;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("vnpc")
public class NPCCommand extends Command {

    @HelpCommand
    @Subcommand("help")
    @CommandAlias("vnpchelp")
    @Description("{@@commands.descriptions.npc.help}")
    @Override
    public void onHelp(final CommandSender sender, @NotNull CommandHelp commandHelp, String @NotNull [] args) {
        MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
        MinecraftUtils.msg(sender, "&6/vnpc create");
        MinecraftUtils.msg(sender, "&6/vnpc remove");
        MinecraftUtils.msg(sender, "&6/vnpc update");
        MinecraftUtils.msg(sender, "&6/vnpc updateall");
        MinecraftUtils.msg(sender, "&c&l==============");
    }

    @Default
    @Description("{@@commands.descriptions.npc}")
    @CommandPermission("voterewards.npc")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
        MinecraftUtils.msg(sender, "&6/vnpc create");
        MinecraftUtils.msg(sender, "&6/vnpc remove");
        MinecraftUtils.msg(sender, "&6/vnpc update");
        MinecraftUtils.msg(sender, "&6/vnpc updateall");
        MinecraftUtils.msg(sender, "&c&l==============");
    }

    @Subcommand("create")
    @CommandAlias("vnpccreate")
    @CommandCompletion("<npcname> @range:1-999")
    @Description("{@@commands.descriptions.npc.create}")
    @Syntax("create <npcname> <position>")
    public void create(CommandSender sender, String @NotNull [] args) {
        if (args.length <= 1) {
            MinecraftUtils.msg(sender, "&c&l(!)&c Not enough arguments");
            return;
        }

        if (!(sender instanceof Player player)) {
            MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
            return;
        }

        if (NPCAPI.npcExists(args[0])) {
            MinecraftUtils.msg(sender, "&c&l(!)&c NPC " + args[1] + " already exists!");
            return;
        }

        if (!args[1].matches("-?(0|[1-9]\\d*)")) {
            MinecraftUtils.msg(sender, "&c&l(!)&c The position must be a number (1 - +∞)");
            return;
        }

        NPCAPI.create(args[0], Integer.parseInt(args[1]), player.getLocation(), true);

        MinecraftUtils.msg(sender, "&a&l(!)&a Successfully created " + args[0] + " npc with position " + args[1] + "!");
    }

    @Subcommand("remove")
    @CommandAlias("vnpcremove")
    @CommandCompletion("<npcname>")
    @Description("{@@commands.descriptions.npc.remove}")
    @Syntax("remove <npcname>")
    public void remove(CommandSender sender, String @NotNull [] args) {
        if (args.length == 0) {
            MinecraftUtils.msg(sender, "&c&l(!)&c Not enough arguments");
            return;
        }

        if (NPCAPI.npcExists(args[0])) {
            NPCAPI.remove(args[0], true);
            MinecraftUtils.msg(sender, "&a&l(!)&a Successfully removed " + args[0] + " npc!");
        } else {
            MinecraftUtils.msg(sender, "&c&l(!)&c NPC " + args[0] + " does not exists!");
        }
    }

    @Subcommand("update")
    @CommandAlias("vnpcupdate")
    @CommandCompletion("<npcname>")
    @Description("{@@commands.descriptions.npc.update}")
    @Syntax("update <npcname> @range:1-999")
    public void update(CommandSender sender, String @NotNull [] args) {
        if (args.length == 0) {
            MinecraftUtils.msg(sender, "&c&l(!)&c Not enough arguments");
            return;
        }

        if (NPCAPI.npcExists(args[0])) {
            NPCAPI.updateNPC(args[0], NPCAPI.getNPC(args[0]).getLocation(), args.length > 1 ? args[1].matches("-?(0|[1-9]\\d*)") ? Integer.parseInt(args[1]) : NPCAPI.getNPCMap().get(args[0], ObjectMap.Pair.create(NPCAPI.getNPC(args[0]), 1)).value() : NPCAPI.getNPCMap().get(args[0], ObjectMap.Pair.create(NPCAPI.getNPC(args[0]), 1)).value(), args.length > 1);
            MinecraftUtils.msg(sender, "&a&l(!)&a Successfully updated " + args[0] + " npc!");
        } else {
            MinecraftUtils.msg(sender, "&c&l(!)&c NPC " + args[0] + " does not exists!");
        }
    }

    @Subcommand("updateall")
    @CommandAlias("vnpcupdateall")
    @CommandCompletion("<npcname>")
    @Description("{@@commands.descriptions.npc.updateall}")
    @Syntax("update <npcname> @range:1-999")
    public void updateAll(CommandSender sender, String @NotNull [] args) {
        NPCAPI.updateAll();
        MinecraftUtils.msg(sender, "&a&l(!)&a All NPCs have been updated!");
    }
}