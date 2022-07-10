package com.georgev22.voterewards.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.api.minecraft.configmanager.CFG;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.Backup;
import com.georgev22.voterewards.utilities.player.UserVoteData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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

import static com.georgev22.api.utilities.Utils.*;

@CommandAlias("votereward|voterewards|vr")
public class VoteRewardsCommand extends Command {

    @Default
    @Description("{@@commands.descriptions.votereward.basic}")
    @CommandCompletion("backup|reload|help|clear|restore|set|region")
    @Syntax("<subcommand> <option1> <option2> <option3>...")
    @CommandPermission("voterewards.basic")
    public void execute(@NotNull final CommandSender sender, final String[] args) {
        MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
        MinecraftUtils.msg(sender, "&6/vr reload");
        MinecraftUtils.msg(sender, "&6/vr backup");
        MinecraftUtils.msg(sender, "&6/vr help [player, voteparty]");
        MinecraftUtils.msg(sender, "&c&l==============");
    }

    @HelpCommand
    @Subcommand("help")
    @CommandAlias("vhelp")
    @Description("{@@commands.descriptions.votereward.help}")
    @CommandCompletion("player|voteparty")
    @Override
    public void onHelp(final CommandSender sender, @NotNull CommandHelp commandHelp, final String @NotNull [] args) {
        if (args.length == 0) {
            MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            MinecraftUtils.msg(sender, "&6/vr reload");
            MinecraftUtils.msg(sender, "&6/vr backup");
            MinecraftUtils.msg(sender, "&6/vr help [player, voteparty]");
            MinecraftUtils.msg(sender, "&c&l==============");
            return;
        }
        if (args[0].equalsIgnoreCase("player")) {
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            MinecraftUtils.msg(sender, "&6/vr clear <player>");
            MinecraftUtils.msg(sender, "&6/vr set <player> <data> <value>");
            MinecraftUtils.msg(sender, "&6/vr region add/remove <regionName>");
            MinecraftUtils.msg(sender, "&c&l==============");
        } else if (args[0].equalsIgnoreCase("voteparty")) {
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, " ");
            MinecraftUtils.msg(sender, "&c&l(!)&c Commands&c &l(!)");
            MinecraftUtils.msg(sender, "&6/vp start");
            MinecraftUtils.msg(sender, "&6/vp claim");
            MinecraftUtils.msg(sender, "&6/vp give <player> <amount>");
            MinecraftUtils.msg(sender, "&c&l==============");
        }
    }

    @Subcommand("clear")
    @CommandAlias("vclear")
    @CommandCompletion("@players")
    @Description("{@@commands.descriptions.votereward.clear}")
    @Syntax("clear <player>")
    public void clear(@NotNull final CommandSender sender, final String player) {
        if (player == null) {
            MinecraftUtils.msg(sender, "&c&l(!)&c /vr clear <player>");
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
            MinecraftUtils.msg(sender, "&c&l(!)&c You cleared player " + target.getName());
        } else {
            MinecraftUtils.msg(sender, "&c&l(!)&c Player " + target.getName() + " doesn't exist");
        }
    }

    @Subcommand("set")
    @CommandAlias("vset")
    @Description("{@@commands.descriptions.votereward.set}")
    @CommandCompletion("@players votes|voteparty|time|dailyvotes|alltimevotes @range")
    @Syntax("set <player> <votes|voteparty|time|dailyvotes|alltimevotes> <values...>")
    public void set(final CommandSender sender, final String @NotNull [] args) {
        if (args.length <= 2) {
            MinecraftUtils.msg(sender, "&c&l(!)&c /vr set <player> <data> <value>!");
            MinecraftUtils.msg(sender, "&c&l(!)&c Data: vote voteparty time dailyvotes alltimevotes");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UserVoteData userVoteData = UserVoteData.getUser(target.getUniqueId());
        if (args[1].equalsIgnoreCase("votes")) {
            userVoteData.setVotes(Integer.parseInt(args[2]));
            MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " votes to " + args[2]);
        } else if (args[1].equalsIgnoreCase("voteparty")) {
            userVoteData.setVoteParties(Integer.parseInt(args[2]));
            MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " voteparty crates to " + args[2]);
        } else if (args[1].equalsIgnoreCase("time")) {
            userVoteData.setLastVoted(Long.parseLong(args[2]));
            MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " last time vote to " + args[2]);
        } else if (args[1].equalsIgnoreCase("dailyvotes")) {
            userVoteData.setDailyVotes(Integer.parseInt(args[2]));
            MinecraftUtils.msg(sender, "&a&l(!) &aSuccessfully set " + target.getName() + " daily votes to " + args[2]);
        } else {
            MinecraftUtils.msg(sender, "&c&l(!)&c /vr set <player> <data>!");
            MinecraftUtils.msg(sender, "&c&l(!)&c Data: vote voteparty time dailyvotes");
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
            MinecraftUtils.msg(sender, "&c&l(!)&c The server must be empty before backup starts!");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            MinecraftUtils.disallowLogin(true, "Backup ongoing!");
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
            new Backup("backup" + zonedDateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy--h-mm-a"))).backup(new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    MinecraftUtils.disallowLogin(false, "");
                    return true;
                }

                @Override
                public Boolean onFailure() {
                    return false;
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    MinecraftUtils.disallowLogin(false, "");
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
            MinecraftUtils.msg(sender, "&c&l(!)&c /vr restore <file name>");
            MinecraftUtils.msg(sender, "&c&l(!)&c Do not include file extension!");
            return;
        }
        MinecraftUtils.kickAll(voteRewardPlugin, "Restore started!");
        MinecraftUtils.disallowLogin(true, "Restore ongoing!");
        new Backup(fileName + ".yml").restore(new Callback<>() {
            @Override
            public Boolean onSuccess() {
                MinecraftUtils.disallowLogin(false, "");
                return true;
            }

            @Override
            public Boolean onFailure() {
                return false;
            }

            @Override
            public Boolean onFailure(Throwable throwable) {
                throwable.printStackTrace();
                MinecraftUtils.disallowLogin(false, "");
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
        MinecraftUtils.msg(sender, "&a&l(!) &aPlugin reloaded!");
    }

    @Subcommand("region")
    @CommandAlias("vregion")
    @Description("{@@commands.descriptions.votereward.region}")
    @CommandCompletion("add|remove <regionName>")
    @Syntax("region add|remove <regionName>")
    public void region(final CommandSender sender, final String @NotNull [] args) {
        if (args.length == 0) {
            MinecraftUtils.msg(sender, "&c&l(!)&c /vr region <add/remove> <name>");
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 1) {
                MinecraftUtils.msg(sender, "&c&l(!)&c /vr region add <name>");
                return;
            }

            if (!(sender instanceof Player player)) {
                MessagesUtil.ONLY_PLAYER_COMMAND.msg(sender);
                return;
            }
            WorldGuardWrapper worldGuardWrapper = WorldGuardWrapper.getInstance();
            Optional<ISelection> iSelection = worldGuardWrapper.getPlayerSelection(player);
            if (iSelection.isEmpty()) {
                MinecraftUtils.msg(player, "&c&l(!)&c Selection is empty");
                return;
            }
            ICuboidSelection selection = (ICuboidSelection) iSelection.get();
            Location a = selection.getMinimumPoint();
            Location b = selection.getMaximumPoint();
            String regionName = args[1];
            if (a == null || b == null) {
                MinecraftUtils.msg(sender, "&c&l(!)&c Please make a selection first!");
                return;
            }
            CFG cfg = FileManager.getInstance().getData();
            FileConfiguration data = cfg.getFileConfiguration();
            data.set("Regions." + regionName + ".minimumPos", a);
            data.set("Regions." + regionName + ".maximumPos", b);
            cfg.saveFile();
            MinecraftUtils.msg(sender, "&a&l(!) &aAdded Location \na: " + a.getX() + "," + a.getY() + "," + a.getZ()
                    + "\nb: " + b.getX() + "," + b.getY() + "," + b.getZ());
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                MinecraftUtils.msg(sender, "&c&l(!)&c /vr region remove <name>");
                return;
            }

            String regionName = args[1];
            CFG cfg = FileManager.getInstance().getData();
            FileConfiguration data = cfg.getFileConfiguration();

            data.set("Regions." + regionName, null);
            cfg.saveFile();

            MinecraftUtils.msg(sender, "&c&l(!)&c Location " + regionName + " removed!");
        }
    }

}
