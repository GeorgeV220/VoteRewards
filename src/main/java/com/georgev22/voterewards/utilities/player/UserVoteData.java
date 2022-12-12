package com.georgev22.voterewards.utilities.player;

import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.LinkedObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.interfaces.IDatabaseType;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.georgev22.library.utilities.Utils.*;

/**
 * Used to handle all user's data and anything related to them.
 */
public record UserVoteData(User user) {

    private static final VoteReward voteReward = VoteReward.getInstance();

    private static final ObjectMap<UUID, User> allUsersMap = new ConcurrentObjectMap<>();

    public UserVoteData(@NotNull User user) {
        if (!allUsersMap.containsKey(user.getUniqueId())) {
            if (OptionsUtil.DEBUG_VOTE_PRE.getBooleanValue()) {
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Player " + Bukkit.getOfflinePlayer(user.getUniqueId()).getName() + " is not loaded!");
            }

            allUsersMap.append(user.getUniqueId(), new User(user.getUniqueId()));
        }
        this.user = allUsersMap.get(user.getUniqueId());
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), user.toString());
    }

    /**
     * Returns all the players in a map
     *
     * @return all the players
     */
    public static ObjectMap<UUID, User> getAllUsersMap() {
        return allUsersMap;
    }

    public static @NotNull ObjectMap<String, User> getAllUsersMapWithName() {
        ObjectMap<String, User> objectMap = new HashObjectMap<>();
        for (Map.Entry<UUID, User> entry : allUsersMap.entrySet()) {
            objectMap.append(entry.getValue().getName(), entry.getValue());
        }
        return objectMap;
    }

    /**
     * Load all users
     *
     * @throws Exception When something goes wrong
     */
    public static void loadAllUsers() throws Exception {
        allUsersMap.putAll(voteReward.getIDatabaseType().getAllUsers());
        if (OptionsUtil.DEBUG_LOAD.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), getAllUsersMap().toString());
    }

    /**
     * Returns a copy of this UserVoteData class for a specific user.
     *
     * @param offlinePlayer Offline player object
     * @return a copy of this UserVoteData class for a specific user.
     * @since v4.7.0
     */
    @Contract("_ -> new")
    public static @NotNull UserVoteData getUser(@NotNull OfflinePlayer offlinePlayer) {
        return getUser(offlinePlayer.getUniqueId());
    }

    /**
     * Returns a copy of this UserVoteData class for a specific user.
     *
     * @param uuid Player's Unique identifier
     * @return a copy of this UserVoteData class for a specific user.
     */
    @Contract("_ -> new")
    public static @NotNull UserVoteData getUser(@NotNull UUID uuid) {
        return new UserVoteData(new User(uuid));
    }

    public UserVoteData appendServiceLastVote(String serviceName) {
        user.append("servicesLastVote", user.getServicesLastVote().append(serviceName, user.getLastVoted()));
        return this;
    }

    /**
     * Set player name
     */
    public UserVoteData setName(String name) {
        user.append("name", name);
        return this;
    }

    /**
     * Set player votes
     *
     * @param votes The amount of votes.
     */
    public UserVoteData setVotes(int votes) {
        user.append("votes", votes);
        return this;
    }

    /**
     * Set player all time votes
     *
     * @param votes The amount of votes.
     */
    public UserVoteData setAllTimeVotes(int votes) {
        user.append("totalvotes", votes);
        return this;
    }

    /**
     * Set the last time when a player voted
     *
     * @param lastVoted Last time the player voted.
     */
    public UserVoteData setLastVoted(long lastVoted) {
        user.append("last", lastVoted);
        return this;
    }

    /**
     * Set how many virtual crates a player have
     *
     * @param voteParties The amount of vote party crates.
     */
    public UserVoteData setVoteParties(int voteParties) {
        user.append("voteparty", voteParties);
        return this;
    }

    /**
     * Set the offline services
     *
     * @param services The services that player voted when he was offline.
     */
    public UserVoteData setOfflineServices(List<String> services) {
        if (OptionsUtil.DEBUG_VOTES_OFFLINE.getBooleanValue())
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Offline Voting Debug" + services.toString());
        user.append("services", services);
        return this;
    }

    /**
     * Set daily votes
     *
     * @param votes The amount of daily votes
     */
    public UserVoteData setDailyVotes(int votes) {
        user.append("daily", votes);
        return this;
    }

    /**
     * Get user daily votes
     *
     * @return user daily votes
     */
    public int getDailyVotes() {
        return user.getDailyVotes();
    }


    /**
     * Get user all time votes
     *
     * @return user all time votes
     */
    public int getAllTimeVotes() {
        return user.getAllTimeVotes();
    }

    public ObjectMap<String, Long> getServicesLastVote() {
        return user.getServicesLastVote();
    }

    /**
     * Get user total votes
     *
     * @return user total votes
     */
    public int getVotes() {
        return user.getVotes();
    }

    /**
     * Get user virtual crates
     *
     * @return user virtual crates
     */
    public int getVoteParty() {
        return user.getVoteParties();
    }

    /**
     * Get the last time when the user voted
     *
     * @return the last time when the user voted
     */
    public long getLastVote() {
        return user.getLastVoted();
    }

    /**
     * Get all services that the user have voted
     * when he was offline
     *
     * @return services
     */
    public List<String> getOfflineServices() {
        return user.getServices();
    }

    /**
     * Check if the user exists
     *
     * @return true if user exists or false when is not
     */
    public boolean playerExists() {
        return getAllUsersMap().containsKey(user.getUniqueId());
    }

    /**
     * Get the total votes until the next cumulative reward
     *
     * @return Integer total votes until the next cumulative reward
     */
    public int votesUntilNextCumulativeVote() {
        if (voteReward.getConfig().getConfigurationSection("Rewards.Cumulative") == null) {
            return 0;
        }
        int votesUntil = 0;
        for (String b : voteReward.getConfig().getConfigurationSection("Rewards.Cumulative").getKeys(false)) {
            int cumulative = Integer.parseInt(b);
            if (cumulative <= getVotes()) {
                continue;
            }
            votesUntil = cumulative - getVotes();
            break;
        }
        return votesUntil;
    }

    /**
     * Run the commands_old from config
     * Check {@link BukkitMinecraftUtils#runCommand(Plugin, String)}
     *
     * @param s the list with all the commands_old
     */
    public void runCommands(@NotNull List<String> s) {
        BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "RUNNING COMMANDS FOR PLAYER: " + user.getName());
        for (String b : s) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), b.replace("%player%", Objects.requireNonNull(user.getName())));
        }
    }

    /**
     * Load user data
     *
     * @param callback Callback
     * @throws Exception When something goes wrong
     */
    public UserVoteData load(Callback<Boolean> callback) throws Exception {
        voteReward.getIDatabaseType().load(user, callback);
        return this;
    }

    /**
     * Save all user's data
     *
     * @param async True if you want to save async
     */
    public UserVoteData save(boolean async, Callback<Boolean> callback) {
        if (async) {
            SchedulerManager.getScheduler().runTaskAsynchronously(voteReward.getClass(), () -> save(callback));
        } else {
            save(callback);
        }
        return this;
    }

    private void save(Callback<Boolean> callback) {
        try {
            voteReward.getIDatabaseType().save(user);
            callback.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(e.getCause());
        }
    }

    /**
     * Reset user's stats
     *
     * @param allTime Set it true to reset all time votes.
     */
    public UserVoteData reset(boolean allTime) {
        SchedulerManager.getScheduler().runTaskAsynchronously(voteReward.getClass(), () -> {
            try {
                voteReward.getIDatabaseType().reset(user, allTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return this;
    }

    /**
     * Delete user from database
     */
    public UserVoteData delete() {
        SchedulerManager.getScheduler().runTaskAsynchronously(voteReward.getClass(), () -> {
            try {
                voteReward.getIDatabaseType().delete(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return this;
    }

    /**
     * All SQL Utils for the user
     * Everything here must run asynchronously
     * Expect shits to happen
     */
    public static class SQLUserUtils implements IDatabaseType {

        private static final VoteReward voteReward = VoteReward.getInstance();

        /**
         * Save all user's data
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class not found
         */
        public void save(@NotNull User user) throws SQLException, ClassNotFoundException {
            voteReward.getDatabaseWrapper().getSQLDatabase().updateSQL(
                    "UPDATE `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` " +
                            "SET `votes` = '" + user.getVotes() + "', " +
                            "`name` = '" + user.getName() + "', " +
                            "`time` = '" + user.getLastVoted() + "', " +
                            "`voteparty` = '" + user.getVoteParties() + "', " +
                            "`daily` = '" + user.getDailyVotes() + "', " +
                            "`services` = '" + stringListToString(user.getServices()) + "', " +
                            "`servicesLastVote` = '" + stringListToString(mapToStringList(user.getServicesLastVote())) + "', " +
                            "`totalvotes` = '" + user.getAllTimeVotes() + "' " +
                            "WHERE `uuid` = '" + user.getUniqueId() + "'");
        }

        /**
         * Remove user's data from database.
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class is not found
         */
        public void delete(@NotNull User user) throws SQLException, ClassNotFoundException {
            voteReward.getDatabaseWrapper().getSQLDatabase().updateSQL("DELETE FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.getUniqueId().toString() + "';");
            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "User " + user.getName() + " deleted from the database!");
            allUsersMap.remove(user.getUniqueId());
        }

        /**
         * Load all user's data
         *
         * @param callback Callback
         */
        public void load(User user, Callback<Boolean> callback) {
            setupUser(user, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    try {
                        ResultSet resultSet = voteReward.getDatabaseWrapper().getSQLDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.getUniqueId().toString() + "'");
                        while (resultSet.next()) {
                            user.append("votes", resultSet.getInt("votes"))
                                    .append("name", resultSet.getString("name"))
                                    .append("last", resultSet.getLong("time"))
                                    .append("servicesLastVote", stringListToObjectMap(stringToStringList(resultSet.getString("servicesLastVote")), Long.class))
                                    .append("services", stringToStringList(resultSet.getString("services")))
                                    .append("voteparty", resultSet.getInt("voteparty"))
                                    .append("daily", resultSet.getInt("daily"))
                                    .append("totalvotes", resultSet.getInt("totalvotes"));
                        }
                        return callback.onSuccess();
                    } catch (SQLException | ClassNotFoundException throwables) {
                        return callback.onFailure(throwables.getCause());
                    }
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

        /**
         * Check if the user exists
         *
         * @return true if user exists or false when is not
         */
        public boolean playerExists(@NotNull User user) throws SQLException, ClassNotFoundException {
            return voteReward.getDatabaseWrapper().getSQLDatabase().querySQL("SELECT * FROM " + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + " WHERE `uuid` = '" + user.getUniqueId().toString() + "'").next();
        }

        /**
         * Set up the user data to the database
         *
         * @param callback Callback
         */
        public void setupUser(User user, Callback<Boolean> callback) {
            try {
                if (!playerExists(user)) {
                    voteReward.getDatabaseWrapper().getSQLDatabase().updateSQL(
                            "INSERT INTO `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` (`uuid`, `name`, `votes`, `time`, `daily`, `voteparty`, `services`, `servicesLastVote`, `totalvotes`)" +
                                    " VALUES " +
                                    "('" + user.getUniqueId().toString() + "', '" + user.getOfflinePlayer().getName() + "','0', '0', '0', '0', '" + stringListToString(Lists.newArrayList()) + "', '" + stringListToString(Lists.newArrayList()) + "', '0'" + ");");
                }
                callback.onSuccess();
            } catch (SQLException | ClassNotFoundException throwables) {
                callback.onFailure(throwables.getCause());
            }
        }

        /**
         * Get all users from the database
         *
         * @return all the users from the database
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When the class is not found
         */
        public ObjectMap<UUID, User> getAllUsers() throws Exception {
            ObjectMap<UUID, User> map = new ConcurrentObjectMap<>();
            ResultSet resultSet = voteReward.getDatabaseWrapper().getSQLDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "`");
            while (resultSet.next()) {
                UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(resultSet.getString("uuid")));
                userVoteData.load(new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        map.append(userVoteData.user().getUniqueId(), userVoteData.user());
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
            return map;
        }
    }

    /**
     * All Mongo Utils for the user
     * Everything here must run asynchronously
     * Expect shits to happen
     */
    public static class MongoDBUtils implements IDatabaseType {

        /**
         * Save all user's data
         */
        public void save(@NotNull User user) {
            BasicDBObject query = new BasicDBObject();
            query.append("uuid", user.getUniqueId().toString());

            BasicDBObject updateObject = new BasicDBObject();
            updateObject.append("$set", new BasicDBObject()
                    .append("uuid", user.getUniqueId().toString())
                    .append("name", user.getName())
                    .append("votes", user.getVotes())
                    .append("voteparty", user.getVoteParties())
                    .append("daily", user.getDailyVotes())
                    .append("last-vote", user.getLastVoted())
                    .append("services", user.getServices())
                    .append("servicesLastVote", user.getServicesLastVote())
                    .append("totalvotes", user.getAllTimeVotes()));

            voteReward.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).updateOne(query, updateObject);
        }

        /**
         * Load user data
         *
         * @param user     User
         * @param callback Callback
         */
        public void load(User user, Callback<Boolean> callback) {
            setupUser(user, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    BasicDBObject searchQuery = new BasicDBObject();
                    searchQuery.append("uuid", user.getUniqueId().toString());
                    FindIterable<Document> findIterable = voteReward.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).find(searchQuery);
                    Document document = findIterable.first();
                    user.append("votes", document.getInteger("votes"))
                            .append("name", document.getString("name"))
                            .append("daily", document.getInteger("daily"))
                            .append("voteparty", document.getInteger("voteparty"))
                            .append("last", document.getLong("last-vote"))
                            .append("services", document.getList("services", String.class))
                            .append("servicesLastVote", document.get("servicesLastVote"))
                            .append("totalvotes", document.getInteger("totalvotes"));
                    return callback.onSuccess();
                }

                @Contract(pure = true)
                @Override
                public @NotNull Boolean onFailure() {
                    return true;
                }

                @Override
                public @NotNull Boolean onFailure(@NotNull Throwable throwable) {
                    callback.onFailure(throwable.getCause());
                    return onFailure();
                }
            });
        }

        /**
         * Set up the user
         *
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(User user, Callback<Boolean> callback) {
            if (!playerExists(user)) {
                voteReward.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).insertOne(new Document()
                        .append("uuid", user.getUniqueId().toString())
                        .append("name", user.getOfflinePlayer().getName())
                        .append("votes", 0)
                        .append("voteparty", 0)
                        .append("daily", 0)
                        .append("last-vote", 0L)
                        .append("services", Lists.newArrayList())
                        .append("servicesLastVote", ObjectMap.newHashObjectMap())
                        .append("totalvotes", 0));
            }
            callback.onSuccess();
        }

        /**
         * Check if the user exists
         *
         * @return true if user exists or false when is not
         */
        public boolean playerExists(@NotNull User user) {
            long count = voteReward.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).count(new BsonDocument("uuid", new BsonString(user.getUniqueId().toString())));
            return count > 0;
        }

        /**
         * Remove user's data from database.
         */
        public void delete(@NotNull User user) {
            BasicDBObject theQuery = new BasicDBObject();
            theQuery.put("uuid", user.getUniqueId().toString());
            DeleteResult result = voteReward.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).deleteMany(theQuery);
            if (result.getDeletedCount() > 0) {
                if (OptionsUtil.DEBUG_DELETE.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "User " + user.getName() + " deleted from the database!");
                }
                allUsersMap.remove(user.getUniqueId());
            }
        }


        /**
         * Get all users from the database
         *
         * @return all the users from the database
         */
        public ObjectMap<UUID, User> getAllUsers() {
            ObjectMap<UUID, User> map = new ConcurrentObjectMap<>();
            FindIterable<Document> iterable = voteReward.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).find();
            iterable.forEach((Block<Document>) document -> {
                UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(document.getString("uuid")));
                try {
                    userVoteData.load(new Callback<>() {
                        @Override
                        public Boolean onSuccess() {
                            map.append(userVoteData.user().getUniqueId(), userVoteData.user());
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return map;
        }

    }


    /**
     * All User Utils for the user
     */

    public static class FileUserUtils implements IDatabaseType {

        private final VoteReward voteReward = VoteReward.getInstance();

        /**
         * Save all user's data
         *
         * @param user User object
         */
        @Override
        public void save(@NotNull User user) {
            File file = new File(voteReward.getDataFolder(),
                    "userdata" + File.separator + user.getUniqueId().toString() + ".yml");
            if (!file.exists()) {
                setupUser(user, new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
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
            }
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            yamlConfiguration.set("votes", user.getVotes());
            yamlConfiguration.set("name", user.getName());
            yamlConfiguration.set("time", user.getLastVoted());
            yamlConfiguration.set("services", user.getServices());
            yamlConfiguration.set("servicesLastVote", mapToStringList(user.getServicesLastVote()));
            yamlConfiguration.set("voteparty", user.getVoteParties());
            yamlConfiguration.set("daily", user.getDailyVotes());
            yamlConfiguration.set("totalvotes", user.getAllTimeVotes());
            try {
                yamlConfiguration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Load user data
         *
         * @param user     User object
         * @param callback Callback
         */
        public void load(User user, Callback<Boolean> callback) {
            setupUser(user, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(voteReward.getDataFolder(),
                            "userdata" + File.separator + user.getUniqueId().toString() + ".yml"));
                    user.append("votes", yamlConfiguration.getInt("votes"))
                            .append("name", yamlConfiguration.getString("name"))
                            .append("last", yamlConfiguration.getLong("time"))
                            .append("services", yamlConfiguration.getStringList("services"))
                            .append("servicesLastVote", stringListToObjectMap(yamlConfiguration.getStringList("servicesLastVote"), Long.class))
                            .append("voteparty", yamlConfiguration.getInt("voteparty"))
                            .append("daily", yamlConfiguration.getInt("daily"))
                            .append("totalvotes", yamlConfiguration.getInt("totalvotes"));
                    return callback.onSuccess();
                }

                @Contract(pure = true)
                @Override
                public @NotNull Boolean onFailure() {
                    return false;
                }

                @Override
                public @NotNull Boolean onFailure(@NotNull Throwable throwable) {
                    callback.onFailure(throwable.getCause());
                    return onFailure();
                }
            });
        }

        /**
         * Set up the user
         *
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(User user, Callback<Boolean> callback) {
            if (new File(voteReward.getDataFolder(),
                    "userdata").mkdirs()) {
                if (OptionsUtil.DEBUG_CREATE.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Folder userdata has been created!");
                }
            }
            File file = new File(voteReward.getDataFolder(),
                    "userdata" + File.separator + user.getUniqueId().toString() + ".yml");
            if (!playerExists(user)) {
                try {
                    if (file.createNewFile()) {
                        if (OptionsUtil.DEBUG_CREATE.getBooleanValue()) {
                            BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "File " + file.getName() + " for the user " + Bukkit.getOfflinePlayer(user.getUniqueId()).getName() + " has been created!");
                        }
                    }
                } catch (IOException e) {
                    callback.onFailure(e.getCause());
                }
                user.append("votes", 0)
                        .append("name", user.getOfflinePlayer().getName())
                        .append("last", 0L)
                        .append("services", Lists.newArrayList())
                        .append("servicesLastVote", ObjectMap.newHashObjectMap())
                        .append("voteparty", 0)
                        .append("daily", 0)
                        .append("totalvotes", 0);
                save(user);
            }
            callback.onSuccess();
        }

        /**
         * Remove user's data from file.
         */
        public void delete(@NotNull User user) {
            File file = new File(voteReward.getDataFolder(),
                    "userdata" + File.separator + user.getUniqueId().toString() + ".yml");
            if (file.exists() & file.delete()) {
                if (OptionsUtil.DEBUG_DELETE.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "File " + file.getName() + " deleted!");
                }
                UserVoteData.getAllUsersMap().remove(user.getUniqueId());
            }
        }

        /**
         * Check if the user exists
         *
         * @return true if user exists or false when is not
         */
        public boolean playerExists(@NotNull User user) {
            return new File(voteReward.getDataFolder(),
                    "userdata" + File.separator + user.getUniqueId().toString() + ".yml").exists();
        }

        public ObjectMap<UUID, User> getAllUsers() throws Exception {
            ObjectMap<UUID, User> map = new LinkedObjectMap<>();

            File[] files = new File(voteReward.getDataFolder(), "userdata").listFiles((dir, name) -> name.endsWith(".yml"));

            if (files == null) {
                return map;
            }

            for (File file : files) {
                UserVoteData userVoteData = UserVoteData.getUser(UUID.fromString(file.getName().replace(".yml", "")));
                userVoteData.load(new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        map.append(userVoteData.user().getUniqueId(), userVoteData.user());
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
            }

            return map;
        }
    }
}
