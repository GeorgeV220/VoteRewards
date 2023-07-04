package com.georgev22.voterewards.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.Updater;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PlayerListeners implements Listener {

    private final VoteReward voteReward = VoteReward.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (BukkitMinecraftUtils.isLoginDisallowed()) {
            //noinspection deprecation
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, BukkitMinecraftUtils.colorize(BukkitMinecraftUtils.getDisallowLoginMessage()));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        voteReward.getPlayerDataManager().getEntity(event.getPlayer().getUniqueId()).handle((user, throwable) -> {
            if (throwable != null) {
                voteReward.getLogger().log(Level.SEVERE, "Error while trying to get " + event.getPlayer().getName() + " entity", throwable);
                return null;
            }
            return user;
        }).thenAccept(user -> {
            final Stopwatch sw = Stopwatch.createStarted();
            user.name(event.getPlayer().getName());
            //OFFLINE VOTING
            if (OptionsUtil.OFFLINE.getBooleanValue() && !Bukkit.getPluginManager().isPluginEnabled("AuthMeReloaded")) {
                for (String serviceName : user.services()) {
                    try {
                        new VoteUtils(user).processVote(serviceName, false);
                    } catch (IOException e) {
                        voteReward.getLogger().log(Level.SEVERE, "Error while trying to process " + event.getPlayer().getName(), e);
                    }
                }
                user.services(Lists.newArrayList());
            }

            voteReward.getPlayerDataManager().save(user).thenAccept(unused -> {
                if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                            VoteUtils.debugUserMessage(user, "saved", true));
                }
            });
            //HOLOGRAMS
            if (voteReward.getHolograms().isHooked()) {
                if (!voteReward.getHolograms().getHolograms().isEmpty()) {
                    for (Object hologram : voteReward.getHolograms().getHolograms()) {
                        voteReward.getHolograms().show(hologram, event.getPlayer());
                    }

                    voteReward.getHolograms().updateAll();
                }
            }
            final long elapsedMillis = sw.elapsed(TimeUnit.MILLISECONDS);
            if (OptionsUtil.DEBUG_LOAD.getBooleanValue()) {
                BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "Elapsed time to load user data: " + elapsedMillis);
            }
        });

        //UPDATER
        if (OptionsUtil.UPDATER.getBooleanValue()) {
            if (event.getPlayer().hasPermission("voterewards.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }

        if (OptionsUtil.REMINDER.getBooleanValue())
            VoteUtils.reminderMap.append(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        voteReward.getPlayerDataManager().getEntity(event.getPlayer().getUniqueId()).handle((user, throwable) -> {
            if (throwable != null) {
                voteReward.getLogger().log(Level.SEVERE, "Error while trying to save " + event.getPlayer().getName(), throwable);
                return null;
            }
            return user;
        }).thenAccept(user -> {
            if (user != null) {
                voteReward.getPlayerDataManager().save(user).thenAccept(unused -> {
                    if (OptionsUtil.REMINDER.getBooleanValue())
                        VoteUtils.reminderMap.remove(event.getPlayer());
                    if (OptionsUtil.DEBUG_SAVE.getBooleanValue()) {
                        BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(),
                                VoteUtils.debugUserMessage(user, "saved", true));
                    }
                });
            }
        });
    }


    private final Set<Action> clicks = EnumSet.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK,
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!clicks.contains(event.getAction())) {
            return;
        }

        final Player player = event.getPlayer();
        //noinspection deprecation
        final ItemStack item = player.getInventory().getItemInHand();

        if (item.getType() != XMaterial.matchXMaterial(Objects.requireNonNull(OptionsUtil.VOTEPARTY_CRATE_ITEM.getStringValue()))
                .orElseThrow()
                .parseMaterial()) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        if (!(meta != null && meta.hasDisplayName())) {
            return;
        }

        final String itemName = OptionsUtil.VOTEPARTY_CRATE_NAME.getStringValue();
        if (itemName == null) {
            return;
        }
        //noinspection deprecation
        if (!meta.getDisplayName().equals(BukkitMinecraftUtils.colorize(itemName))) {
            return;
        }

        final int amount = item.getAmount();

        if (amount == 1) {
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
        } else {
            item.setAmount(amount - 1);
        }

        VotePartyUtils.chooseRandom(player, OptionsUtil.VOTEPARTY_RANDOM.getBooleanValue());

        if (OptionsUtil.VOTEPARTY_SOUND_CRATE.getBooleanValue()) {
            if (BukkitMinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(BukkitMinecraftUtils.MinecraftVersion.V1_12_R1)) {
                Objects.requireNonNull(player.getPlayer()).playSound(player.getPlayer().getLocation(), Objects.requireNonNull(XSound
                                .matchXSound(OptionsUtil.SOUND_CRATE_OPEN.getStringValue()).orElseThrow().parseSound()),
                        1000, 1);
                if (OptionsUtil.DEBUG_OTHER.getBooleanValue()) {
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "========================================================");
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "SoundCategory doesn't exists in versions below 1.12");
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "SoundCategory doesn't exists in versions below 1.12");
                    BukkitMinecraftUtils.debug(voteReward.getName(), voteReward.getVersion(), "========================================================");
                }
            } else {
                Objects.requireNonNull(player.getPlayer()).playSound(player.getPlayer().getLocation(), Objects.requireNonNull(XSound
                                .matchXSound(OptionsUtil.SOUND_CRATE_OPEN.getStringValue()).orElseThrow().parseSound()),
                        org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_CRATE_OPEN_CHANNEL.getStringValue()),
                        1000, 1);
            }
        }

        event.setCancelled(true);

    }
}
