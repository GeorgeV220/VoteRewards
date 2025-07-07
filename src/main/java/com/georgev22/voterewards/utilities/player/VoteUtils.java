package com.georgev22.voterewards.utilities.player;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.LinkedObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.library.utilities.Utils;
import com.georgev22.library.yaml.ConfigurationSection;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

public record VoteUtils(User user) {

    private static final VoteReward voteReward = VoteReward.getInstance();

    private static final FileManager fileManager = FileManager.getInstance();

    /**
     * Process player vote
     *
     * @param serviceName the service name (dah)
     */
    public void processVote(String serviceName) throws IOException {
        processVote(serviceName, OptionsUtil.VOTEPARTY.getBooleanValue());
    }

    /**
     * Process player vote
     *
     * @param serviceName  the service name (dah)
     * @param addVoteParty count the vote on voteparty
     * @since v4.7.0
     */
    public void processVote(String serviceName, boolean addVoteParty) throws IOException {
        if (OptionsUtil.DEBUG_VOTES_REGULAR.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "VOTE OF: " + user.name());
        user.votes(user.votes() + 1);
        user.lastVote(System.currentTimeMillis());
        user.addServicesLastVote(serviceName);
        if (OptionsUtil.DEBUG_VOTES_REGULAR.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), user.servicesLastVote().toString());

