package com.georgev22.voterewards;

import co.aikar.commands.PaperCommandManager;
import com.georgev22.api.libraryloader.LibraryLoader;
import com.georgev22.api.libraryloader.annotations.MavenLibrary;
import com.georgev22.api.libraryloader.exceptions.InvalidDependencyException;
import com.georgev22.api.libraryloader.exceptions.UnknownDependencyException;
import com.georgev22.library.database.DatabaseType;
import com.georgev22.library.database.DatabaseWrapper;
import com.georgev22.library.database.mongo.MongoDB;
import com.georgev22.library.extensions.Extension;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.MinecraftUtils;
import com.georgev22.library.minecraft.inventory.PagedInventoryAPI;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.library.utilities.Utils;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.library.yaml.serialization.ConfigurationSerialization;
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
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "30.1.1-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
@MavenLibrary(groupId = "commons-io", artifactId = "commons-io", version = "2.11.0")
@MavenLibrary(groupId = "commons-codec", artifactId = "commons-codec", version = "1.15")
public class VoteReward {

    private final List<Extension> extensionList = Lists.newArrayList();

    private PAPI placeholdersAPI = null;

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
    @Setter
    private Plugin plugin = null;

    @Getter
    private NoPlayerCharacterAPI noPlayerCharacterAPI = null;

    private static VoteReward instance = null;

    @Getter
    private FileManager fileManager;

    @Getter
    private Logger logger;

    @Getter
    private File dataFolder;

    private VoteRewardImpl voteReward;

    private PaperCommandManager commandManager;

    @Getter
    private PagedInventoryAPI pagedInventoryAPI = null;

    public static VoteReward getInstance() {
        return instance;
    }

    protected VoteReward(@NotNull VoteRewardImpl voteReward) {
        this.voteReward = voteReward;
        this.dataFolder = voteReward.getDataFolder();
        this.logger = voteReward.getLogger();
    }

