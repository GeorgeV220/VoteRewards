package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.yaml.ConfigurationSection;
import com.georgev22.library.yaml.file.YamlConfiguration;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.google.common.annotations.Beta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

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
    public void backup() {
        BukkitMinecraftUtils.disallowLogin(true, "Backup ongoing!");
        File file = new File(getBackupFolder(), fileName + ".yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        int total = voteReward.getPlayerDataManager().getLoadedEntities().size();
        int progress = 0;
        for (Map.Entry<UUID, User> b : voteReward.getPlayerDataManager().getLoadedEntities().entrySet()) {
            yamlConfiguration.set(b.getKey().toString() + ".last-name", b.getValue().name());
            yamlConfiguration.set(b.getKey().toString() + ".uuid", b.getValue().getId().toString());
            yamlConfiguration.set(b.getKey().toString() + ".votes", b.getValue().votes());
            yamlConfiguration.set(b.getKey().toString() + ".all-time-votes", b.getValue().totalVotes());
            yamlConfiguration.set(b.getKey().toString() + ".daily-votes", b.getValue().dailyVotes());
            yamlConfiguration.set(b.getKey().toString() + ".voteparty", b.getValue().voteparty());
            yamlConfiguration.set(b.getKey().toString() + ".last-vote", b.getValue().lastVote());
            yamlConfiguration.set(b.getKey().toString() + ".services", b.getValue().services());
            yamlConfiguration.set(b.getKey().toString() + ".restored", false);
            progress += 1;
            System.out.println(progress * 100 / total);
        }

        try {
            yamlConfiguration.save(file);
            BukkitMinecraftUtils.disallowLogin(false, "");
        } catch (IOException exception) {
            voteReward.getLogger().log(Level.SEVERE, "Error while trying to back player data", exception);
        }
    }


    /**
     * @since v4.7.1.0
     */
    @Beta
    public void restore() {
        BukkitMinecraftUtils.disallowLogin(true, "Restore ongoing!");
        File file = new File(getBackupFolder(), fileName);
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection("");
        if (configurationSection == null) {
            return;
        }
        int total = configurationSection.getKeys(false).size();
        final int[] progress = {0};
        for (String b : configurationSection.getKeys(false)) {
            UUID uuid = UUID.fromString(Objects.requireNonNull(yamlConfiguration.getString(b + ".uuid")));
            User user = new User(uuid);
            user.addCustomDataIfNotExists("entity_id", uuid);
            user.name(yamlConfiguration.getString(b + ".last-name"));
            user.votes(yamlConfiguration.getInt(b + ".votes"));
            user.totalVotes(yamlConfiguration.getInt(b + ".all-time-votes"));
            user.dailyVotes(yamlConfiguration.getInt(b + ".daily-votes"));
            user.voteparty(yamlConfiguration.getInt(b + ".voteparty"));
            user.lastVote(yamlConfiguration.getLong(b + ".last-vote"));
            user.services(new ArrayList<>(yamlConfiguration.getStringList(b + ".services")));
            yamlConfiguration.set(b + ".restored", true);
            progress[0] += 1;
            voteReward.getLogger().info("Restore progress: " + progress[0] * 100 / total);
            try {
                yamlConfiguration.save(file);
                voteReward.getPlayerDataManager().save(user);
                if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                            VoteUtils.debugUserMessage(user, "saved", true));
                }
            } catch (IOException exception) {
                voteReward.getLogger().log(Level.SEVERE, "Error while trying to restore player data " + b, exception);
                break;
            }
        }
        BukkitMinecraftUtils.disallowLogin(false, "");
    }

}
