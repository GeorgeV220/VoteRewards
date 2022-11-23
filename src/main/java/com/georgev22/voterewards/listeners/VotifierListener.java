package com.georgev22.voterewards.listeners;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.MinecraftUtils;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;

/*
 *
 * This class handles votes going through the server.
 *
 */
public class VotifierListener implements Listener {

    private static VoteReward voteReward = VoteReward.getInstance();

    @EventHandler
    public void onVote(VotifierEvent e) throws IOException {
        final Vote vote = e.getVote();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(vote.getUsername());
        if (OptionsUtil.DEBUG_VOTE_PRE.getBooleanValue())
            MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Pre process of vote: " + vote);

        UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
        if (!offlinePlayer.isOnline()) {

            if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
                MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Player " + offlinePlayer.getName() + " is offline!");
            if (OptionsUtil.OFFLINE.getBooleanValue()) {
                try {
                    if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
                        MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Process " + vote.getUsername() + " vote with " + vote.getServiceName() + " service name!");
                    new VoteUtils(userVoteData.user()).processOfflineVote(vote.getServiceName());
                } catch (Exception ioException) {
                    ioException.printStackTrace();
                }
            }
            return;
        }

        new VoteUtils(userVoteData.user()).processVote(vote.getServiceName());
        ObjectMap<String, String> placeholders = new HashObjectMap<>();
        placeholders.append("%player%", vote.getUsername()).append("%servicename%", vote.getServiceName());
        if (OptionsUtil.MESSAGE_VOTE.getBooleanValue())
            MessagesUtil.VOTE.msgAll(placeholders, true);

        placeholders.clear();
    }

}