    public void onLoad() throws UnknownDependencyException, InvalidDependencyException {
        instance = this;
        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_16_R1))
            new LibraryLoader(this.getClass(), this.getDataFolder()).loadAll();
        ConfigurationSerialization.registerClass(MinecraftUtils.SerializableLocation.class);
    }

    public void onEnable() throws Exception {
        fileManager = FileManager.getInstance();

        fileManager.loadFiles(this.logger, this.getClass());

        MessagesUtil.repairPaths(fileManager.getMessages());

        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            MinecraftUtils.debug(getName(), getVersion(), "onEnable() Thread ID: " + Thread.currentThread().getId());
        logger.info("""

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

        CFG dataCFG = fileManager.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            MinecraftUtils.debug(getName(), getVersion(), "onEnable() Thread ID: " + Thread.currentThread().getId());
        if (data.get("month") == null) {
            data.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
            dataCFG.saveFile();
        }
        if (getMain().endsWith("VoteRewardPlugin")) {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> SchedulerManager.getScheduler().mainThreadHeartbeat(Bukkit.getServer().getCurrentTick()), 0L, 1L);
        }
        MinecraftUtils.registerListeners(plugin, new VotifierListener(), new PlayerListeners(), new DeveloperInformListener());
        pagedInventoryAPI = new PagedInventoryAPI(plugin);
        commandManager = new PaperCommandManager(plugin);

        if (!MinecraftUtils.MinecraftVersion.getCurrentVersion().isAboveOrEqual(MinecraftUtils.MinecraftVersion.UNKNOWN)) {
            new Metrics(plugin, 3179);
            if (YamlConfiguration.loadConfiguration(new File(new File(this.getDataFolder().getParentFile(), "bStats"), "config.yml")).getBoolean("enabled", true)) {
                MinecraftUtils.debug(getName(), getVersion(), "Metrics are enabled!");
            }
        }

        if (OptionsUtil.UPDATER.getBooleanValue()) {
            new Updater();
        }


        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
            MinecraftUtils.debug(getName(), getVersion(), "This version of Minecraft is extremely outdated and support for it has reached its end of life. You will still be able to run VoteRewards on this Minecraft version(" + MinecraftUtils.MinecraftVersion.getCurrentVersionNameVtoLowerCase() + "). Please consider updating to give your players a better experience and to avoid issues that have long been fixed.");
        }

        SchedulerManager.getScheduler().runTaskLater(this.getClass(), () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                placeholdersAPI = new PAPI();
                if (placeholdersAPI.register())
                    MinecraftUtils.debug(getName(), getVersion(), "Hooked into PlaceholderAPI!");
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
                    default -> {
                        setEnabled(false);
                        throw new RuntimeException("Please use one of the available holograms implementations\nAvailable implementations: ProtocolLib(JavaPlugin) and HolographicDisplays");
                    }
                }

                holograms.setHook(true);

                if (holograms.isHooked()) {
                    if (data.get("Holograms") != null) {
                        Objects.requireNonNull(data.getConfigurationSection("Holograms")).getKeys(false)
                                .forEach(s -> holograms.create(s, data.getSerializable("Holograms." + s + ".location", MinecraftUtils.SerializableLocation.class),
                                        data.getString("Holograms." + s + ".type"), false));
                    }
                    MinecraftUtils.debug(getName(), getVersion(), holograms.getClass().getName() + " hooked - Holograms enabled!");
                }
            }
            if (Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
                Bukkit.getPluginManager().registerEvents(new AuthMe(), plugin);
                MinecraftUtils.debug(getName(), getVersion(), "Hooked into AuthMeReloaded!");
            }
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                noPlayerCharacterAPI = new NoPlayerCharacterAPI();
                noPlayerCharacterAPI.setHook(true);
                MinecraftUtils.debug(getName(), getVersion(), "ProtocolLib installed - NPCs enabled!");
            }
            try {
                setupDatabase();
            } catch (Exception throwable) {
                throwable.printStackTrace();
            }
        }, 1L);

        setupCommands();
    }

    public void onDisable() {
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            MinecraftUtils.debug(getName(), getVersion(), "onDisable() Thread ID: " + Thread.currentThread().getId());
        if (holograms.isHooked() && !holograms.getHologramMap().isEmpty())
            holograms.getHologramMap().forEach((name, hologram) -> holograms.remove(name, false));
        if (noPlayerCharacterAPI.isHooked() && !noPlayerCharacterAPI.getNPCMap().isEmpty())
            noPlayerCharacterAPI.getNPCMap().forEach((name, npcIntegerPair) -> noPlayerCharacterAPI.remove(name, false));
        Bukkit.getOnlinePlayers().forEach(player -> {
            UserVoteData userVoteData = UserVoteData.getUser(player.getUniqueId());
            userVoteData.save(false, new Utils.Callback<>() {
                @Override
                public Boolean onSuccess() {
                    if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                        MinecraftUtils.debug(getName(), getVersion(),
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
        });
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (placeholdersAPI.isRegistered()) {
                if (placeholdersAPI.unregister()) {
                    MinecraftUtils.debug(getName(), getVersion(), "Unhooked from PlaceholderAPI!");
                }
            }
        }
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
        unregisterCommands();
        SchedulerManager.getScheduler().cancelTasks(this.getClass());
    }


    /**
     * Setup database Values: File, MySQL, SQLite
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    private void setupDatabase() throws Exception {
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            MinecraftUtils.debug(getName(), getVersion(), "setupDatabase() Thread ID: " + Thread.currentThread().getId());
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
                    MinecraftUtils.debug(getName(), getVersion(), "Database: MySQL");
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
                    MinecraftUtils.debug(getName(), getVersion(), "Database: PostgreSQL");
                }
            }
            case "SQLite" -> {
                if (connection == null || connection.isClosed()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.SQLITE, getDataFolder().getAbsolutePath(), OptionsUtil.DATABASE_SQLITE.getStringValue());
                    connection = databaseWrapper.connect().getSQLConnection();
                    databaseWrapper.getSQLDatabase().createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    iDatabaseType = new UserVoteData.SQLUserUtils();
                    MinecraftUtils.debug(getName(), getVersion(), "Database: SQLite");
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
                MinecraftUtils.debug(getName(), getVersion(), "Database: MongoDB");
            }
            case "File" -> {
                databaseWrapper = null;
                iDatabaseType = new UserVoteData.FileUserUtils();
                MinecraftUtils.debug(getName(), getVersion(), "Database: File");
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
                userVoteData.load(new Utils.Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        UserVoteData.getAllUsersMap().append(userVoteData.user().getUniqueId(), userVoteData.user());
                        if (OptionsUtil.DEBUG_LOAD.getBooleanValue())
                            MinecraftUtils.debug(getName(), getVersion(), "Successfully loaded user " + userVoteData.user().getOfflinePlayer().getName());
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

        if (noPlayerCharacterAPI.isHooked())
            noPlayerCharacterAPI.updateAll();

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

    @NotNull
    public FileConfiguration getConfig() {
        return FileManager.getInstance().getConfig().getFileConfiguration();
    }

    public void saveConfig() {
        FileManager.getInstance().getConfig().saveFile();
    }

    public String getName() {
        return voteReward.getDesc().getName();
    }

    public String getVersion() {
        return voteReward.getDesc().getVersion();
    }

    public String getMain() {
        return voteReward.getDesc().getMain();
    }

    public @NotNull List<String> getAuthors() {
        return voteReward.getDesc().getAuthors();
    }

    public void saveResource(String resourcePath, boolean replace) {
        voteReward.saveResource(resourcePath, replace);
    }

    public void setEnabled(boolean enable) {
        voteReward.setEnable(enable);
    }

    private void setupCommands() {
        commandManager.enableUnstableAPI("help");

        loadCommandLocales(commandManager);

        if (OptionsUtil.COMMAND_FAKEVOTE.getBooleanValue())
            commandManager.registerCommand(new FakeVoteCommand());
        if (OptionsUtil.COMMAND_HOLOGRAM.getBooleanValue())
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
        if (OptionsUtil.COMMAND_NPC.getBooleanValue())
            commandManager.registerCommand(new NPCCommand());
    }

    private void unregisterCommands() {
        if (OptionsUtil.COMMAND_FAKEVOTE.getBooleanValue())
            commandManager.unregisterCommand(new FakeVoteCommand());
        if (OptionsUtil.COMMAND_HOLOGRAM.getBooleanValue())
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
        if (OptionsUtil.COMMAND_NPC.getBooleanValue())
            commandManager.unregisterCommand(new NPCCommand());
    }

    private void loadCommandLocales(@NotNull PaperCommandManager commandManager) {
        try {
            saveResource("lang_en.yaml", true);
            commandManager.getLocales().setDefaultLocale(Locale.ENGLISH);
            commandManager.getLocales().loadYamlLanguageFile(new File(getDataFolder(), "lang_en.yaml"), Locale.ENGLISH);
            commandManager.usePerIssuerLocale(true);
        } catch (IOException | InvalidConfigurationException e) {
            MinecraftUtils.debug(getName(), getVersion(), "Failed to load language config 'lang_en.yaml': " + e.getMessage());
            e.printStackTrace();
        }
    }

}
