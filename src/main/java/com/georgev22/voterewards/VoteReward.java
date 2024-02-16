package com.georgev22.voterewards;

import co.aikar.commands.PaperCommandManager;
import com.georgev22.api.libraryloader.LibraryLoader;
import com.georgev22.api.libraryloader.annotations.MavenLibrary;
import com.georgev22.api.libraryloader.exceptions.InvalidDependencyException;
import com.georgev22.api.libraryloader.exceptions.UnknownDependencyException;
import com.georgev22.library.database.DatabaseType;
import com.georgev22.library.database.DatabaseWrapper;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.minecraft.inventory.PagedInventoryAPI;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.library.yaml.InvalidConfigurationException;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.library.yaml.file.YamlConfiguration;
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
import com.georgev22.voterewards.utilities.player.PlayerDataManager;
import com.georgev22.voterewards.utilities.player.User;
import com.georgev22.voterewards.utilities.player.UserTypeAdapter;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.georgev22.voterewards.votereward.VoteRewardImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "30.1.1-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
@MavenLibrary(groupId = "commons-io", artifactId = "commons-io", version = "2.11.0")
@MavenLibrary(groupId = "commons-codec", artifactId = "commons-codec", version = "1.15")
public class VoteReward {

    private PAPI placeholdersAPI = null;

    @Getter
    private DatabaseWrapper databaseWrapper = null;


    @Getter
    private PlayerDataManager playerDataManager;

    @Getter
    private Holograms holograms = new Holograms.HologramsNoop();

    @Getter
    @Setter
    private JavaPlugin plugin = null;

    @Getter
    private NoPlayerCharacterAPI noPlayerCharacterAPI = null;

    @Getter
    private static VoteReward instance = null;

    @Getter
    private FileManager fileManager;

    @Getter
    private final Logger logger;

    @Getter
    private final File dataFolder;

    private final VoteRewardImpl voteReward;

    private PaperCommandManager commandManager;

    @Getter
    private PagedInventoryAPI pagedInventoryAPI = null;

    private LibraryLoader libraryLoader;

    private int tick = 0;
    @Getter
    private Gson gson;

    public VoteReward(@NotNull VoteRewardImpl voteReward) {
        this.voteReward = voteReward;
        this.dataFolder = voteReward.getDataFolder();
        this.logger = voteReward.getLogger();
    }

    public void onLoad() throws UnknownDependencyException, InvalidDependencyException {
        instance = this;
        if (BukkitMinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(BukkitMinecraftUtils.MinecraftVersion.V1_16_R1)) {
            this.libraryLoader = new LibraryLoader(this.getClass().getClassLoader(), this.getDataFolder(), this.getLogger());
            this.libraryLoader.loadAll(this.getClass(), true);
        }
        ConfigurationSerialization.registerClass(BukkitMinecraftUtils.SerializableLocation.class);
    }

    public void onEnable() throws Exception {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new UserTypeAdapter())
                //.setPrettyPrinting()
                .create();
        fileManager = FileManager.getInstance();

        fileManager.loadFiles(this.logger, this.getClass());

