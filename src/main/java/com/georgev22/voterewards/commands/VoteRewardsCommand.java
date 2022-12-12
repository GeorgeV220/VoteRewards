package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.Backup;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;
import org.codemc.worldguardwrapper.selection.ISelection;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.georgev22.library.utilities.Utils.*;

@CommandAlias("votereward|voterewards|vr")
public class VoteRewardsCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.votereward.basic}")
    @CommandCompletion("backup|reload|help|clear|restore|set|region")
    @Syntax("<subcommand> <option1> <option2> <option3>...")
    @CommandPermission("voterewards.basic")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
        BukkitMinecraftUtils.msg(sender, "&6/vr reload");
        BukkitMinecraftUtils.msg(sender, "&6/vr backup");
        BukkitMinecraftUtils.msg(sender, "&6/vr help [player, voteparty]");
        BukkitMinecraftUtils.msg(sender, "&c&l==============");
    }

    @HelpCommand
    @Subcommand("help")
    @CommandAlias("vhelp")
    @Description("{@@commands.descriptions.votereward.help}")
    @CommandCompletion("player|voteparty")
    @Override
    public void onHelp(final CommandSender sender, @NotNull CommandHelp commandHelp, final String @NotNull [] args) {
        if (args.length == 0) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            BukkitMinecraftUtils.msg(sender, "&6/vr reload");
            BukkitMinecraftUtils.msg(sender, "&6/vr backup");
            BukkitMinecraftUtils.msg(sender, "&6/vr help [player, voteparty]");
            BukkitMinecraftUtils.msg(sender, "&c&l==============");
            return;
        }
        if (args[0].equalsIgnoreCase("player")) {
            BukkitMinecraftUtils.msg(sender, " ");
            BukkitMinecraftUtils.msg(sender, " ");
            BukkitMinecraftUtils.msg(sender, " ");
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            BukkitMinecraftUtils.msg(sender, "&6/vr clear <player>");
            BukkitMinecraftUtils.msg(sender, "&6/vr set <player> <data> <value>");
            BukkitMinecraftUtils.msg(sender, "&6/vr region add/remove <regionName>");
            BukkitMinecraftUtils.msg(sender, "&c&l==============");
        } else if (args[0].equalsIgnoreCase("voteparty")) {
            BukkitMinecraftUtils.msg(sender, " ");
            BukkitMinecraftUtils.msg(sender, " ");
            BukkitMinecraftUtils.msg(sender, " ");
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            BukkitMinecraftUtils.msg(sender, "&6/vp start");
            BukkitMinecraftUtils.msg(sender, "&6/vp claim");
            BukkitMinecraftUtils.msg(sender, "&6/vp give <player> <amount>");
            BukkitMinecraftUtils.msg(sender, "&c&l==============");
        }
    }

    @Subcommand("clear")
    @CommandAlias("vclear")
    @CommandCompletion("@players")
    @Description("{@@commands.descriptions.votereward.clear}")
    @Syntax("clear <player>")
    public void clear(@NotNull final CommandSender sender, final String player) {
        if (player == null) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c /vr clear <player>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(player);
        UserVoteData userVoteData = UserVoteData.getUser(target.getUniqueId());

        if (userVoteData.playerExists()) {
            try {
                userVoteData.reset(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c You cleared player " + target.getName());
        } else {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Player " + target.getName() + " doesn't exist");
        }
    }

    @Subcommand("set")
    @CommandAlias("vset")
    @Description("{@@commands.descriptions.votereward.set}")
    @CommandCompletion("@players votes|voteparty|time|dailyvotes|alltimevotes @range")
    @Syntax("set <player> <votes|voteparty|time|dailyvotes|alltimevotes> <values...>")
    public void set(final CommandSender sender, final String @NotNull [] args) {
        if (args.length <= 2) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c /vr set <player> <data> <value>!");
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Data: vote voteparty time dailyvotes alltimevotes");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserVoteData userVoteData = UserVoteData.getUser(target.getUniqueId());
        if (args[1].equalsIgnoreCase("votes")) {
            userVoteData.setVotes(Integer.parseInt(args[2]));
            BukkitMinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " votes to " + args[2]);
        } else if (args[1].equalsIgnoreCase("voteparty")) {
            userVoteData.setVoteParties(Integer.parseInt(args[2]));
            BukkitMinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " voteparty crates to " + args[2]);
        } else if (args[1].equalsIgnoreCase("time")) {
            userVoteData.setLastVoted(Long.parseLong(args[2]));
            BukkitMinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " last time vote to " + args[2]);
        } else if (args[1].equalsIgnoreCase("dailyvotes")) {
            userVoteData.setDailyVotes(Integer.parseInt(args[2]));
            BukkitMinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " daily votes to " + args[2]);
        } else {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c /vr set <player> <data>!");
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Data: vote voteparty time dailyvotes");
        }
        UserVoteData.getAllUsersMap().replace(target.getUniqueId(), userVoteData.user());
        userVoteData.save(true, new Callback<>() {
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

    @Subcommand("backup")
    @CommandAlias("vbackup|vbup")
    @Description("{@@commands.descriptions.votereward.backup}")
    @Syntax("backup")
    public void backup(final CommandSender sender) {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c The server must be empty before backup starts!");
            return;
        }
        SchedulerManager.getScheduler().runTaskAsynchronously(voteReward.getClass(), () -> {
            BukkitMinecraftUtils.disallowLogin(true, "Backup ongoing!");
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
            new Backup("backup" + zonedDateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy--h-mm-a"))).backup(new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    BukkitMinecraftUtils.disallowLogin(false, "");
                    return true;
                }

                @Override
                public Boolean onFailure() {
                    return false;
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    BukkitMinecraftUtils.disallowLogin(false, "");
                    return onFailure();
                }
            });
        });
    }

    @Subcommand("restore")
    @CommandAlias("vrestore|vre")
    @Description("{@@commands.descriptions.votereward.restore}")
    @Syntax("restore <filename>")
    public void restore(final CommandSender sender, final String fileName) {
        if (fileName == null) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c /vr restore <file name>");
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Do not include file extension!");
            return;
        }
        BukkitMinecraftUtils.kickAll(voteReward.getPlugin(), "Restore started!");
        BukkitMinecraftUtils.disallowLogin(true, "Restore ongoing!");
        new Backup(fileName + ".yml").restore(new Callback<>() {
            @Override
            public Boolean onSuccess() {
                BukkitMinecraftUtils.disallowLogin(false, "");
                return true;
            }

            @Override
            public Boolean onFailure() {
                return false;
            }

            @Override
            public Boolean onFailure(Throwable throwable) {
                throwable.printStackTrace();
                BukkitMinecraftUtils.disallowLogin(false, "");
                return onFailure();
            }
        });
    }

    @Subcommand("reload")
    @CommandAlias("vreload|vrl")
    @Description("{@@commands.descriptions.votereward.reload}")
    public void reload(final CommandSender sender) {
        final FileManager fm = FileManager.getInstance();
        fm.getConfig().reloadFile();
        fm.getMessages().reloadFile();
        fm.getVoteInventory().reloadFile();
        fm.getVoteTopInventory().reloadFile();
        fm.getDiscord().reloadFile();
        MessagesUtil.repairPaths(fm.getMessages());
        BukkitMinecraftUtils.msg(sender, "&a&l(!) &aPlugin reloaded!");
    }

    @Subcommand("region")
    @CommandAlias("vregion")
    @Description("{@@commands.descriptions.votereward.region}")
    @CommandCompletion("add|remove <regionName>")
    @Syntax("region add|remove <regionName>")
    public void region(final CommandSender sender, final String @NotNull [] args) {
        if (args.length == 0) {
            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c /vr region <add/remove> <name>");
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 1) {
                BukkitMinecraftUtils.msg(sender, "&c&l(!)&c /vr region add <name>");
                return;
            }

            if (!(sender instanceof Player player)) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                return;
            }
            WorldGuardWrapper worldGuardWrapper = WorldGuardWrapper.getInstance();
            Optional<ISelection> iSelection = worldGuardWrapper.getPlayerSelection(player);
            if (iSelection.isEmpty()) {
                BukkitMinecraftUtils.msg(player, "&c&l(!)&c Selection is empty");
                return;
            }
            ICuboidSelection selection = (ICuboidSelection) iSelection.get();
            BukkitMinecraftUtils.SerializableLocation a = new BukkitMinecraftUtils.SerializableLocation(selection.getMinimumPoint());
            BukkitMinecraftUtils.SerializableLocation b = new BukkitMinecraftUtils.SerializableLocation(selection.getMaximumPoint());
            String regionName = args[1];
            if (a == null || b == null) {
                BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Please make a selection first!");
                return;
            }
            CFG cfg = FileManager.getInstance().getData();
            FileConfiguration data = cfg.getFileConfiguration();
            data.set("Regions." + regionName + ".minimumPos", a);
            data.set("Regions." + regionName + ".maximumPos", b);
            cfg.saveFile();
            BukkitMinecraftUtils.msg(sender, "&a&l(!) &aAdded Location \na: " + a.getLocation().getX() + "," + a.getLocation().getY() + "," + a.getLocation().getZ()
                    + "\nb: " + b.getLocation().getX() + "," + b.getLocation().getY() + "," + b.getLocation().getZ());
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                BukkitMinecraftUtils.msg(sender, "&c&l(!)&c /vr region remove <name>");
                return;
            }

            String regionName = args[1];
            CFG cfg = FileManager.getInstance().getData();
            FileConfiguration data = cfg.getFileConfiguration();

            data.set("Regions." + regionName, null);
            cfg.saveFile();

            BukkitMinecraftUtils.msg(sender, "&c&l(!)&c Location " + regionName + " removed!");
        }
    }

}
