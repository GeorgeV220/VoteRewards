package com.georgev22.voterewards.hooks;

import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.google.common.collect.Lists;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author GeorgeV22
 */
public class AuthMe implements Listener {

    private final VoteReward voteReward = VoteReward.getInstance();

    @EventHandler
    public void onAuthLogin(LoginEvent event) {
        if (OptionsUtil.OFFLINE.getBooleanValue()) {
            voteReward.getPlayerDataManager().getEntity(event.getPlayer().getUniqueId()).handle((user, throwable) -> {
                if (throwable != null) {
                    voteReward.getLogger().log(Level.SEVERE, "Error while trying process offline services (AuthMe)", throwable);
                    return null;
                }
                return user;
            }).thenAccept(user -> {
                if (user != null) {
                    for (String serviceName : user.services()) {
                        try {
                            new VoteUtils(user).processVote(serviceName, false);
                        } catch (IOException e) {
                            voteReward.getLogger().log(Level.SEVERE, "Error while trying process offline services (AuthMe)", e);
                        }
                    }
                    user.services(Lists.newArrayList());
                    voteReward.getPlayerDataManager().save(user);
                }
            });
        }
    }

}
