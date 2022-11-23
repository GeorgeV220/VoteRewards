package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.MinecraftUtils;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("vnpc")
public class NPCCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.npc.default}")
    @CommandPermission("voterewards.npc")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        if (!voteReward.getNoPlayerCharacterAPI().isHooked()) {
            MinecraftUtils.msg(sender, "&c&l(!) &cNPCs has not been hooked!");
            return;
        }
        MinecraftUtils.msg(sender, "&c&l(!)&c Commands &c&l(!)");
        MinecraftUtils.msg(sender, "&6/vnpc create");
        MinecraftUtils.msg(sender, "&6/vnpc remove");
        MinecraftUtils.msg(sender, "&6/vnpc update");
        MinecraftUtils.msg(sender, "&6/vnpc updateall");
        MinecraftUtils.msg(sender, "&c&l==============");
    }

    @HelpCommand
    @Subcommand("help")
    @CommandAlias("vnpchelp")
    @Description("{@@commands.descriptions.npc.help}")
    @Override
    public void onHelp(final CommandSender sender, @NotNull CommandHelp commandHelp, String @NotNull [] args) {
        MinecraftUtils.msg(sender, "&c&l(!)&c Commands &c&l(!)");
        MinecraftUtils.msg(sender, "&6/vnpc create");
        MinecraftUtils.msg(sender, "&6/vnpc remove");
        MinecraftUtils.msg(sender, "&6/vnpc update");
        MinecraftUtils.msg(sender, "&6/vnpc updateall");
        MinecraftUtils.msg(sender, "&c&l==============");
    }

    @Subcommand("create")
    @CommandAlias("vnpccreate")
    @CommandCompletion("<npcname> @range:0-999")
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

        if (voteReward.getNoPlayerCharacterAPI().npcExists(args[0])) {
            MinecraftUtils.msg(sender, "&c&l(!)&c NPC " + args[1] + " already exists!");
            return;
        }

        if (!args[1].matches("-?(0|[1-9]\\d*)")) {
            MinecraftUtils.msg(sender, "&c&l(!)&c The position must be a number (1 - +∞)");
            return;
        }

        voteReward.getNoPlayerCharacterAPI().create(args[0], Integer.parseInt(args[1]), new MinecraftUtils.SerializableLocation(player.getLocation()), true);

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

        if (voteReward.getNoPlayerCharacterAPI().npcExists(args[0])) {
            voteReward.getNoPlayerCharacterAPI().remove(args[0], true);
            MinecraftUtils.msg(sender, "&a&l(!)&a Successfully removed " + args[0] + " npc!");
        } else {
            MinecraftUtils.msg(sender, "&c&l(!)&c NPC " + args[0] + " does not exists!");
        }
    }

    @Subcommand("update")
    @CommandAlias("vnpcupdate")
    @CommandCompletion("<npcname>")
    @Description("{@@commands.descriptions.npc.update}")
    @Syntax("update <npcname> @range:0-999")
    public void update(CommandSender sender, String @NotNull [] args) {
        if (args.length == 0) {
            MinecraftUtils.msg(sender, "&c&l(!)&c Not enough arguments");
            return;
        }

        if (voteReward.getNoPlayerCharacterAPI().npcExists(args[0])) {
            voteReward.getNoPlayerCharacterAPI().updateNPC(args[0], new MinecraftUtils.SerializableLocation(voteReward.getNoPlayerCharacterAPI().getNPC(args[0]).getLocation()), args.length > 1 ? args[1].matches("-?(0|[1-9]\\d*)") ? Integer.parseInt(args[1]) : voteReward.getNoPlayerCharacterAPI().getNPCMap().get(args[0], ObjectMap.Pair.create(voteReward.getNoPlayerCharacterAPI().getNPC(args[0]), 1)).value() : voteReward.getNoPlayerCharacterAPI().getNPCMap().get(args[0], ObjectMap.Pair.create(voteReward.getNoPlayerCharacterAPI().getNPC(args[0]), 1)).value(), args.length > 1);
            MinecraftUtils.msg(sender, "&a&l(!)&a Successfully updated " + args[0] + " npc!");
        } else {
            MinecraftUtils.msg(sender, "&c&l(!)&c NPC " + args[0] + " does not exists!");
        }
    }

    @Subcommand("updateall")
    @CommandAlias("vnpcupdateall")
    @Description("{@@commands.descriptions.npc.updateall}")
    @Syntax("updateall")
    public void updateAll(CommandSender sender, String @NotNull [] args) {
        voteReward.getNoPlayerCharacterAPI().updateAll();
        MinecraftUtils.msg(sender, "&a&l(!)&a All NPCs have been updated!");
    }
}