package com.georgev22.voterewards.command.commands;

import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.CommandCompletion;
import com.georgev22.voterewards.command.annotation.Permission;
import com.georgev22.voterewards.command.annotation.Subcommand;
import com.georgev22.voterewards.utilities.MessagesUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("vnpc")
@Permission("voterewards.npc")
public class NPCCommand extends VoteRewardsBaseCommand {

    public NPCCommand() {
        addSubcommand(new CreateSubCommand());
        addSubcommand(new RemoveSubCommand());
        addSubcommand(new UpdateSubCommand());
        addSubcommand(new UpdateAllSubCommand());
    }

    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        if (!voteReward.getNoPlayerCharacterAPI().isHooked()) {
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cNPCs has not been hooked!");
            return;
        }
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Commands &c&l(!)");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vnpc create");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vnpc remove");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vnpc update");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vnpc updateall");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l==============");
    }

    @Subcommand("create")
    @Permission("voterewards.npc.create")
    @CommandCompletion("<npcname> @range:0-999")
    public static class CreateSubCommand extends VoteRewardsBaseCommand {

        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (!voteReward.getNoPlayerCharacterAPI().isHooked()) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cNPCs has not been hooked!");
                return;
            }
            if (args.length <= 1) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Not enough arguments");
                return;
            }

            if (!commandIssuer.isPlayer()) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(commandIssuer.getIssuer());
                return;
            }

            Player player = commandIssuer.getIssuer();

            if (voteReward.getNoPlayerCharacterAPI().npcExists(args[0])) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c NPC " + args[1] + " already exists!");
                return;
            }

            if (!args[1].matches("-?(0|[1-9]\\d*)")) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c The position must be a number (1 - +∞)");
                return;
            }

            voteReward.getNoPlayerCharacterAPI().create(args[0], Integer.parseInt(args[1]), new BukkitMinecraftUtils.SerializableLocation(player.getLocation()), true);

            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a Successfully created " + args[0] + " npc with position " + args[1] + "!");
        }
    }

    @Subcommand("remove")
    @Permission("voterewards.npc.remove")
    @CommandCompletion("<npcname>")
    public static class RemoveSubCommand extends VoteRewardsBaseCommand {

        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (!voteReward.getNoPlayerCharacterAPI().isHooked()) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cNPCs has not been hooked!");
                return;
            }
            if (args.length == 0) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Not enough arguments");
                return;
            }

            if (voteReward.getNoPlayerCharacterAPI().npcExists(args[0])) {
                voteReward.getNoPlayerCharacterAPI().remove(args[0], true);
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a Successfully removed " + args[0] + " npc!");
            } else {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c NPC " + args[0] + " does not exists!");
            }
        }
    }

    @Subcommand("update")
    @Permission("voterewards.npc.update")
    @CommandCompletion("<npcname>")
    public static class UpdateSubCommand extends VoteRewardsBaseCommand {

        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (!voteReward.getNoPlayerCharacterAPI().isHooked()) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cNPCs has not been hooked!");
                return;
            }
            if (args.length == 0) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Not enough arguments");
                return;
            }

            if (voteReward.getNoPlayerCharacterAPI().npcExists(args[0])) {
                voteReward.getNoPlayerCharacterAPI().updateNPC(args[0], new BukkitMinecraftUtils.SerializableLocation(voteReward.getNoPlayerCharacterAPI().getNPC(args[0]).getLocation()), args.length > 1 ? args[1].matches("-?(0|[1-9]\\d*)") ? Integer.parseInt(args[1]) : voteReward.getNoPlayerCharacterAPI().getNPCMap().get(args[0], ObjectMap.Pair.create(voteReward.getNoPlayerCharacterAPI().getNPC(args[0]), 1)).value() : voteReward.getNoPlayerCharacterAPI().getNPCMap().get(args[0], ObjectMap.Pair.create(voteReward.getNoPlayerCharacterAPI().getNPC(args[0]), 1)).value(), args.length > 1);
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a Successfully updated " + args[0] + " npc!");
            } else {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c NPC " + args[0] + " does not exists!");
            }
        }
    }

    @Subcommand("updateall")
    @Permission("voterewards.npc.updateall")
    @CommandCompletion("<npcname>")
    public static class UpdateAllSubCommand extends VoteRewardsBaseCommand {

        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (!voteReward.getNoPlayerCharacterAPI().isHooked()) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!) &cNPCs has not been hooked!");
                return;
            }
            voteReward.getNoPlayerCharacterAPI().updateAll();
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!)&a All NPCs have been updated!");
        }
    }
}