        MessagesUtil.repairPaths(fileManager.getMessages());

        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            BukkitMinecraftUtils.debug(getName(), getVersion(), "onEnable() Thread ID: " + Thread.currentThread().getId());
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
            BukkitMinecraftUtils.debug(getName(), getVersion(), "onEnable() Thread ID: " + Thread.currentThread().getId());
        if (data.get("month") == null) {
            data.set("month", Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
            dataCFG.saveFile();
        }

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tick++;
            SchedulerManager.getScheduler().mainThreadHeartbeat(tick);
        }, 0, 1L);

        if (!Bukkit.getPluginManager().isPluginEnabled("Votifier") && !Bukkit.getPluginManager().isPluginEnabled("VotifierPlus")) {
            if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
                BukkitMinecraftUtils.debug(getName(), getVersion(), "Neither Votifier nor VotifierPlus is enabled.");
            return;
        }

        BukkitMinecraftUtils.registerListeners(plugin, new VotifierListener(), new PlayerListeners(), new DeveloperInformListener());
        pagedInventoryAPI = new PagedInventoryAPI(plugin);
        commandManager = new PaperCommandManager(plugin);

        if (!BukkitMinecraftUtils.MinecraftVersion.getCurrentVersion().isAboveOrEqual(BukkitMinecraftUtils.MinecraftVersion.UNKNOWN)) {
            if (YamlConfiguration.loadConfiguration(new File(new File(this.getDataFolder().getParentFile(), "bStats"), "config.yml")).getBoolean("enabled", true) & OptionsUtil.METRICS.getBooleanValue()) {
                new Metrics(plugin, 3179);
                BukkitMinecraftUtils.debug(getName(), getVersion(), "Metrics are enabled!");
            }
        }

        if (OptionsUtil.UPDATER.getBooleanValue()) {
            new Updater();
        }


        if (BukkitMinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(BukkitMinecraftUtils.MinecraftVersion.V1_12_R1)) {
            BukkitMinecraftUtils.debug(getName(), getVersion(), "This version of Minecraft is extremely outdated and support for it has reached its end of life. You will still be able to run VoteRewards on this Minecraft version(" + BukkitMinecraftUtils.MinecraftVersion.getCurrentVersionNameVtoLowerCase() + "). Please consider updating to give your players a better experience and to avoid issues that have long been fixed.");
        }

        SchedulerManager.getScheduler().runTaskLater(this.getClass(), () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                placeholdersAPI = new PAPI();
                if (placeholdersAPI.register())
                    BukkitMinecraftUtils.debug(getName(), getVersion(), "Hooked into PlaceholderAPI!");
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
                                .forEach(s -> holograms.create(s, data.getSerializable("Holograms." + s + ".location", BukkitMinecraftUtils.SerializableLocation.class),
                                        data.getString("Holograms." + s + ".type"), false));
                    }
                    BukkitMinecraftUtils.debug(getName(), getVersion(), holograms.getClass().getName() + " hooked - Holograms enabled!");
                }
            }
            if (Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
                Bukkit.getPluginManager().registerEvents(new AuthMe(), plugin);
                BukkitMinecraftUtils.debug(getName(), getVersion(), "Hooked into AuthMeReloaded!");
            }
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                noPlayerCharacterAPI = new NoPlayerCharacterAPI();
                noPlayerCharacterAPI.setHook(true);
                BukkitMinecraftUtils.debug(getName(), getVersion(), "ProtocolLib installed - NPCs enabled!");
            }
            try {
                setupDatabase();
            } catch (Exception throwable) {
                this.getLogger().log(Level.SEVERE, "Error while setting up the database:", throwable);
            }
        }, 1L);

        setupCommands();
    }

    public void onDisable() {
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            BukkitMinecraftUtils.debug(getName(), getVersion(), "onDisable() Thread ID: " + Thread.currentThread().getId());
        if (holograms.isHooked() && !holograms.getHologramMap().isEmpty())
            holograms.getHologramMap().forEach((name, hologram) -> holograms.remove(name, false));
        if (noPlayerCharacterAPI != null && noPlayerCharacterAPI.isHooked() && !noPlayerCharacterAPI.getNPCMap().isEmpty())
            noPlayerCharacterAPI.getNPCMap().forEach((name, npcIntegerPair) -> noPlayerCharacterAPI.remove(name, false));
        Bukkit.getOnlinePlayers().forEach(player -> {
            playerDataManager.getEntity(player.getUniqueId()).handle((userData, throwable) -> {
                if (throwable != null) {
                    this.getLogger().log(Level.SEVERE, "Error while disabling the plugin:", throwable);
                    return null;
                }
                return userData;
            }).thenAccept(userData -> {
                if (userData != null) {
                    playerDataManager.save(userData);
                    if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                        BukkitMinecraftUtils.debug(getName(), getVersion(),
                                VoteUtils.debugUserMessage(userData, "saved", true));
                    }
                }
            }).handle((unused, throwable) -> {
                if (throwable != null) {
                    this.getLogger().log(Level.SEVERE, "Error while disabling the plugin:", throwable);
                    return unused;
                }
                return unused;
            });
        });
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (placeholdersAPI.isRegistered()) {
                if (placeholdersAPI.unregister()) {
                    BukkitMinecraftUtils.debug(getName(), getVersion(), "Unhooked from PlaceholderAPI!");
                }
            }
        }
        if (databaseWrapper != null && databaseWrapper.isConnected()) {
            databaseWrapper.disconnect();
        }
        unregisterCommands();
        SchedulerManager.getScheduler().cancelTasks(this.getClass());
        if (this.libraryLoader != null) {
            try {
                this.libraryLoader.unloadAll();
            } catch (InvalidDependencyException e) {
                this.getLogger().log(Level.SEVERE, "Error:", e);
            }
        }
    }


    /**
     * Setup database Values: File, MySQL, SQLite
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    private void setupDatabase() throws Exception {
        if (OptionsUtil.DEBUG_OTHER.getBooleanValue())
            BukkitMinecraftUtils.debug(getName(), getVersion(), "setupDatabase() Thread ID: " + Thread.currentThread().getId());
        ObjectMap<String, ObjectMap.Pair<String, String>> map = new HashObjectMap<String, ObjectMap.Pair<String, String>>()
                .append("entity_id", ObjectMap.Pair.create("VARCHAR(38)", "NULL"))
                .append("data", ObjectMap.Pair.create("LONGTEXT", "NULL"));
        switch (OptionsUtil.DATABASE_TYPE.getStringValue()) {
            case "MySQL" -> {
                if (databaseWrapper == null || !databaseWrapper.isConnected()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.MYSQL,
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            this.getLogger());
                    this.sqlConnect(map);
                    BukkitMinecraftUtils.debug(getName(), getVersion(), "Database: MySQL");
                }
            }
            case "PostgreSQL" -> {
                if (databaseWrapper == null || !databaseWrapper.isConnected()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.POSTGRESQL,
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            this.getLogger());
                    this.sqlConnect(map);
                    BukkitMinecraftUtils.debug(getName(), getVersion(), "Database: PostgreSQL");
                }
            }
            case "SQLite" -> {
                if (this.databaseWrapper == null || !this.databaseWrapper.isConnected()) {
                    this.databaseWrapper = new DatabaseWrapper(DatabaseType.SQLITE,
                            getDataFolder().getAbsolutePath(),
                            0,
                            "",
                            "",
                            OptionsUtil.DATABASE_SQLITE.getStringValue(),
                            this.getLogger());
                    this.sqlConnect(map);
                    BukkitMinecraftUtils.debug(getName(), getVersion(), "Database: SQLite");
                }
            }
            case "MongoDB" -> {
                databaseWrapper = new DatabaseWrapper(DatabaseType.MONGO,
                        OptionsUtil.DATABASE_MONGO_HOST.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PORT.getIntValue(),
                        OptionsUtil.DATABASE_MONGO_USER.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PASSWORD.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue(),
                        this.getLogger());
                this.databaseWrapper.connect();
                playerDataManager = new PlayerDataManager(databaseWrapper, OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue());
                BukkitMinecraftUtils.debug(getName(), getVersion(), "Database: MongoDB");
            }
            case "File" -> {
                this.databaseWrapper = null;
                this.playerDataManager = new PlayerDataManager(new File(this.getDataFolder(), "userdata"), null);
                BukkitMinecraftUtils.debug(getName(), getVersion(), "Database: File");
            }
            default -> {
                setEnabled(false);
                throw new RuntimeException("Please use one of the available databases\nAvailable databases: File, MySQL, SQLite, PostgreSQL and MongoDB");
            }
        }

        this.playerDataManager.loadAll();

        Bukkit.getOnlinePlayers().forEach(player -> {
            this.playerDataManager.getEntity(player.getUniqueId()).handle((user, throwable) -> {
                if (throwable != null) {
                    voteReward.getLogger().log(Level.SEVERE, "Error while trying to get " + player.getName(), throwable);
                    return null;
                }
                return user;
            }).thenAccept(user -> {
                if (OptionsUtil.DEBUG_LOAD.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(getName(), getVersion(), "Successfully loaded user " + player.getName());
                }
            });
        });

        if (holograms.isHooked())
            holograms.updateAll();

        if (noPlayerCharacterAPI != null && noPlayerCharacterAPI.isHooked())
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
            this.getLogger().log(Level.SEVERE, "Error while trying to load command locales", e);
        }
    }

    private void sqlConnect(ObjectMap<String, ObjectMap.Pair<String, String>> map) throws SQLException, ClassNotFoundException {
        this.databaseWrapper.connect();
        Objects.requireNonNull(this.databaseWrapper.getSQLDatabase()).createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
        playerDataManager = new PlayerDataManager(databaseWrapper, OptionsUtil.DATABASE_TABLE_NAME.getStringValue());
    }

}
