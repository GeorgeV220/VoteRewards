package com.georgev22.voterewards;

import co.aikar.commands.PaperCommandManager;
import com.georgev22.api.database.DatabaseType;
import com.georgev22.api.database.DatabaseWrapper;
import com.georgev22.api.database.mongo.MongoDB;
import com.georgev22.api.extensions.Extension;
import com.georgev22.api.extensions.ExtensionLoader;
import com.georgev22.api.extensions.JavaExtensionLoader;
import com.georgev22.api.maps.HashObjectMap;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.maven.LibraryLoader;
import com.georgev22.api.maven.MavenLibrary;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.api.minecraft.configmanager.CFG;
import com.georgev22.api.minecraft.inventory.PagedInventoryAPI;
import com.georgev22.voterewards.commands.*;
import com.georgev22.voterewards.hooks.*;
import com.georgev22.voterewards.listeners.DeveloperInformListener;
import com.georgev22.voterewards.listeners.PlayerListeners;
import com.georgev22.voterewards.listeners.VotifierListener;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.interfaces.Holograms;
import com.georgev22.voterewards.utilities.interfaces.IDatabaseType;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoClient;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.georgev22.api.utilities.Utils.Callback;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "30.1.1-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
@MavenLibrary(groupId = "commons-io", artifactId = "commons-io", version = "2.11.0")
@MavenLibrary(groupId = "commons-codec", artifactId = "commons-codec", version = "1.15")
public class VoteRewardPlugin extends JavaPlugin {

    private static VoteRewardPlugin instance = null;

    @Getter
    private DatabaseWrapper databaseWrapper = null;

    /**
     * Return Database Type
     *
     * @return Database Type
     */
    @Getter
    private IDatabaseType iDatabaseType = null;

    /**
     * Get Database open connection
     *
     * @return connection
     */
    @Getter
    private Connection connection = null;

    /**
     * Return MongoDB instance when MongoDB is in use.
     * <p>
     * Returns null if MongoDB is not in use
     *
     * @return {@link MongoDB} instance
     */
    @Getter
    private @Nullable MongoClient mongoClient = null;

    @Getter
    private Holograms holograms = new Holograms.HologramsNoop();

    @Getter
    private PagedInventoryAPI pagedInventoryAPI = null;

    private PAPI placeholdersAPI = null;

    @Getter
    private PaperCommandManager commandManager;

    private final List<Extension> extensionList = Lists.newArrayList();

    /**
     * Return the VoteRewardPlugin instance
     *
     * @return VoteRewardPlugin instance
     */
    public static VoteRewardPlugin getInstance() {
        return instance == null ? instance = VoteRewardPlugin.getPlugin(VoteRewardPlugin.class) : instance;
    }

