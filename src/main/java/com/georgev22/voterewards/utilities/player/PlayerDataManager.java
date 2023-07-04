package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.utilities.EntityManager;
import com.georgev22.library.yaml.ConfigurationSection;
import com.georgev22.voterewards.VoteReward;
import org.jetbrains.annotations.Nullable;

public class PlayerDataManager extends EntityManager<User> {

    private final VoteReward voteReward = VoteReward.getInstance();

    /**
     * Constructor for the EntityManager class
     *
     * @param obj            the object to be used for storage (DatabaseWrapper or File)
     * @param collectionName the name of the collection to be used for MONGODB and SQL, null for other types
     */
    public PlayerDataManager(Object obj, @Nullable String collectionName) {
        super(obj, collectionName, User.class);
    }

    /**
     * Get the total votes until the next cumulative reward
     *
     * @return Integer total votes until the next cumulative reward
     */
    public int votesUntilNextCumulativeVote(User user) {
        ConfigurationSection configurationSection = voteReward.getConfig().getConfigurationSection("Rewards.Cumulative");
        if (configurationSection == null) {
            return 0;
        }
        int votesUntil = 0;
        for (String b : configurationSection.getKeys(false)) {
            int cumulative = Integer.parseInt(b);
            if (cumulative <= user.votes()) {
                continue;
            }
            votesUntil = cumulative - user.votes();
            break;
        }
        return votesUntil;
    }
}
