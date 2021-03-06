package com.georgev22.voterewards.utilities.player;

import com.georgev22.externals.xseries.XMaterial;
import com.georgev22.externals.xseries.XSound;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.CFG;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Regions;
import com.georgev22.voterewards.utilities.Utils;
import com.georgev22.voterewards.utilities.interfaces.ObjectMap;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class VotePartyUtils {

    private static VotePartyUtils instance;

    public static VotePartyUtils getInstance() {
        return instance == null ? instance = new VotePartyUtils() : instance;
    }

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

    private final Set<OfflinePlayer> players = Sets.newHashSet();

    public void run(OfflinePlayer player, boolean start) {
        final FileManager fm = FileManager.getInstance();

        final FileConfiguration dataFile = fm.getData().getFileConfiguration();

        dataFile.set("VoteParty-Votes", dataFile.getInt("VoteParty-Votes", 0) + 1);
        fm.getData().saveFile();

        int maxVotes = OptionsUtil.VOTEPARTY_VOTES.getIntValue();
        int currentVotes = dataFile.getInt("VoteParty-Votes", 0);

        final ObjectMap<String, String> placeholders = ObjectMap.newHashObjectMap();
        if (!start) {
            if (!OptionsUtil.VOTEPARTY.isEnabled()) {
                return;
            }
            if (OptionsUtil.MESSAGE_VOTEPARTY.isEnabled())
                if (maxVotes - currentVotes > 0) {
                    placeholders.append("%votes%", Utils.formatNumber(maxVotes - currentVotes));
                    MessagesUtil.VOTEPARTY_VOTES_NEED.msgAll(placeholders, true);
                    placeholders.clear();
                }

            if (OptionsUtil.VOTEPARTY_PARTICIPATE.isEnabled()) {
                players.add(player);
            } else {
                this.players.addAll(Bukkit.getOnlinePlayers());
            }

            if (currentVotes < maxVotes) {
                return;

            }
        } else {
            players.addAll(Bukkit.getOnlinePlayers());
        }
        if (OptionsUtil.VOTEPARTY_COOLDOWN.isEnabled()) {
            placeholders.append("%secs%",
                    String.valueOf(OptionsUtil.VOTEPARTY_COOLDOWN_SECONDS.getLongValue()));
            MessagesUtil.VOTEPARTY_START.msgAll(placeholders, true);
            placeholders.clear();

            for (Player all : Bukkit.getOnlinePlayers())
                if (!players.contains(all)) {
                    MessagesUtil.VOTEPARTY_NOT_PARTICIPATED.msg(all);
                }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (OfflinePlayer offlinePlayer : players) {
                        if (OptionsUtil.VOTEPARTY_CRATE.isEnabled()) {
                            if (offlinePlayer != null && offlinePlayer.isOnline()) {
                                if (isInLocation(offlinePlayer.getPlayer().getLocation())) {
                                    UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
                                    userVoteData.setVoteParties(userVoteData.getVoteParty() + 1);
                                    MessagesUtil.VOTEPARTY_UNCLAIM.msg(offlinePlayer.getPlayer());
                                } else
                                    offlinePlayer.getPlayer().getInventory().addItem(crate(1));
                            }
                        } else {
                            chooseRandom(OptionsUtil.VOTEPARTY_RANDOM.isEnabled(), offlinePlayer);
                        }
                        if ((offlinePlayer != null && offlinePlayer.isOnline()) && OptionsUtil.VOTEPARTY_SOUND_START.isEnabled()) {
                            offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(), XSound
                                            .matchXSound(OptionsUtil.SOUND_VOTEPARTY_START.getStringValue()).get().parseSound(),
                                    1000, 1);
                        }
                    }
                    players.clear();
                }
            }.runTaskLaterAsynchronously(voteRewardPlugin, OptionsUtil.VOTEPARTY_COOLDOWN_SECONDS.getLongValue() * 20L);

        } else {
            for (OfflinePlayer offlinePlayer : players) {
                if (OptionsUtil.VOTEPARTY_CRATE.isEnabled()) {
                    if (offlinePlayer.getPlayer() != null) {
                        if (isInLocation(offlinePlayer.getPlayer().getLocation())) {
                            UserVoteData userVoteData = UserVoteData.getUser(offlinePlayer.getUniqueId());
                            userVoteData.setVoteParties(userVoteData.getVoteParty() + 1);
                            MessagesUtil.VOTEPARTY_UNCLAIM.msg(offlinePlayer.getPlayer());
                        } else
                            offlinePlayer.getPlayer().getInventory().addItem(crate(1));
                    }

                } else {
                    chooseRandom(OptionsUtil.VOTEPARTY_RANDOM.isEnabled(), offlinePlayer);
                }

            }
            players.clear();
        }

        if (!start) {
            dataFile.set("VoteParty-Votes", 0);
            fm.getData().saveFile();
        }
    }

    /**
     * Choose random voteparty rewards
     *
     * @param enable        The boolean of random rewards
     * @param offlinePlayer Player to give the rewards
     */
    public void chooseRandom(boolean enable, OfflinePlayer offlinePlayer) {
        List<String> list = OptionsUtil.VOTEPARTY_REWARDS.getStringList();
        if (enable) {
            Random random = new Random();
            int selector = random.nextInt(list.size());
            runCommands(list.get(selector).replace("%player%", offlinePlayer.getName()));
        } else {
            for (String s : list) {
                runCommands(s.replace("%player%", offlinePlayer.getName()));
            }
        }
    }

    public boolean isInLocation(Location location) {
        CFG cfg = FileManager.getInstance().getData();
        FileConfiguration data = cfg.getFileConfiguration();
        if (!OptionsUtil.VOTEPARTY_REGIONS.isEnabled()) {
            return false;
        }
        if (data.getConfigurationSection("Regions") == null || data.getConfigurationSection("Regions").getKeys(false).isEmpty()) {
            return false;
        }
        for (String s : data.getConfigurationSection("Regions").getKeys(false)) {
            Location a = (Location) data.get("Regions." + s + ".minimumPos");
            Location b = (Location) data.get("Regions." + s + ".maximumPos");
            Regions regions = new Regions(a, b);
            return regions.locationIsInRegion(location);
        }
        return false;
    }

    public ItemStack crate(int amount) {
        ItemStack itemStack = new ItemStack(
                Objects.requireNonNull(
                        XMaterial.matchXMaterial(
                                Objects.requireNonNull(OptionsUtil.VOTEPARTY_CRATE_ITEM.getStringValue())).get().parseMaterial()));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Utils.colorize(OptionsUtil.VOTEPARTY_CRATE_NAME.getStringValue()));
        itemMeta.setLore(Utils.colorize(OptionsUtil.VOTEPARTY_CRATE_LORES.getStringList()));
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(amount);
        return itemStack;
    }

    /**
     * Run the commands from config
     *
     * @param s Command to run
     */
    private void runCommands(String s) {
        Bukkit.getScheduler().runTask(VoteRewardPlugin.getInstance(), () -> {
            if (s == null)
                return;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.colorize(s));
        });
    }
}
