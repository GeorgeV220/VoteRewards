package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.LinkedObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.MinecraftUtils;
import com.georgev22.library.minecraft.xseries.XSound;
import com.georgev22.library.minecraft.xseries.messages.Titles;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.library.utilities.Utils;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.georgev22.library.utilities.Utils.Callback;

public record VoteUtils(User user) {

    private static VoteReward voteReward = VoteReward.getInstance();

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
            MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "VOTE OF: " + user.getName());
        UserVoteData userVoteData = UserVoteData.getUser(user.getUniqueId());
        userVoteData.setVotes(userVoteData.getVotes() + 1);
        userVoteData.setLastVoted(System.currentTimeMillis());
        userVoteData.appendServiceLastVote(serviceName);
        MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), userVoteData.getServicesLastVote().toString());

        userVoteData.setAllTimeVotes(userVoteData.getAllTimeVotes() + 1);
        userVoteData.setDailyVotes(userVoteData.getDailyVotes() + 1);
        UserVoteData.getAllUsersMap().append(user.getUniqueId(), UserVoteData.getUser(user.getUniqueId()).user());

        if (OptionsUtil.VOTE_TITLE.getBooleanValue()) {
            Titles.sendTitle(user.getPlayer(),
                    MinecraftUtils.colorize(MessagesUtil.VOTE_TITLE.getMessages()[0]).replace("%player%", user.getName()),
                    MinecraftUtils.colorize(MessagesUtil.VOTE_SUBTITLE.getMessages()[0]).replace("%player%", user.getName()));
        }

        // WORLD REWARDS (WITH SERVICES)
        if (OptionsUtil.WORLD.getBooleanValue()) {
            if (OptionsUtil.DEBUG_VOTES_WORLD.getBooleanValue())
                MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Vote of " + user.getName() + " for world " + user.getPlayer().getWorld());
            if (voteReward.getConfig().getString("Rewards.Worlds." + user.getPlayer().getWorld() + "." + serviceName) != null && OptionsUtil.WORLD_SERVICES.getBooleanValue()) {
                userVoteData.runCommands(voteReward.getConfig()
                        .getStringList("Rewards.Worlds." + user.getPlayer().getWorld().getName() + "." + serviceName));
            } else {
                userVoteData.runCommands(voteReward.getConfig()
                        .getStringList("Rewards.Worlds." + user.getPlayer().getWorld().getName() + ".default"));
            }
        }

        // SERVICE REWARDS
        if (OptionsUtil.SERVICES.getBooleanValue()) {
            if (voteReward.getConfig().getString("Rewards.Services." + serviceName) != null) {
                userVoteData.runCommands(voteReward.getConfig()
                        .getStringList("Rewards.Services." + serviceName + ".commands"));
            } else {
                userVoteData.runCommands(voteReward.getConfig()
                        .getStringList("Rewards.Services.default.commands"));
            }
        }

        // LUCKY REWARDS
        if (OptionsUtil.LUCKY.getBooleanValue()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int i = random.nextInt(OptionsUtil.LUCKY_NUMBERS.getIntValue() + 1);
            for (String s2 : voteReward.getConfig().getConfigurationSection("Rewards.Lucky")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(i)) {
                    userVoteData.runCommands(voteReward.getConfig()
                            .getStringList("Rewards.Lucky." + s2 + ".commands"));
                }
            }
        }

        // PERMISSIONS REWARDS
        if (OptionsUtil.PERMISSIONS.getBooleanValue()) {
            for (String s2 : voteReward.getConfig().getConfigurationSection("Rewards.Permission").getKeys(false)) {
                if (user.getPlayer().hasPermission("voterewards.permission." + s2)) {
                    if (OptionsUtil.DEBUG_VOTES_PERMISSIONS.getBooleanValue())
                        MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Vote of " + user.getName() + " with permission " + "voterewards.permission." + s2);
                    userVoteData.runCommands(voteReward.getConfig()
                            .getStringList("Rewards.Permission." + s2 + ".commands"));
                }
            }
        }

        // CUMULATIVE REWARDS
        if (OptionsUtil.CUMULATIVE.getBooleanValue()) {
            for (String s2 : voteReward.getConfig().getConfigurationSection("Rewards.Cumulative")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(userVoteData.getVotes())) {
                    if (OptionsUtil.DEBUG_VOTES_CUMULATIVE.getBooleanValue())
                        MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Vote of " + user.getName() + " with cumulative number " + s2);
                    userVoteData.runCommands(voteReward.getConfig()
                            .getStringList("Rewards.Cumulative." + s2 + ".commands"));
                }
            }
        }

        // PLAY SOUND
        if (OptionsUtil.SOUND.getBooleanValue()) {
            if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
                user.getPlayer().playSound(user.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_VOTE.getStringValue()).get().parseSound(),
                        1000, 1);
                if (OptionsUtil.DEBUG_OTHER.getBooleanValue()) {
                    MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "========================================================");
                    MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "SoundCategory doesn't exists in versions below 1.12");
                    MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "SoundCategory doesn't exists in versions below 1.12");
                    MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "========================================================");
                }
            } else {
                user.getPlayer().playSound(user.getPlayer().getLocation(), XSound
                                .matchXSound(OptionsUtil.SOUND_VOTE.getStringValue()).get().parseSound(),
                        org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_VOTE_CHANNEL.getStringValue()),
                        1000, 1);
            }
        }

        if (OptionsUtil.DAILY.getBooleanValue()) {
            int votes = userVoteData.getDailyVotes();
            for (String s2 : voteReward.getConfig().getConfigurationSection("Rewards.Daily")
                    .getKeys(false)) {
                if (Integer.valueOf(s2).equals(votes)) {
                    userVoteData.runCommands(voteReward.getConfig()
                            .getStringList("Rewards.Daily." + s2 + ".commands"));
                }
            }
        }

        // VOTE PARTY
        if (addVoteParty)
            VotePartyUtils.voteParty(user.getOfflinePlayer(), false);

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
            MinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "vote", user.placeholders(), user.placeholders()).execute();
        }

        // DEBUG
        if (OptionsUtil.DEBUG_VOTE_AFTER.getBooleanValue()) {
            MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                    debugUserMessage(user, "", false));
        }
    }

    /**
     * Do not look at this:)
     * <p>
     * Process player offline vote
     *
     * @param serviceName service name (dah)
     * @throws Exception When something goes wrong
     */
    public void processOfflineVote(final String serviceName) throws Exception {
        UserVoteData userVoteData = UserVoteData.getUser(user.getUniqueId());
        userVoteData.load(new Callback<>() {
            @Override
            public Boolean onSuccess() {
                List<String> services = userVoteData.getOfflineServices();
                services.add(serviceName);
                userVoteData.setOfflineServices(services);
                userVoteData.save(true, new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                            MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                                    debugUserMessage(user, "saved", true));
                        }
                        return true;
                    }

                    @Contract(pure = true)
                    @Override
                    public @NotNull Boolean onFailure() {
                        return false;
                    }

                    @Override
                    public @NotNull Boolean onFailure(@NotNull Throwable throwable) {
                        throwable.printStackTrace();
                        return onFailure();
                    }
                });
                VotePartyUtils.voteParty(user.getOfflinePlayer(), false);
                return true;
            }

            @Contract(pure = true)
            @Override
            public @NotNull Boolean onFailure() {
                return true;
            }

            @Override
            public @NotNull Boolean onFailure(@NotNull Throwable throwable) {
                throwable.printStackTrace();
                return onFailure();
            }
        });
    }

    /**
     * Monthly reset the players stats
     *
     * @since v4.7.0
     */
    public static void monthlyReset() {
        SchedulerManager.getScheduler().runTaskTimer(voteReward.getClass(), () -> {
            if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
                MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "monthlyReset0() Thread ID: " + Thread.currentThread().getId());
            CFG cfg = fileManager.getData();
            FileConfiguration dataConfiguration = cfg.getFileConfiguration();
            if (OptionsUtil.MONTHLY_REWARDS.getBooleanValue())
                for (int i = 0; i < OptionsUtil.MONTHLY_REWARDS_TO_TOP.getIntValue(); i++) {
                    String player = getTopPlayer(i);
                    UserVoteData userVoteData = UserVoteData.getUser(UserVoteData.getAllUsersMapWithName().entrySet().stream().filter(stringUserEntry -> stringUserEntry.getKey().equals(player)).findFirst().get().getValue().getOfflinePlayer());
                    userVoteData.runCommands(voteReward.getConfig().getStringList("Rewards.monthly." + i));
                }
            if (dataConfiguration.getInt("month") != Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue()) {
                ObjectMap<UUID, User> objectMap = UserVoteData.getAllUsersMap();
                objectMap.forEach((uuid, user) -> {
                    UserVoteData userVoteData = UserVoteData.getUser(uuid);
                    userVoteData.reset(false);
                });
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
            MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "purgeData0() Thread ID: " + Thread.currentThread().getId());
        ObjectMap<UUID, User> objectMap = UserVoteData.getAllUsersMap();
        objectMap.forEach((uuid, user) -> {
            UserVoteData userVoteData = UserVoteData.getUser(uuid);
            long time = userVoteData.getLastVote() + (OptionsUtil.PURGE_DAYS.getLongValue() * 86400000);
            if (OptionsUtil.DEBUG_OTHER.getBooleanValue()) {
                MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(userVoteData.getLastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
                MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(time).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
            }
            if (time <= System.currentTimeMillis()) {
                userVoteData.delete();
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
            MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "dailyReset0() Thread ID: " + Thread.currentThread().getId());
        ObjectMap<UUID, User> objectMap = UserVoteData.getAllUsersMap();
        objectMap.forEach((uuid, user) -> {
            UserVoteData userVoteData = UserVoteData.getUser(uuid);
            long time = userVoteData.getLastVote() + (OptionsUtil.DAILY_HOURS.getIntValue() * 60 * 60 * 1000);
            if (OptionsUtil.DEBUG_OTHER.getBooleanValue()) {
                MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(userVoteData.getLastVote()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
                MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), Instant.ofEpochMilli(time).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toString());
            }

            if (time <= System.currentTimeMillis()) {
                if (userVoteData.getDailyVotes() > 0) {
                    userVoteData.setDailyVotes(0);
                    if (user.getOfflinePlayer().isOnline()) {
                        objectMap.append(uuid, userVoteData.user());
                    } else {
                        userVoteData.save(true, new Callback<>() {
                            @Override
                            public Boolean onSuccess() {
                                if (OptionsUtil.DEBUG_VOTES_DAILY.getBooleanValue()) {
                                    MinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Daily vote reset!");
                                }
                                return true;
                            }

                            @Override
                            public Boolean onFailure() {
                                return true;
                            }

                            @Override
                            public Boolean onFailure(Throwable throwable) {
                                throwable.printStackTrace();
                                return onFailure();
                            }
                        });
                    }
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

        for (Map.Entry<UUID, User> entry : UserVoteData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("votes"));
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

        for (Map.Entry<UUID, User> entry : UserVoteData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("votes"));
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

        for (Map.Entry<UUID, User> entry : UserVoteData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("totalvotes"));
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

        for (Map.Entry<UUID, User> entry : UserVoteData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("totalvotes"));
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
            UserVoteData userVoteData = UserVoteData.getUser(key.getUniqueId());
            if (System.currentTimeMillis() >= userVoteData.getLastVote() + (24 * 60 * 60 * 1000)) {
                ObjectMap<String, String> placeholders = new HashObjectMap<>();
                placeholders.append("%player%", key.getName());
                MessagesUtil.REMINDER.msg(key, placeholders, true);
            }
            reminderMap.replace(key, System.currentTimeMillis() + (OptionsUtil.REMINDER_SEC.getIntValue() * 1000));
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
        return map.append("%bar%", MinecraftUtils.getProgressBar(
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
                        MinecraftUtils.colorize(Utils.placeHolder(
                                MessagesUtil.VOTEPARTY_WAITING_FOR_MORE_PLAYERS_PLACEHOLDER.getMessages()[0],
                                new HashObjectMap<String, String>()
                                        .append("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                                        .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_PLAYERS_NEED.getIntValue())),
                                true)),
                        MinecraftUtils.colorize(Utils.placeHolder(
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
        return new String[]{c ? "User " + user.getName() + " successfully " + b + "!" : "User: " + user.getName(),
                "Votes: " + user.getVotes(),
                "Daily Votes: " + user.getDailyVotes(),
                "Last Voted: " + Instant.ofEpochMilli(user.getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + user.getLastVoted(),
                "Services: " + user.getServices(),
                "Services Last Vote: " + user.getServicesLastVote().entrySet().stream().toList(),
                "Vote Parties: " + user.getVoteParties(),
                "All time votes: " + user.getAllTimeVotes()};
    }

}
