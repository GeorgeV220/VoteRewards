package com.georgev22.voterewards.utilities;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.utilities.Utils;
import com.georgev22.library.yaml.configmanager.CFG;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum MessagesUtil {

    NO_PERMISSION("Messages.No-Permission", "&c&l(!) &cYou do not have the correct permissions to do this!"),

    ONLY_PLAYER_COMMAND("Messages.Only-Player-Command", "&c&l(!) &cOnly players can run this command!"),

    VOTE("Messages.Vote", "&a&l(!) &7%player% &aThanks for vote on %servicename%"),

    REMINDER("Messages.Reminder", "&a&l(!) &7%player%&a please don't forget to vote for us today!"),

    VOTE_COMMAND("Messages.Vote-Command", "https://example.com", "%votes%"),

    VOTE_COMMAND_CUMULATIVE("Messages.Vote-Command-Cumulative", "&a&l(!) &7Votes until next cumulative reward: %votes%"),

    OFFLINE_PLAYER("Messages.Offline-Player", "&c&l(!) &cThis player is offline!"),

    VOTEPARTY("Messages.VoteParty", "&c&l(!) &aVote until VoteParty: %votes% (%current%/%need%"),

    VOTEPARTY_START("Messages.VoteParty-Start", "&a&l(!) &7A VoteParty will begin in %secs% seconds&3!"),

    VOTEPARTY_GIVE("Messages.VoteParty-Give", "&a&l(!) &7You just got %amount% VoteCrate"),

    VOTEPARTY_CLAIM("Messages.VoteParty-Claim", "&a&l(!) &7You have claimed %crates%!"),

    VOTEPARTY_NOTHING_TO_CLAIM("Messages.VoteParty-NothingToClaim", "&c&l(!) &cYou don't have voteparty rewards to claim!"),

    VOTEPARTY_UNCLAIMED("Messages.VoteParty-Unclaimed", "&c&l(!) &cYou have unclaimed voteparty rewards!"),

    VOTEPARTY_VOTES_NEED("Messages.VoteParty-VotesNeed", "&a&l(!) &7Votes needed for party: &f%votes%"),

    VOTEPARTY_NOT_PARTICIPATED("Messages.VoteParty-Participate",
            "&c&l(!) &cYou didn't vote to be able to participate in this VoteParty!"),

    VOTEPARTY_CRATE("Messages.VoteParty-CrateOpen", "&a&l(!) &7Your vote rewards have been applied!"),

    VOTEPARTY_CRATE_GIVE("Messages.VoteParty-CrateGive", "&a&l(!) &7A reward crate has been added to your inventory!"),

    VOTEPARTY_NOT_ENOUGH_PLAYERS("Messages.VoteParty-Not-Enough-Players", "&c&l(!)&c VoteParty cannot start without the minimum required players! (%required%)"),

    VOTEPARTY_WAITING_FOR_MORE_PLAYERS_PLACEHOLDER("Messages.VoteParty-Waiting-For-More-Players-Placeholder", "&c&l(!)&c Waiting for more players (%online%/%need%"),

    VOTEPARTY_PLAYERS_FULL_PLACEHOLDER("Messages-VoteParty-Players-Full-Placeholder", "%total%/%need% {%until%}"),

    VOTES("Messages.Votes", "=======", " %player% ", " %votes% ", "======="),

    VOTE_TOP_HEADER("Messages.VoteTop-Header", "&8[&aTop Voters&8]"),

    VOTE_TOP_BODY("Messages.VoteTop-Body", "%name% %votes%"),

    VOTE_TOP_FOOTER("Messages.VoteTop-Footer", "&8[&aTop Voters&8]"),

    VOTE_TOP_LINE("Messages.VoteTop-Line", " ", "===========", " "),

    REWARDS("Messages.Rewards", "&a&l(!) &aRewards:", "&a100$", "&a10 Diamonds"),

    VOTE_TITLE("Messages.Vote-Title", "&bThank your for your vote"),

    VOTE_SUBTITLE("Messages.Vote-Subtitle", "%player%"),

    ;

    /**
     * @see #getMessages()
     */
    private String[] messages;
    private final String path;

    MessagesUtil(final String path, final String... messages) {
        this.messages = messages;
        this.path = path;
    }

    /**
     * @return boolean - Whether the messages array contains more than 1
     * element. If true, it's more than 1 message/string.
     */
    private boolean isMultiLined() {
        return this.messages.length > 1;
    }

    /**
     * @param cfg CFG instance
     */
    public static void repairPaths(final CFG cfg) {

        boolean changed = false;

        for (MessagesUtil enumMessage : MessagesUtil.values()) {

            /* Does our file contain our path? */
            if (cfg.getFileConfiguration().contains(enumMessage.getPath())) {
                /* It does! Let's set our message to be our path. */
                setPathToMessage(cfg, enumMessage);
                continue;
            }

            /* Since the path doesn't exist, let's set our default message to that path. */
            setMessageToPath(cfg, enumMessage);
            if (!changed) {
                changed = true;
            }

        }
        /* Save the custom yaml file. */
        if (changed) {
            cfg.saveFile();
        }
    }

    /**
     * Sets a message from the MessagesX enum to the file.
     *
     * @param cfg         CFG instance
     * @param enumMessage Message
     */
    private static void setMessageToPath(final CFG cfg, final @NotNull MessagesUtil enumMessage) {
        /* Is our message multi lined? */
        if (enumMessage.isMultiLined()) {
            /* Set our message (array) to the path. */
            cfg.getFileConfiguration().set(enumMessage.getPath(), enumMessage.getMessages());
        } else {
            /* Set our message (string) to the path. */
            cfg.getFileConfiguration().set(enumMessage.getPath(), enumMessage.getMessages()[0]);
        }
    }

    /**
     * Sets the current MessagesX messages to a string/list retrieved from the
     * messages file.
     *
     * @param cfg         CFG instance
     * @param enumMessage Message
     */
    private static void setPathToMessage(final CFG cfg, final MessagesUtil enumMessage) {
        /* Is our path a list? */
        if (Utils.isList(cfg.getFileConfiguration(), enumMessage.getPath())) {
            /* Set our default message to be the path's message. */
            enumMessage.setMessages(
                    cfg.getFileConfiguration().getStringList(enumMessage.getPath()).toArray(new String[0]));
        } else {
            /* Set our default message to be the path's message. */
            enumMessage.setMessages(cfg.getFileConfiguration().getString(enumMessage.getPath()));
        }
    }

    /**
     * @return the path - The path of the enum in the file.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @return the messages - The messages array that contains all strings.
     */
    public String[] getMessages() {
        return this.messages;
    }

    /**
     * Sets the current messages to a different string array.
     *
     * @param messages The messages array that contains all strings.
     */
    public void setMessages(final String[] messages) {
        this.messages = messages;
    }

    /**
     * Sets the string message to a different string assuming that the array has
     * only 1 element.
     *
     * @param messages The message
     */
    public void setMessages(final String messages) {
        this.messages[0] = messages;
    }

    /**
     * @param target Message target
     * @see #msg(CommandSender, Map, boolean)
     */
    public void msg(final CommandSender target) {
        msg(target, new HashObjectMap<>(), false);
    }

    /**
     * Sends a translated message to a target commandsender with placeholders gained
     * from a map. If the map is null, no placeholder will be set, and it will still
     * execute.
     *
     * @param target     Message target
     * @param map        The Map with the placeholders
     * @param ignoreCase If you want to ignore case
     */
    public void msg(final CommandSender target, final Map<String, String> map, final boolean ignoreCase) {
        if (this.isMultiLined()) {
            BukkitMinecraftUtils.msg(target, this.getMessages(), map, ignoreCase);
        } else {
            BukkitMinecraftUtils.msg(target, this.getMessages()[0], map, ignoreCase);
        }
    }

    /**
     * Sends a translated message to a target commandsender with placeholders gained
     * from a map. If the map is null, no placeholder will be set, and it will still
     * execute.
     */
    public void msgAll() {
        if (this.isMultiLined()) {
            Bukkit.getOnlinePlayers().forEach(target -> BukkitMinecraftUtils.msg(target, this.getMessages()));
        } else {
            Bukkit.getOnlinePlayers().forEach(target -> BukkitMinecraftUtils.msg(target, this.getMessages()[0]));
        }
    }

    /**
     * Sends a translated message to a target with placeholders gained
     * from a map. If the map is null, no placeholder will be set, and it will still
     * execute.
     *
     * @param map        The placeholders map
     * @param ignoreCase If you want to ignore case
     */
    public void msgAll(final Map<String, String> map, final boolean ignoreCase) {
        if (this.isMultiLined()) {
            Bukkit.getOnlinePlayers().forEach(target -> BukkitMinecraftUtils.msg(target, this.getMessages(), map, ignoreCase));
        } else {
            Bukkit.getOnlinePlayers().forEach(target -> BukkitMinecraftUtils.msg(target, this.getMessages()[0], map, ignoreCase));
        }
    }

}
