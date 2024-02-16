package com.georgev22.voterewards.utilities.inventories;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.minecraft.inventory.CustomItemInventory;
import com.georgev22.library.minecraft.inventory.IPagedInventory;
import com.georgev22.library.minecraft.inventory.ItemBuilder;
import com.georgev22.library.minecraft.inventory.NavigationRow;
import com.georgev22.library.minecraft.inventory.handlers.PagedInventoryCustomNavigationHandler;
import com.georgev22.library.minecraft.inventory.navigationitems.*;
import com.georgev22.library.scheduler.SchedulerManager;
import com.georgev22.library.yaml.ConfigurationSection;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@Beta
public class VoteInventory {

    private final VoteReward voteReward = VoteReward.getInstance();

    public void openInventory(@NotNull Player player) {
        voteReward.getPlayerDataManager().getEntity(player.getUniqueId()).handle((user, throwable) -> {
            if (throwable != null) {
                voteReward.getLogger().log(Level.SEVERE, "Error while trying to get " + player.getName(), throwable);
                return null;
            }
            return user;
        }).thenAccept(user -> SchedulerManager.getScheduler().runTask(voteReward.getClass(), () -> {
            if (user != null) {
                List<NavigationItem> navigationItemList = Lists.newArrayList();
                ObjectMap<Integer, ItemStack> objectMap = new HashObjectMap<>();

                final FileManager fileManager = FileManager.getInstance();

                ConfigurationSection navigationConfigurationSection = fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.navigation");
                if (navigationConfigurationSection == null) {
                    return;
                }
                for (String s : navigationConfigurationSection.getKeys(false)) {
                    ItemStack itemStack = ItemBuilder.buildItemFromConfig(
                                    fileManager.getVoteInventory().getFileConfiguration(),
                                    "custom item.navigation." + s,
                                    voteReward.getPagedInventoryAPI().getRegistrar().kryo())
                            .build();
                    CustomNavigationItem navigationItem = new CustomNavigationItem(itemStack, Integer.parseInt(s)) {
                        @Override
                        public void handleClick(PagedInventoryCustomNavigationHandler handler) {
                            for (String command : fileManager.getVoteInventory().getFileConfiguration().getStringList("custom item.navigation." + s + ".commands")) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", handler.getPlayer().getName()));
                            }
                        }
                    };
                    navigationItemList.add(navigationItem);
                }


                ConfigurationSection customItemGuiConfigurationSection = fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.gui");
                if (customItemGuiConfigurationSection == null) {
                    return;
                }
                for (String s : customItemGuiConfigurationSection.getKeys(false)) {
                    String itemServiceName = fileManager.getVoteInventory().getFileConfiguration().getString("custom item.gui." + s + ".service name");
                    if (!user.servicesLastVote().containsKey(itemServiceName)) {
                        user.servicesLastVote(user.servicesLastVote().append(itemServiceName, 0L));
                        voteReward.getPlayerDataManager().save(user);
                    }
                    ZonedDateTime zonedDateTime = Instant.ofEpochMilli(
                                    user.servicesLastVote().get(itemServiceName))
                            .atZone(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));

                    ItemStack itemStack = ItemBuilder.buildItemFromConfig(fileManager.getVoteInventory().getFileConfiguration(), "custom item.gui." + s,
                            new HashObjectMap<String, String>()
                                    .append("%check%", user.servicesLastVote().getLong(itemServiceName) + (OptionsUtil.DAILY_HOURS.getIntValue() * 3600000) >= System.currentTimeMillis() ? "You can't vote" : "You can vote")
                                    .append("%lastVoteInTimeMillis%", String.valueOf(user.servicesLastVote().getLong(itemServiceName)))
                                    .append("%lastVote%", String.valueOf(zonedDateTime))
                                    .append("%lastVoteDay%", String.valueOf(zonedDateTime.getDayOfMonth()))
                                    .append("%lastVoteMonthName%", zonedDateTime.getMonth().name())
                                    .append("%lastVoteMonth%", String.valueOf(zonedDateTime.getMonthValue()))
                                    .append("%lastVoteYear%", String.valueOf(zonedDateTime.getYear()))
                                    .append("%lastVoteHour%", String.valueOf(zonedDateTime.getHour()))
                                    .append("%lastVoteMinute%", String.valueOf(zonedDateTime.getMinute()))
                                    .append("%lastVoteSecond%", String.valueOf(zonedDateTime.getSecond())),
                            new HashObjectMap<>(),
                            voteReward.getPagedInventoryAPI().getRegistrar().kryo()).build();
                    objectMap.append(Integer.parseInt(s), itemStack);
                }


                IPagedInventory pagedInventory = voteReward.getPagedInventoryAPI()
                        .createPagedInventory(
                                new NavigationRow(
                                        new NextNavigationItem(
                                                ItemBuilder.buildItemFromConfig(
                                                        fileManager.getVoteInventory().getFileConfiguration(),
                                                        "navigation.next",
                                                        voteReward.getPagedInventoryAPI().getRegistrar().kryo()
                                                ).build(),
                                                fileManager.getVoteInventory().getFileConfiguration().getInt("navigation.next.slot", 6)
                                        ),
                                        new PreviousNavigationItem(
                                                ItemBuilder.buildItemFromConfig(
                                                        fileManager.getVoteInventory().getFileConfiguration(),
                                                        "navigation.back",
                                                        voteReward.getPagedInventoryAPI().getRegistrar().kryo()
                                                ).build(),
                                                fileManager.getVoteInventory().getFileConfiguration().getInt("navigation.back.slot", 2)
                                        ),
                                        new CloseNavigationItem(
                                                ItemBuilder.buildItemFromConfig(
                                                        fileManager.getVoteInventory().getFileConfiguration(),
                                                        "navigation.cancel",
                                                        voteReward.getPagedInventoryAPI().getRegistrar().kryo()
                                                ).build(),
                                                fileManager.getVoteInventory().getFileConfiguration().getInt("navigation.cancel.slot", 4)
                                        ),
                                        navigationItemList.toArray(new NavigationItem[0])),
                                fileManager.getVoteInventory().getFileConfiguration().getBoolean("animation.enabled")
                        );

                CustomItemInventory customItemInventory = new CustomItemInventory(
                        BukkitMinecraftUtils.colorize(
                                Objects.requireNonNull(
                                        fileManager.getVoteInventory().getFileConfiguration().getString("name")
                                )
                        ),
                        objectMap,
                        54);

                Inventory inventory = customItemInventory.getInventory();

                pagedInventory.addPage(inventory);

                pagedInventory.open(player, 0);
            }
        }));
    }

}