    @Override
    public void onLoad() {
        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_16_R1))
            new LibraryLoader(this.getClass(), this.getDataFolder()).loadAll();
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("""

                 __     __             __                _______                                                     __          \s
                |  \\   |  \\           |  \\              |       \\                                                   |  \\         \s
                | $$   | $$  ______  _| $$_     ______  | $$$$$$$\\  ______   __   __   __   ______    ______    ____| $$  _______\s
                | $$   | $$ /      \\|   $$ \\   /      \\ | $$__| $$ /      \\ |  \\ |  \\ |  \\ |      \\  /      \\  /      $$ /       \\
                 \\$$\\ /  $$|  $$$$$$\\\\$$$$$$  |  $$$$$$\\| $$    $$|  $$$$$$\\| $$ | $$ | $$  \\$$$$$$\\|  $$$$$$\\|  $$$$$$$|  $$$$$$$
                  \\$$\\  $$ | $$  | $$ | $$ __ | $$    $$| $$$$$$$\\| $$    $$| $$ | $$ | $$ /      $$| $$   \\$$| $$  | $$ \\$$    \\\s
                   \\$$ $$  | $$__/ $$ | $$|  \\| $$$$$$$$| $$  | $$| $$$$$$$$| $$_/ $$_/ $$|  $$$$$$$| $$      | $$__| $$ _\\$$$$$$\\
                    \\$$$    \\$$    $$  \\$$  $$ \\$$     \\| $$  | $$ \\$$     \\ \\$$   $$   $$ \\$$    $$| $$       \\$$    $$|       $$
                     \\$      \\$$$$$$    \\$$$$   \\$$$$$$$ \\$$   \\$$  \\$$$$$$$  \\$$$$$\\$$$$   \\$$$$$$$ \\$$        \\$$$$$$$ \\$$$$$$$\s
                                                                                                                                 \s
                """);
        final FileManager fm = FileManager.getInstance();
        fm.loadFiles(this);
        MessagesUtil.repairPaths(fm.getMessages());
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();
        pagedInventoryAPI = new PagedInventoryAPI(this);
        commandManager = new PaperCommandManager(this);
        if (OptionsUtil.DEBUG_USELESS.getBooleanValue())
            MinecraftUtils.debug(this, "onEnable Thread ID: " + Thread.currentThread().getId());
        MinecraftUtils.registerListeners(this, new VotifierListener(), new PlayerListeners(), new DeveloperInformListener());

        if (data.get("month") == null) {
            data.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
            dataCFG.saveFile();
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                setupDatabase();
            } catch (Exception throwable) {
                throwable.printStackTrace();
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholdersAPI = new PAPI();
            if (placeholdersAPI.register())
                Bukkit.getLogger().info("[VoteRewards] Hooked into PlaceholderAPI!");
        }

        if (OptionsUtil.HOLOGRAMS_ENABLED.getBooleanValue()) {
            switch (OptionsUtil.HOLOGRAMS_TYPE.getStringValue()) {
                case "ProtocolLib" -> {
                    if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                        holograms = new HologramAPI();
                    }
                }
                case "HolographicDisplays" -> {
                    if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
                        holograms = new HolographicDisplaysHook();
                    }
                }
            }

            holograms.setHook(true);

            if (holograms.isHooked()) {
                if (data.get("Holograms") != null) {
                    Objects.requireNonNull(data.getConfigurationSection("Holograms")).getKeys(false)
                            .forEach(s -> holograms.create(s, (Location) data.get("Holograms." + s + ".location"),
                                    data.getString("Holograms." + s + ".type"), false));
                }
                Bukkit.getLogger().info("[VoteRewards] " + holograms.getClass().getName() + " hooked - Holograms enabled!");
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            NPCAPI.setHook(true);
            Bukkit.getLogger().info("[VoteRewards] ProtocolLib installed - NPCs enabled!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
            Bukkit.getPluginManager().registerEvents(new AuthMe(), this);
            Bukkit.getLogger().info("[VoteRewards] Hooked into AuthMeReloaded!");
        }

        if (OptionsUtil.UPDATER.getBooleanValue()) {
            new Updater();
        }

        new Metrics(this, 3179);
        if (YamlConfiguration.loadConfiguration(new File(new File(this.getDataFolder().getParentFile(), "bStats"), "config.yml")).getBoolean("enabled", true)) {
            Bukkit.getLogger().info("[VoteRewards] Metrics are enabled!");
        }

        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
            MinecraftUtils.debug(this, "This version of Minecraft is extremely outdated and support for it has reached its end of life. You will still be able to run VoteRewards on this Minecraft version(" + MinecraftUtils.MinecraftVersion.getCurrentVersionNameVtoLowerCase() + "). Please consider updating to give your players a better experience and to avoid issues that have long been fixed.");
        }
        try {
            ExtensionLoader extensionLoader = new JavaExtensionLoader(Bukkit.getLogger());
            File extensionsFolder = new File(getDataFolder(), "extensions");
            if (!extensionsFolder.exists())
                extensionsFolder.mkdirs();

            File[] files = new File(getDataFolder(), "extensions").listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null) {
                for (File jarFile : files) {
                    Extension extension;
                    extensionLoader.enableExtension(extension = extensionLoader.loadExtension(jarFile));
                    extensionList.add(extension);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        setupCommands();
    }

    @Override
    public void onDisable() {
        for (Extension extension : extensionList) {
            extension.onDisable();
        }
        extensionList.clear();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (placeholdersAPI.isRegistered()) {
                if (placeholdersAPI.unregister()) {
                    Bukkit.getLogger().info("[VoteRewards] Unhooked from PlaceholderAPI!");
                }
            }
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            userVoteData.save(false, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                        MinecraftUtils.debug(VoteRewardPlugin.getInstance(),
                                "User " + userVoteData.user().getName() + " successfully saved!",
                                "Votes: " + userVoteData.user().getVotes(),
                                "Daily Votes: " + userVoteData.user().getDailyVotes(),
                                "Last Voted: " + Instant.ofEpochMilli(userVoteData.user().getLastVoted()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())) + userVoteData.user().getLastVoted(),
                                "Vote Parties: " + userVoteData.user().getVoteParties(),
                                "All time votes: " + userVoteData.user().getAllTimeVotes());
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
        });
        if (holograms.isHooked() && !holograms.getHologramMap().isEmpty())
            holograms.getHologramMap().forEach((name, hologram) -> holograms.remove(name, false));
        if (NPCAPI.isHooked() && !NPCAPI.getNPCMap().isEmpty())
            NPCAPI.getNPCMap().forEach((name, npcIntegerPair) -> NPCAPI.remove(name, false));
        if (OptionsUtil.COMMAND_FAKEVOTE.getBooleanValue())
            commandManager.unregisterCommand(new FakeVoteCommand());
        if (OptionsUtil.COMMAND_HOLOGRAM.getBooleanValue() & holograms.isHooked())
            commandManager.unregisterCommand(new HologramCommand());
        if (OptionsUtil.COMMAND_VOTE.getBooleanValue())
            commandManager.unregisterCommand(new VoteCommand());
        if (OptionsUtil.COMMAND_VOTEREWARDS.getBooleanValue())
            commandManager.unregisterCommand(new VoteRewardsCommand());
        if (OptionsUtil.COMMAND_VOTES.getBooleanValue())
            commandManager.unregisterCommand(new VotesCommand());
        if (OptionsUtil.COMMAND_VOTEPARTY.getBooleanValue())
            commandManager.unregisterCommand(new VotePartyCommand());
        if (OptionsUtil.COMMAND_REWARDS.getBooleanValue())
            commandManager.unregisterCommand(new RewardsCommand());
        if (OptionsUtil.COMMAND_VOTETOP.getBooleanValue())
            commandManager.unregisterCommand(new VoteTopCommand());
        if (OptionsUtil.COMMAND_NPC.getBooleanValue() & Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
            commandManager.unregisterCommand(new NPCCommand());
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    /**
     * Setup database Values: File, MySQL, SQLite
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    private void setupDatabase() throws Exception {
        if (OptionsUtil.DEBUG_USELESS.getBooleanValue())
            MinecraftUtils.debug(this, "Setup database Thread ID: " + Thread.currentThread().getId());
        ObjectMap<String, ObjectMap.Pair<String, String>> map = new HashObjectMap<String, ObjectMap.Pair<String, String>>()
                .append("uuid", ObjectMap.Pair.create("VARCHAR(38)", "NULL"))
                .append("name", ObjectMap.Pair.create("VARCHAR(18)", "NULL"))
                .append("votes", ObjectMap.Pair.create("INT(10)", "0"))
                .append("time", ObjectMap.Pair.create("BIGINT(30)", "0"))
                .append("voteparty", ObjectMap.Pair.create("INT(10)", "0"))
                .append("daily", ObjectMap.Pair.create("INT(10)", "0"))
                .append("services", ObjectMap.Pair.create("TEXT", "NULL"))
                .append("servicesLastVote", ObjectMap.Pair.create("TEXT", "NULL"))
                .append("totalvotes", ObjectMap.Pair.create("INT(10)", "0"));
        switch (OptionsUtil.DATABASE_TYPE.getStringValue()) {
            case "MySQL" -> {
                if (connection == null || connection.isClosed()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.MYSQL,
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue());
                    connection = databaseWrapper.connect().getSQLConnection();
                    databaseWrapper.getSQLDatabase().createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    MinecraftUtils.debug(this, "Database: MySQL");
                }
            }
            case "PostgreSQL" -> {
                if (connection == null || connection.isClosed()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.POSTGRESQL,
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue());
                    connection = databaseWrapper.connect().getSQLConnection();
                    databaseWrapper.getSQLDatabase().createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    MinecraftUtils.debug(this, "Database: PostgreSQL");
                }
            }
            case "SQLite" -> {
                if (connection == null || connection.isClosed()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.SQLITE, getDataFolder().getAbsolutePath(), OptionsUtil.DATABASE_SQLITE.getStringValue());
                    connection = databaseWrapper.connect().getSQLConnection();
                    databaseWrapper.getSQLDatabase().createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    MinecraftUtils.debug(this, "Database: SQLite");
                }
            }
            case "MongoDB" -> {
                databaseWrapper = new DatabaseWrapper(DatabaseType.MONGO,
                        OptionsUtil.DATABASE_MONGO_HOST.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PORT.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_USER.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PASSWORD.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue());
                iDatabaseType = new UserVoteData.MongoDBUtils();
                mongoClient = databaseWrapper.connect().getMongoClient();
                MinecraftUtils.debug(this, "Database: MongoDB");
            }
            case "File" -> {
                databaseWrapper = null;
                iDatabaseType = new UserVoteData.FileUserUtils();
                MinecraftUtils.debug(this, "Database: File");
            }
            default -> {
                setEnabled(false);
                throw new RuntimeException("Please use one of the available databases\nAvailable databases: File, MySQL, SQLite, PostgreSQL and MongoDB");
            }
        }

        UserVoteData.loadAllUsers();

        Bukkit.getOnlinePlayers().forEach(player -> {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            try {
                userVoteData.load(new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        UserVoteData.getAllUsersMap().append(userVoteData.user().getUniqueId(), userVoteData.user());
                        if (OptionsUtil.DEBUG_LOAD.getBooleanValue())
                            MinecraftUtils.debug(VoteRewardPlugin.getInstance(), "Successfully loaded user " + userVoteData.user().getOfflinePlayer().getName());
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (holograms.isHooked())
            holograms.updateAll();
        if (NPCAPI.isHooked())
            NPCAPI.updateAll();

        if (OptionsUtil.PURGE_ENABLED.getBooleanValue())
            VoteUtils.purgeData();

        if (OptionsUtil.MONTHLY_ENABLED.getBooleanValue())
            VoteUtils.monthlyReset();

        if (OptionsUtil.REMINDER.getBooleanValue()) {
            VoteUtils.reminder();
        }

        if (OptionsUtil.DAILY.getBooleanValue()) {
            VoteUtils.dailyReset();
        }
    }

    private void setupCommands() {
        commandManager.enableUnstableAPI("help");

        loadCommandLocales(commandManager);

        if (OptionsUtil.COMMAND_FAKEVOTE.getBooleanValue())
            commandManager.registerCommand(new FakeVoteCommand());
        if (OptionsUtil.COMMAND_HOLOGRAM.getBooleanValue() & holograms.isHooked())
            commandManager.registerCommand(new HologramCommand());
        if (OptionsUtil.COMMAND_VOTE.getBooleanValue())
            commandManager.registerCommand(new VoteCommand());
        if (OptionsUtil.COMMAND_VOTEREWARDS.getBooleanValue())
            commandManager.registerCommand(new VoteRewardsCommand());
        if (OptionsUtil.COMMAND_VOTES.getBooleanValue())
            commandManager.registerCommand(new VotesCommand());
        if (OptionsUtil.COMMAND_VOTEPARTY.getBooleanValue())
            commandManager.registerCommand(new VotePartyCommand());
        if (OptionsUtil.COMMAND_REWARDS.getBooleanValue())
            commandManager.registerCommand(new RewardsCommand());
        if (OptionsUtil.COMMAND_VOTETOP.getBooleanValue())
            commandManager.registerCommand(new VoteTopCommand());
        if (OptionsUtil.COMMAND_NPC.getBooleanValue() & Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
            commandManager.registerCommand(new NPCCommand());
    }

    private void loadCommandLocales(@NotNull PaperCommandManager commandManager) {
        try {
            saveResource("lang_en.yaml", true);
            commandManager.getLocales().setDefaultLocale(Locale.ENGLISH);
            commandManager.getLocales().loadYamlLanguageFile("lang_en.yaml", Locale.ENGLISH);
            commandManager.usePerIssuerLocale(true);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to load language config 'lang_en.yaml': " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @NotNull
    public FileConfiguration getConfig() {
        return FileManager.getInstance().getConfig().getFileConfiguration();
    }

    @Override
    public void saveConfig() {
        FileManager.getInstance().getConfig().saveFile();
    }


}
