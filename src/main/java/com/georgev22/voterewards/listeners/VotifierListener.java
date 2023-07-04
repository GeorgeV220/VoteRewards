package com.georgev22.voterewards.listeners;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.util.logging.Level;

/*
 *
 * This class handles votes going through the server.
 *
 */
public class VotifierListener implements Listener {

    private final VoteReward voteReward = VoteReward.getInstance();

    @EventHandler
    public void onVote(VotifierEvent e) {
        final Vote vote = e.getVote();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(vote.getUsername());
        if (OptionsUtil.DEBUG_VOTE_PRE.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Pre process of vote: " + vote);
        voteReward.getPlayerDataManager().exists(offlinePlayer.getUniqueId()).thenAccept(aBoolean -> {
            if (aBoolean) {
                voteReward.getPlayerDataManager().getEntity(offlinePlayer.getUniqueId()).handle((user, throwable) -> {
                    if (throwable != null) {
                        voteReward.getLogger().log(Level.SEVERE, "Error while trying to process " + vote.getUsername() + " vote", throwable);
                        return null;
                    }
                    return user;
                }).thenAccept(user -> {
                    if (user != null) {
                        if (!offlinePlayer.isOnline()) {

                            if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
                                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Player " + offlinePlayer.getName() + " is offline!");
                            if (OptionsUtil.OFFLINE.getBooleanValue()) {
                                try {
                                    if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
                                        BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Process " + vote.getUsername() + " vote with " + vote.getServiceName() + " service name!");
                                    new VoteUtils(user).processOfflineVote(vote.getServiceName());
                                } catch (Exception ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                            return;
                        }

                        try {
                            new VoteUtils(user).processVote(vote.getServiceName());
                        } catch (IOException ex) {
                            voteReward.getLogger().log(Level.SEVERE, "Error while trying to process " + vote.getUsername() + " vote", ex);
                            return;
                        }
                        ObjectMap<String, String> placeholders = new HashObjectMap<>();
                        placeholders.append("%player%", vote.getUsername()).append("%servicename%", vote.getServiceName());
                        if (OptionsUtil.MESSAGE_VOTE.getBooleanValue())
                            MessagesUtil.VOTE.msgAll(placeholders, true);

                        placeholders.clear();
                    }
                });
            }
        });
    }

}
