package com.georgev22.voterewards.command.commands;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.library.yaml.file.FileConfiguration;
import com.georgev22.voterewards.command.CommandContext;
import com.georgev22.voterewards.command.CommandIssuer;
import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.CommandCompletion;
import com.georgev22.voterewards.command.annotation.Permission;
import com.georgev22.voterewards.command.annotation.Subcommand;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.Backup;
import com.georgev22.voterewards.utilities.player.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.util.logging.Level;

@CommandAlias({"votereward", "voterewards", "vr"})
@Permission("voterewards.admin")
public class VoteRewardsCommand extends VoteRewardsBaseCommand {

    public VoteRewardsCommand() {
        addSubcommand(new HelpSubCommand());
        addSubcommand(new ClearSubCommand());
        addSubcommand(new SetSubCommand());
        addSubcommand(new BackupSubCommand());
        addSubcommand(new RestoreSubCommand());
        addSubcommand(new ReloadSubCommand());
        addSubcommand(new RegionSubCommand());
    }

    @Override
    protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Commands&c &l(!)");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr reload");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr backup");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr help [player, voteparty]");
        BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l==============");
    }

    @Subcommand("help")
    @CommandCompletion("player|voteparty")
    @Permission("voterewards.admin.help")
    public static class HelpSubCommand extends VoteRewardsBaseCommand {

        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (args.length == 0) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Commands&c &l(!)");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr reload");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr backup");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr help [player, voteparty]");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l==============");
                return;
            }
            if (args[0].equalsIgnoreCase("player")) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), " ");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), " ");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), " ");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Commands&c &l(!)");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr clear <player>");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr set <player> <data> <value>");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vr region add/remove <regionName>");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l==============");
            } else if (args[0].equalsIgnoreCase("voteparty")) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), " ");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), " ");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), " ");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Commands&c &l(!)");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vp start");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vp claim");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&6/vp give <player> <amount>");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l==============");
            }
        }
    }

    @Subcommand("clear")
    @CommandCompletion("@players")
    @Permission("voterewards.admin.clear")
    public static class ClearSubCommand extends VoteRewardsBaseCommand {

        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            String player = args.length > 0 ? args[0] : null;
            if (player == null) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /vr clear <player>");
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(player);
            voteReward.getPlayerDataManager().exists(target.getUniqueId()).thenAccept(aBoolean -> {
                if (aBoolean) {
                    voteReward.getPlayerDataManager().getEntity(target.getUniqueId()).handle((userData, throwable) -> {
                        if (throwable != null) {
                            voteReward.getLogger().log(Level.SEVERE, "Error while trying to reset player data " + player, throwable);
                            return null;
                        }
                        return userData;
                    }).thenApply(userData -> {
                        if (userData != null) {
                            User.reset(userData, true);
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c You cleared player " + target.getName());
                            return userData;
                        }
                        return null;
                    }).handle((userData, throwable) -> {
                        if (throwable != null) {
                            voteReward.getLogger().log(Level.SEVERE, "Error while trying to reset player data " + player, throwable);
                            return null;
                        }
                        return userData;
                    });
                } else {
                    BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Player " + target.getName() + " doesn't exist");
                }
            });
        }
    }

    @Subcommand("set")
    @CommandCompletion("@players votes|voteparty|time|dailyvotes|alltimevotes @range")
    @Permission("voterewards.admin.set")
    public static class SetSubCommand extends VoteRewardsBaseCommand {

        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (args.length <= 2) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /vr set <player> <data> <value>!");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Data: vote voteparty time dailyvotes alltimevotes");
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            voteReward.getPlayerDataManager().exists(target.getUniqueId()).thenAccept(aBoolean ->
                    voteReward.getPlayerDataManager().getEntity(target.getUniqueId()).thenAccept(userData -> {
                        if (args[1].equalsIgnoreCase("votes")) {
                            userData.votes(Integer.parseInt(args[2]));
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " votes to " + args[2]);
                        } else if (args[1].equalsIgnoreCase("voteparty")) {
                            userData.voteparty(Integer.parseInt(args[2]));
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " voteparty crates to " + args[2]);
                        } else if (args[1].equalsIgnoreCase("time")) {
                            userData.lastVote(Long.parseLong(args[2]));
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " last time vote to " + args[2]);
                        } else if (args[1].equalsIgnoreCase("dailyvotes")) {
                            userData.dailyVotes(Integer.parseInt(args[2]));
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aSuccessfully set " + target.getName() + " daily votes to " + args[2]);
                        } else {
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /vr set <player> <data>!");
                            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Data: vote voteparty time dailyvotes");
                        }
                        voteReward.getPlayerDataManager().save(userData);
                    }));
        }
    }

    @Subcommand("backup")
    @Permission("voterewards.admin.backup")
    public static class BackupSubCommand extends VoteRewardsBaseCommand {
        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c The server must be empty before backup starts!");
                return;
            }
            SchedulerManager.getScheduler().runTaskAsynchronously(voteReward.getClass(), () -> {
                BukkitMinecraftUtils.disallowLogin(true, "Backup ongoing!");
                ZonedDateTime zonedDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
                new Backup("backup" + zonedDateTime.format(DateTimeFormatter.ofPattern("MM-dd-yyyy--h-mm-a"))).backup();
            });
        }
    }

    @Subcommand("restore")
    @Permission("voterewards.admin.restore")
    public static class RestoreSubCommand extends VoteRewardsBaseCommand {
        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            String fileName = args.length > 0 ? args[0] : null;
            if (fileName == null) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /vr restore <file name>");
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Do not include file extension!");
                return;
            }
            BukkitMinecraftUtils.kickAll(voteReward.getPlugin(), "Restore started!");
            BukkitMinecraftUtils.disallowLogin(true, "Restore ongoing!");
            new Backup(fileName + ".yml").restore();
        }
    }

    @Subcommand("reload")
    @Permission("voterewards.admin.reload")
    public static class ReloadSubCommand extends VoteRewardsBaseCommand {
        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            final FileManager fm = FileManager.getInstance();
            fm.getConfig().reloadFile();
            fm.getMessages().reloadFile();
            fm.getVoteInventory().reloadFile();
            fm.getVoteTopInventory().reloadFile();
            fm.getDiscord().reloadFile();
            MessagesUtil.repairPaths(fm.getMessages());
            BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aPlugin reloaded!");
        }
    }

    @Subcommand("region")
    @CommandCompletion("add|remove <regionName>")
    @Permission("voterewards.admin.region")
    public static class RegionSubCommand extends VoteRewardsBaseCommand {
        @Override
        protected void handle(@NotNull CommandIssuer commandIssuer, String @NotNull [] args, @NotNull CommandContext context) {
            if (args.length == 0) {
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /vr region <add/remove> <name>");
                return;
            }

            if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 1) {
                    BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /vr region add <name>");
                    return;
                }

                if (!commandIssuer.isPlayer()) {
                    MessagesUtil.ONLY_PLAYER_COMMAND.msg(commandIssuer.getIssuer());
                    return;
                }
                Player player = commandIssuer.getIssuer();
                WorldGuardWrapper worldGuardWrapper = WorldGuardWrapper.getInstance();
                Optional<ISelection> iSelection = worldGuardWrapper.getPlayerSelection(player);
                if (iSelection.isEmpty()) {
                    BukkitMinecraftUtils.msg(player, "&c&l(!)&c Selection is empty");
                    return;
                }
                ICuboidSelection selection = (ICuboidSelection) iSelection.get();
                if (selection.getMinimumPoint() == null || selection.getMaximumPoint() == null) {
                    BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Please make a selection first!");
                    return;
                }
                BukkitMinecraftUtils.SerializableLocation a = new BukkitMinecraftUtils.SerializableLocation(selection.getMinimumPoint());
                BukkitMinecraftUtils.SerializableLocation b = new BukkitMinecraftUtils.SerializableLocation(selection.getMaximumPoint());
                String regionName = args[1];

                CFG cfg = FileManager.getInstance().getData();
                FileConfiguration data = cfg.getFileConfiguration();
                data.set("Regions." + regionName + ".minimumPos", a);
                data.set("Regions." + regionName + ".maximumPos", b);
                cfg.saveFile();
                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&a&l(!) &aAdded Location \na: " + a.getLocation().getX() + "," + a.getLocation().getY() + "," + a.getLocation().getZ()
                        + "\nb: " + b.getLocation().getX() + "," + b.getLocation().getY() + "," + b.getLocation().getZ());
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (args.length == 1) {
                    BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c /vr region remove <name>");
                    return;
                }

                String regionName = args[1];
                CFG cfg = FileManager.getInstance().getData();
                FileConfiguration data = cfg.getFileConfiguration();

                data.set("Regions." + regionName, null);
                cfg.saveFile();

                BukkitMinecraftUtils.msg(commandIssuer.getIssuer(), "&c&l(!)&c Location " + regionName + " removed!");
            }
        }
    }
}
