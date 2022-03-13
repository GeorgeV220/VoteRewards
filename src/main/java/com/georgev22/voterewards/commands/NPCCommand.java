package com.georgev22.voterewards.commands;

import co.aikar.commands.annotation.*;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.profile.Profile;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;

@CommandAlias("vnpc")
public class NPCCommand extends Command {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private final NPCPool npcPool = NPCPool.builder(voteRewardPlugin)
            .spawnDistance(60)
            .actionDistance(30)
            .tabListRemoveTicks(20)
            .build();

    @Default
    @Description("{@@commands.descriptions.npc}")
    @CommandPermission("voterewards.npc")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        if (args.length == 0) {
            return;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 1) {
                return;
            }
            if (!(sender instanceof Player player)) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                return;
            }
            Profile profile = new Profile(VoteUtils.getTopPlayer(Integer.parseInt(args[1])));
            profile.complete();

            profile.setUniqueId(new UUID(new Random().nextLong(), 0));

            NPC.builder()
                    .profile(profile)
                    .location(player.getLocation())
                    .imitatePlayer(false)
                    .lookAtPlayer(true)
                    .build(npcPool);

            MinecraftUtils.msg(sender, "&a&l(!)&a Successfully created npc!");
        }
    }
}