        user.totalVotes(user.totalVotes() + 1);
        user.dailyVotes(user.dailyVotes() + 1);
        voteReward.getPlayerDataManager().save(user);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.getId());

        if (!offlinePlayer.isOnline()) {
            return;
        }

        if (offlinePlayer.getPlayer() == null) {
            return;
        }

        Player player = offlinePlayer.getPlayer();

        if (OptionsUtil.VOTE_TITLE.getBooleanValue()) {
            Titles.sendTitle(player,
                    BukkitMinecraftUtils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", user.name()),
                    BukkitMinecraftUtils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", user.name()));
        }

        // WORLD REWARDS (WITH SERVICES)
        if (OptionsUtil.WORLD.getBooleanValue()) {
            if (OptionsUtil.DEBUG_VOTES_WORLD.getBooleanValue())
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Vote of " + user.name() + " for world " + player.getWorld());
            if (voteReward.getConfig().getString("Rewards.Worlds." + player.getWorld() + "." + serviceName) != null && OptionsUtil.WORLD_SERVICES.getBooleanValue()) {
                runCommands(player, voteReward.getConfig()
                        .getStringList("Rewards.Worlds." + player.getWorld().getName() + "." + serviceName));
            } else {
                runCommands(player, voteReward.getConfig()
                        .getStringList("Rewards.Worlds." + player.getWorld().getName() + ".default"));
            }
        }

        // SERVICE REWARDS
        if (OptionsUtil.SERVICES.getBooleanValue()) {
            if (voteReward.getConfig().getString("Rewards.Services." + serviceName) != null) {
                runCommands(player, voteReward.getConfig()
                        .getStringList("Rewards.Services." + serviceName + ".commands"));
            } else {
                runCommands(player, voteReward.getConfig()
                        .getStringList("Rewards.Services.default.commands"));
            }
        }

        // LUCKY REWARDS
        if (OptionsUtil.LUCKY.getBooleanValue()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int i = random.nextInt(OptionsUtil.LUCKY_NUMBERS.getIntValue() + 1);
            ConfigurationSection configurationSection = voteReward.getConfig().getConfigurationSection("Rewards.Lucky");
            if (configurationSection != null) {
                for (String s2 : configurationSection.getKeys(false)) {
                    if (Integer.valueOf(s2).equals(i)) {
                        runCommands(player, voteReward.getConfig()
                                .getStringList("Rewards.Lucky." + s2 + ".commands"));
                    }
                }
            }
        }

        // PERMISSIONS REWARDS
        if (OptionsUtil.PERMISSIONS.getBooleanValue()) {
            ConfigurationSection configurationSection = voteReward.getConfig().getConfigurationSection("Rewards.Permission");
            if (configurationSection != null) {
                for (String s2 : configurationSection.getKeys(false)) {
                    if (player.hasPermission("voterewards.permission." + s2)) {
                        if (OptionsUtil.DEBUG_VOTES_PERMISSIONS.getBooleanValue())
                            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Vote of " + user.name() + " with permission " + "voterewards.permission." + s2);
                        runCommands(player, voteReward.getConfig()
                                .getStringList("Rewards.Permission." + s2 + ".commands"));
                    }
                }
            }
        }

        // CUMULATIVE REWARDS
        if (OptionsUtil.CUMULATIVE.getBooleanValue()) {
            ConfigurationSection configurationSection = voteReward.getConfig().getConfigurationSection("Rewards.Cumulative");
            if (configurationSection != null) {
                for (String s2 : configurationSection.getKeys(false)) {
                    if (Integer.valueOf(s2).equals(user.votes())) {
                        if (OptionsUtil.DEBUG_VOTES_CUMULATIVE.getBooleanValue())
                            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Vote of " + user.name() + " with cumulative number " + s2);
                        runCommands(player, voteReward.getConfig()
                                .getStringList("Rewards.Cumulative." + s2 + ".commands"));
                    }
                }
            }
        }

        // PLAY SOUND
        if (OptionsUtil.SOUND.getBooleanValue()) {
            if (BukkitMinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(BukkitMinecraftUtils.MinecraftVersion.V1_12_R1)) {
                player.playSound(
                        player.getLocation(),
                        Objects.requireNonNull(XSound.matchXSound(
                                OptionsUtil.SOUND_VOTE.getStringValue()
                        ).orElseThrow().parseSound()),
                        1000, 1);
                if (OptionsUtil.DEBUG_OTHER.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "========================================================");
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "SoundCategory doesn't exists in versions below 1.12");
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "SoundCategory doesn't exists in versions below 1.12");
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "========================================================");
                }
            } else {
                player.playSound(
                        player.getLocation(),
                        Objects.requireNonNull(XSound.matchXSound(
                                OptionsUtil.SOUND_VOTE.getStringValue()
                        ).orElseThrow().parseSound()),
                        org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_VOTE_CHANNEL.getStringValue()),
                        1000, 1);
            }
        }

        if (OptionsUtil.DAILY.getBooleanValue()) {
            int votes = user.dailyVotes();
            ConfigurationSection configurationSection = voteReward.getConfig().getConfigurationSection("Rewards.daily");
            if (configurationSection != null) {
                for (String s2 : configurationSection.getKeys(false)) {
                    if (Integer.valueOf(s2).equals(votes)) {
                        runCommands(player, voteReward.getConfig()
                                .getStringList("Rewards.Daily." + s2 + ".commands"));
                    }
                }
            }
        }

        // VOTE PARTY
        if (addVoteParty)
            VotePartyUtils.voteParty(offlinePlayer, false);

        // NPC/HOLOGRAM UPDATE
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            if (voteReward.getHolograms().isHooked())
                voteReward.getHolograms().updateAll();
            if (voteReward.getNoPlayerCharacterAPI().isHooked())
                voteReward.getNoPlayerCharacterAPI().updateAll();
        }

        // DISCORD WEBHOOK
        if (OptionsUtil.DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            FileConfiguration discordFileConfiguration = fileManager.getDiscord().getFileConfiguration();
            BukkitMinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "vote", User.placeholders(user), User.placeholders(user)).execute();
        }

        // DEBUG
        if (OptionsUtil.DEBUG_VOTE_AFTER.getBooleanValue()) {
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                    debugUserMessage(user, "", false));
        }
    }

    /**
     * Do not look at this:)
     * <p>
     * Process player offline vote
     *
     * @param serviceName service name (dah)
     */
    public void processOfflineVote(final String serviceName) {
        user.addServices(serviceName);
        voteReward.getPlayerDataManager().save(user);
        if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                    debugUserMessage(user, "saved", true));
        }
        VotePartyUtils.voteParty(Bukkit.getOfflinePlayer(user.name()), false);
    }

    /**
     * Monthly reset the players stats
     *
     * @since v4.7.0
     */
    public static void monthlyReset() {
        SchedulerManager.getScheduler().runTaskTimer(voteReward.getClass(), () -> {
            if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "monthlyReset0() Thread ID: " + Thread.currentThread().getId());
            CFG cfg = fileManager.getData();
            FileConfiguration dataConfiguration = cfg.getFileConfiguration();
            if (OptionsUtil.MONTHLY_REWARDS.getBooleanValue()) {
                for (int i = 0; i < OptionsUtil.MONTHLY_REWARDS_TO_TOP.getIntValue(); i++) {
                    String player = getTopPlayer(i);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
                    if (!offlinePlayer.hasPlayedBefore()) {
                        return;
                    }

                    runCommands(offlinePlayer, voteReward.getConfig().getStringList("Rewards.monthly." + i));
                }
            }
            if (dataConfiguration.getInt("month") != Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue()) {
                ObjectMap<UUID, User> objectMap = voteReward.getPlayerDataManager().getLoadedEntities();
                objectMap.forEach((uuid, user) -> User.reset(user, false));
                dataConfiguration.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                cfg.saveFile();
            }
        }, 20L, OptionsUtil.MONTHLY_MINUTES.getLongValue() * 1200L);
    }

    /**
     * Purge players data if they don't have vote for an X days.
     *
     * @since v4.7.0
     */
    public static void purgeData() {
        SchedulerManager.getScheduler().runTaskTimer(voteReward.getClass(), VoteUtils::purgeData0, 20L, OptionsUtil.PURGE_MINUTES.getLongValue() * 1200L);
    }

    private static void purgeData0() {
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "purgeData0() Thread ID: " + Thread.currentThread().getId());
        ObjectMap<UUID, User> objectMap = voteReward.getPlayerDataManager().getLoadedEntities();
        objectMap.forEach((uuid, user) -> {
            long time = user.lastVote() + (OptionsUtil.PURGE_DAYS.getLongValue() * 86400000);
            if (OptionsUtil.DEBUG_OTHER.getBooleanValue()) {
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(user.lastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(time).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
            }
            if (time <= System.currentTimeMillis()) {
                voteReward.getPlayerDataManager().delete(user);
            }

        });
    }

    /**
     * Reset daily votes
     *
     * @since v5.0.2
     */
    public static void dailyReset() {
        SchedulerManager.getScheduler().runTaskTimer(voteReward.getClass(), VoteUtils::dailyReset0, 20, 20 * 1200L);
    }

    private static void dailyReset0() {
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "dailyReset0() Thread ID: " + Thread.currentThread().getId());
        ObjectMap<UUID, User> objectMap = voteReward.getPlayerDataManager().getLoadedEntities();
        objectMap.forEach((uuid, user) -> {
            long time = user.lastVote() + (OptionsUtil.DAILY_HOURS.getIntValue() * 60 * 60 * 1000);
            if (OptionsUtil.DEBUG_OTHER.getBooleanValue()) {
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(user.lastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(time).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
            }

            if (time <= System.currentTimeMillis()) {
                if (user.dailyVotes() > 0) {
                    user.dailyVotes(0);
                    voteReward.getPlayerDataManager().save(user);
                }
            }
        });
    }

    /**
     * @param limit number of top monthly voters in a Map.
     * @return a {@link LinkedObjectMap} with limit top players.
     */
    public static LinkedObjectMap<String, Integer> getTopPlayers(int limit) {
        ObjectMap<String, Integer> objectMap = new LinkedObjectMap<>();

        for (Map.Entry<UUID, User> entry : voteReward.getPlayerDataManager().getLoadedEntities().entrySet()) {
            objectMap.append(entry.getValue().name(), entry.getValue().votes());
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * @return a {@link LinkedObjectMap} of all players.
     * @since v5.0
     */
    public static LinkedObjectMap<String, Integer> getPlayersByVotes() {
        ObjectMap<String, Integer> objectMap = new LinkedObjectMap<>();

        for (Map.Entry<UUID, User> entry : voteReward.getPlayerDataManager().getLoadedEntities().entrySet()) {
            objectMap.append(entry.getValue().name(), entry.getValue().votes());
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * @param limit number of top all time voters in a Map.
     * @return a {@link LinkedObjectMap} with limit top players.
     */
    public static LinkedObjectMap<String, Integer> getAllTimeTopPlayers(int limit) {
        ObjectMap<String, Integer> objectMap = new LinkedObjectMap<>();

        for (Map.Entry<UUID, User> entry : voteReward.getPlayerDataManager().getLoadedEntities().entrySet()) {
            objectMap.append(entry.getValue().name(), entry.getValue().totalVotes());
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * @return a {@link LinkedObjectMap} of all players.
     * @since v5.0
     */
    public static LinkedObjectMap<String, Integer> getPlayersByAllTimeVotes() {
        ObjectMap<String, Integer> objectMap = new LinkedObjectMap<>();

        for (Map.Entry<UUID, User> entry : voteReward.getPlayerDataManager().getLoadedEntities().entrySet()) {
            objectMap.append(entry.getValue().name(), entry.getValue().totalVotes());
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * Get the top player in X place
     *
     * @param number the number of the place
     * @return X place player name
     */
    public static @NotNull String getTopPlayer(int number) {
        try {
            return String.valueOf(getTopPlayers(number + 1).keySet().toArray()[number]).replace("[", "").replace("]", "");
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return "GeorgeV22";
        }
    }

    public static void reminder() {
        SchedulerManager.getScheduler().runTaskTimerAsynchronously(voteReward.getClass(), () -> reminderMap.forEach(VoteUtils::reminder0), 20, 20);
    }

    private static void reminder0(Player key, Long value) {
        if (value <= System.currentTimeMillis()) {
            voteReward.getPlayerDataManager().getEntity(key.getUniqueId()).handle((userData, throwable) -> {
                if (throwable != null) {
                    voteReward.getLogger().log(Level.SEVERE, "Error while trying to remind player to vote", throwable);
                    return null;
                }
                return userData;
            }).thenAccept(userData -> {
                if (userData != null) {
                    if (System.currentTimeMillis() >= userData.lastVote() + (24 * 60 * 60 * 1000)) {
                        ObjectMap<String, String> placeholders = new HashObjectMap<>();
                        placeholders.append("%player%", key.getName());
                        MessagesUtil.REMINDER.msg(key, placeholders, true);
                    }
                    reminderMap.append(key, System.currentTimeMillis() + (OptionsUtil.REMINDER_SEC.getIntValue() * 1000));
                }
            }).handle((unused, throwable) -> {
                if (throwable != null) {
                    voteReward.getLogger().log(Level.SEVERE, "Error while trying to remind player to vote", throwable);
                    return unused;
                }
                return unused;
            });
        }
    }

    /**
     * Creates the reminder map
     * <p>
     * creates a new, empty {@link ConcurrentObjectMap#ConcurrentObjectMap()}
     */
    public static final ObjectMap<Player, Long> reminderMap = new ConcurrentObjectMap<>();


    public static ObjectMap<String, String> getPlaceholdersMap(FileConfiguration data, FileManager fileManager) {
        final ObjectMap<String, String> map = new HashObjectMap<>();
        int top = 1;
        for (Map.Entry<String, Integer> b : VoteUtils.getTopPlayers(OptionsUtil.VOTETOP_VOTERS.getIntValue()).entrySet()) {
            String[] args = String.valueOf(b).split("=");
            map.append("%top-" + top + "%", args[0]).append("%vote-" + top + "%", args[1]);
            top++;
        }
        int allTimeTop = 1;
        for (Map.Entry<String, Integer> b : VoteUtils.getAllTimeTopPlayers(OptionsUtil.VOTETOP_ALL_TIME_VOTERS.getIntValue()).entrySet()) {
            String[] args = String.valueOf(b).split("=");
            map.append("%alltimetop-" + allTimeTop + "%", args[0]).append("%alltimevote-" + allTimeTop + "%", args[1]);
            allTimeTop++;
        }
        return map.append("%bar%", BukkitMinecraftUtils.getProgressBar(
                        data.getInt("VoteParty-Votes"),
                        OptionsUtil.VOTEPARTY_VOTES.getIntValue(),
                        OptionsUtil.VOTEPARTY_BARS.getIntValue(),
                        OptionsUtil.VOTEPARTY_BAR_SYMBOL.getStringValue(),
                        OptionsUtil.VOTEPARTY_COMPLETE_COLOR.getStringValue(),
                        OptionsUtil.VOTEPARTY_NOT_COMPLETE_COLOR.getStringValue()))
                .append("%voteparty_votes_until%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                        - fileManager.getData().getFileConfiguration().getInt("VoteParty-Votes", 0)))
                .append("%voteparty_votes_need%", OptionsUtil.VOTEPARTY_VOTES.getStringValue())
                .append("%voteparty_total_votes%", String.valueOf(fileManager.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                .appendIfTrue("%voteparty_votes_full%",
                        BukkitMinecraftUtils.colorize(Utils.placeHolder(
                                MessagesUtil.VOTEPARTY_WAITING_FOR_MORE_PLAYERS_PLACEHOLDER.getMessages()[0],
                                new HashObjectMap<String, String>()
                                        .append("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                                        .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_PLAYERS_NEED.getIntValue())),
                                true)),
                        BukkitMinecraftUtils.colorize(Utils.placeHolder(
                                MessagesUtil.VOTEPARTY_PLAYERS_FULL_PLACEHOLDER.getMessages()[0],
                                new HashObjectMap<String, String>()
                                        .append("%until%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                                                - fileManager.getData().getFileConfiguration().getInt("VoteParty-Votes", 0)))
                                        .append("%total%", String.valueOf(fileManager.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                                        .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue())),
                                true)),
                        OptionsUtil.VOTEPARTY_PLAYERS.getBooleanValue() & VotePartyUtils.isWaitingForPlayers());
    }

    public static String @NotNull [] debugUserMessage(@NotNull User user, String b, boolean c) {
        return new String[]{c ? "User " + user.name() + " successfully " + b + "!" : "User: " + user.name(),
                "Votes: " + user.votes(),
                "Daily Votes: " + user.dailyVotes(),
                "Last Voted: " + Instant.ofEpochMilli(user.lastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.lastVote(),
                "Services: " + user.services(),
                "Services Last Vote: " + user.servicesLastVote().entrySet().stream().toList(),
                "Vote Parties: " + user.voteparty(),
                "All time votes: " + user.totalVotes()};
    }

    public static void runCommands(@NotNull OfflinePlayer offlinePlayer, @NotNull List<String> s) {
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "RUNNING COMMANDS FOR PLAYER: " + offlinePlayer.getName());
        ObjectMap<String, String> objectMap = new HashObjectMap<String, String>().append("%player%", offlinePlayer.getName());
        for (String b : s) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), BukkitMinecraftUtils.placeholderAPI(offlinePlayer, b, objectMap, true));
        }
    }

}
