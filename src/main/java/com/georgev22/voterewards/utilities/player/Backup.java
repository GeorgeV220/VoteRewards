package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.google.common.annotations.Beta;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.georgev22.library.utilities.Utils.*;

public class Backup {

    private final String fileName;

    public Backup(String fileName) {
        this.fileName = fileName;
    }

    private final VoteReward voteReward = VoteReward.getInstance();

    private @NotNull File getBackupFolder() {
        File backupFolder = new File(voteReward.getDataFolder(), "backups");
        if (backupFolder.mkdirs()) {
            if (OptionsUtil.DEBUG_CREATE.getBooleanValue()) {
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Backup folder has been created!");
            }
        }
        return backupFolder;
    }

    /**
     * @since v4.7.0
     */
    @Beta
    public void backup(Callback<Boolean> callback) {
        File file = new File(getBackupFolder(), fileName + ".yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        int total = UserVoteData.getAllUsersMap().size();
        int progress = 0;
        for (Map.Entry<UUID, User> b : UserVoteData.getAllUsersMap().entrySet()) {
            yamlConfiguration.set(b.getKey().toString() + ".last-name", b.getValue().getOfflinePlayer().getName());
            yamlConfiguration.set(b.getKey().toString() + ".uuid", b.getValue().getUniqueId().toString());
            yamlConfiguration.set(b.getKey().toString() + ".votes", b.getValue().getVotes());
            yamlConfiguration.set(b.getKey().toString() + ".all-time-votes", b.getValue().getAllTimeVotes());
            yamlConfiguration.set(b.getKey().toString() + ".daily-votes", b.getValue().getDailyVotes());
            yamlConfiguration.set(b.getKey().toString() + ".voteparty", b.getValue().getVoteParties());
            yamlConfiguration.set(b.getKey().toString() + ".last-vote", b.getValue().getLastVoted());
            yamlConfiguration.set(b.getKey().toString() + ".services", b.getValue().getServices());
            yamlConfiguration.set(b.getKey().toString() + ".object", b.getValue().toString());
            yamlConfiguration.set(b.getKey().toString() + ".restored", false);
            progress += 1;
            System.out.println(progress * 100 / total);
        }

        try {
            yamlConfiguration.save(file);
        } catch (IOException exception) {
            callback.onFailure(exception.getCause());
        }
        callback.onSuccess();
    }


    /**
     * @since v4.7.1.0
     */
    @Beta
    public void restore(Callback<Boolean> callback) {
        File file = new File(getBackupFolder(), fileName);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        int total = Objects.requireNonNull(yamlConfiguration.getConfigurationSection("")).getKeys(false).size();
        final int[] progress = {0};
        for (String b : Objects.requireNonNull(yamlConfiguration.getConfigurationSection("")).getKeys(false)) {
            UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(Objects.requireNonNull(yamlConfiguration.getString(b + ".uuid"))));
            try {
                userVoteData.load(new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        userVoteData
                                .setName(b + ".last-name")
                                .setVotes(yamlConfiguration.getInt(b + ".votes"))
                                .setAllTimeVotes(yamlConfiguration.getInt(b + ".all-time-votes"))
                                .setDailyVotes(yamlConfiguration.getInt(b + ".daily-votes"))
                                .setVoteParties(yamlConfiguration.getInt(b + ".voteparty"))
                                .setLastVoted(yamlConfiguration.getLong(b + ".last-vote"))
                                .setOfflineServices(yamlConfiguration.getStringList(b + ".services"));
                        userVoteData.save(true, new Callback<>() {
                            @Override
                            public Boolean onSuccess() {
                                if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                                            VoteUtils.debugUserMessage(userVoteData.user(), "saved", true));
                                }
                                return true;
                            }

                            @Override
                            public Boolean onFailure() {
                                return false;
                            }

                            @Override
                            public Boolean onFailure(Throwable throwable) {
                                throwable.printStackTrace();
                                return onFailure();
                            }
                        });
                        yamlConfiguration.set(b + ".restored", true);
                        progress[0] += 1;
                        System.out.println(progress[0] * 100 / total);
                        try {
                            yamlConfiguration.save(file);
                            return true;
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    public Boolean onFailure() {
                        return false;
                    }

                    @Override
                    public Boolean onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                        return true;
                    }
                });
            } catch (Exception exception) {
                callback.onFailure(exception.getCause());
            }
        }
        callback.onSuccess();
    }

}
