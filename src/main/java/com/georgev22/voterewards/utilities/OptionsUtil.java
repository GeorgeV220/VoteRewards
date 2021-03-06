package com.georgev22.voterewards.utilities;

import com.georgev22.voterewards.configmanager.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum OptionsUtil {

    DEBUG_VOTE_PRE("debug.vote.preVote", false),

    DEBUG_VOTE_AFTER("debug.vote.afterVote", false),

    DEBUG_LOAD("debug.load", false),

    DEBUG_SAVE("debug.save", false),

    DEBUG_CREATE("debug.create", false),

    DEBUG_USELESS("debug.useless info", false),

    DEBUG_DELETE("debug.delete", false),

    DEBUG_VOTES_CUMULATIVE("debug.votes.cumulative", false),

    DEBUG_VOTES_DAILY("debug.votes.daily", false),

    DEBUG_VOTES_LUCKY("debug.votes.lucky", false),

    DEBUG_VOTES_REGULAR("debug.votes.regular", false),

    DEBUG_VOTES_WORLD("debug.votes.world", false),

    DEBUG_VOTES_PERMISSIONS("debug.votes.permissions", false),

    DEBUG_VOTES_OFFLINE("debug.votes.offline", false),

    VOTE_TITLE("title.vote", false),

    VOTEPARTY_TITLE("title.voteparty", false),

    UPDATER("updater", true),

    MONTHLY_ENABLED("monthly.enabled", false),

    MONTHLY_MINUTES("monthly.minutes", 30L),

    PURGE_ENABLED("purge.enabled", false),

    PURGE_DAYS("purge.days", 60L),

    PURGE_MINUTES("purge.minutes", 30L),

    DAILY("votes.daily.enabled", false),

    DAILY_HOURS("votes.daily.hours", 12),

    OFFLINE("votes.offline", false),

    PERMISSIONS("votes.permissions", false),

    WORLD("votes.world", false),

    DISABLE_SERVICES("votes.disable services", false),

    LUCKY("votes.lucky.enabled", false),

    LUCKY_NUMBERS("votes.lucky.numbers", 50),

    CUMULATIVE("votes.cumulative", false),

    CUMULATIVE_MESSAGE("votes.cumulative message", true),

    REMINDER("reminder.enabled", false),

    REMINDER_SEC("reminder.time", 120),

    SOUND("sound.vote", false),

    MESSAGE_VOTE("message.vote", false),

    MESSAGE_VOTEPARTY("message.voteparty", false),

    VOTETOP_HEADER("votetop.header", true),

    VOTETOP_LINE("votetop.line", false),

    VOTETOP_FOOTER("votetop.footer", true),

    VOTETOP_VOTERS("votetop.voters", 5),

    VOTETOP_GUI("votetop.gui.enabled", false),

    VOTETOP_GUI_TYPE("votetop.gui.type", "monthly"),

    VOTETOP_ALL_TIME_ENABLED("votetop.all time.enabled", false),

    VOTETOP_ALL_TIME_VOTERS("votetop.all time.voters", 3),

    COMMAND_REWARDS("commands.rewards", true),

    COMMAND_FAKEVOTE("commands.fakevote", true),

    COMMAND_VOTEPARTY("commands.voteparty", true),

    COMMAND_VOTES("commands.votes", true),

    COMMAND_VOTE("commands.vote", true),

    COMMAND_VOTETOP("commands.votetop", true),

    COMMAND_VOTEREWARDS("commands.voterewards", true),

    COMMAND_HOLOGRAM("commands.hologram", true),

    DATABASE_HOST("database.SQL.host", "localhost"),

    DATABASE_PORT("database.SQL.port", 3306),

    DATABASE_USER("database.SQL.user", "youruser"),

    DATABASE_PASSWORD("database.SQL.password", "yourpassword"),

    DATABASE_DATABASE("database.SQL.database", "VoteRewards"),

    DATABASE_TABLE_NAME("database.SQL.table name", "voterewards_users"),

    DATABASE_SQLITE("database.SQLite.file name", "voterewards"),

    DATABASE_MONGO_HOST("database.MongoDB.host", "localhost"),

    DATABASE_MONGO_PORT("database.MongoDB.port", 27017),

    DATABASE_MONGO_USER("database.MongoDB.user", "youruser"),

    DATABASE_MONGO_PASSWORD("database.MongoDB.password", "yourpassword"),

    DATABASE_MONGO_DATABASE("database.MongoDB.database", "VoteRewards"),

    DATABASE_MONGO_COLLECTION("database.MongoDB.collection", "voterewards_users"),

    DATABASE_TYPE("database.type", "File"),

    VOTEPARTY("voteparty.enabled", false),

    VOTEPARTY_PARTICIPATE("voteparty.participate", true),

    VOTEPARTY_CRATE("voteparty.crate.enabled", true),

    VOTEPARTY_RANDOM("voteparty.random rewards", true),

    VOTEPARTY_COOLDOWN("voteparty.cooldown.enabled", true),

    VOTEPARTY_COOLDOWN_SECONDS("voteparty.cooldown.seconds", 5L),

    VOTEPARTY_SOUND_START("sound.voteparty", false),

    VOTEPARTY_SOUND_CRATE("sound.crate", false),

    VOTEPARTY_REGIONS("voteparty.regions", false),

    VOTEPARTY_VOTES("voteparty.votes", 2),

    VOTEPARTY_BARS("voteparty.progress.bars", 10),

    VOTEPARTY_COMPLETE_COLOR("voteparty.progress.complete color", "&a"),

    VOTEPARTY_NOT_COMPLETE_COLOR("voteparty.progress.not complete color", "&c"),

    VOTEPARTY_BAR_SYMBOL("voteparty.progress.bar symbol", "|"),

    VOTEPARTY_CRATE_ITEM("voteparty.crate.item", "PISTON"),

    VOTEPARTY_CRATE_NAME("voteparty.crate.name", "&cVoteParty Crate"),

    VOTEPARTY_CRATE_LORES("voteparty.crate.lores", Collections.singletonList("&cPlace me")),

    VOTEPARTY_REWARDS("voteparty.rewards", Arrays.asList("eco give %player% 6547", "give %player% minecraft:nether_star 31")),

    SOUND_VOTE("sound.sounds.vote received", "NOTE_PIANO"),

    SOUND_CRATE_OPEN("sound.sounds.crate open", "CHEST_OPEN"),

    SOUND_VOTEPARTY_START("sound.sounds.voteparty start", "ENDERDRAGON_DEATH"),

    ;
    private final String pathName;
    private final Object value;

    OptionsUtil(final String pathName, final Object value) {
        this.pathName = pathName;
        this.value = value;
    }

    public boolean isEnabled() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getBoolean(getPath(), true);
    }

    public Object getObjectValue() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.get(getPath(), getDefaultValue());
    }

    public String getStringValue() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getString(getPath(), String.valueOf(getDefaultValue()));
    }

    public Long getLongValue() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getLong(getPath(), (Long) getDefaultValue());
    }

    public Integer getIntValue() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getInt(getPath(), (Integer) getDefaultValue());
    }

    public List<String> getStringList() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getStringList(getPath());
    }

    /**
     * Returns the path.
     *
     * @return the path.
     */
    public String getPath() {
        return "Options." + this.pathName;
    }

    /**
     * Returns the default value if the path have no value.
     *
     * @return the default value if the path have no value.
     */
    public Object getDefaultValue() {
        return value;
    }
}
