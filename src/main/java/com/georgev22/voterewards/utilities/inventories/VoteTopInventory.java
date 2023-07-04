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
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class VoteTopInventory {

    private final VoteReward voteReward = VoteReward.getInstance();

    public void openTopPlayersInventory(@NotNull Player player, boolean allTimeVotes) {
        voteReward.getPlayerDataManager().getEntity(player.getUniqueId()).handle((user, throwable) -> {
            if (throwable != null) {
                voteReward.getLogger().log(Level.SEVERE, "Error while trying to get " + player.getName(), throwable);
                return null;
            }
            return user;
        }).thenAccept(user -> SchedulerManager.getScheduler().runTask(voteReward.getClass(), () -> {
            if (user != null) {
                List<NavigationItem> navigationItemList = Lists.newArrayList();
                final FileManager fileManager = FileManager.getInstance();

                ConfigurationSection navigationConfigurationSection = fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.navigation");
                if (navigationConfigurationSection == null) {
                    return;
                }
                for (String s : navigationConfigurationSection.getKeys(false)) {
                    ItemStack itemStack = ItemBuilder.buildItemFromConfig(
                                    fileManager.getVoteTopInventory().getFileConfiguration(),
                                    "custom item.navigation." + s,
                                    voteReward.getPagedInventoryAPI().getRegistrar().kryo())
                            .build();
                    CustomNavigationItem navigationItem = new CustomNavigationItem(itemStack, Integer.parseInt(s)) {
                        @Override
                        public void handleClick(PagedInventoryCustomNavigationHandler handler) {
                            for (String command : fileManager.getVoteTopInventory().getFileConfiguration().getStringList("custom item.navigation." + s + ".commands")) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", handler.getPlayer().getName()));
                            }
                        }
                    };
                    navigationItemList.add(navigationItem);
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
                                        navigationItemList.toArray(new NavigationItem[0])));

                List<Inventory> inventoryList = Lists.newArrayList();

                ObjectMap<Integer, ItemStack> objectMap = new HashObjectMap<>();

                ConfigurationSection customItemGuiConfigurationSection = fileManager.getVoteInventory().getFileConfiguration().getConfigurationSection("custom item.gui");
                if (customItemGuiConfigurationSection == null) {
                    return;
                }
                for (String s : customItemGuiConfigurationSection.getKeys(false)) {
                    ItemStack itemStack = ItemBuilder.buildItemFromConfig(
                                    fileManager.getVoteTopInventory().getFileConfiguration(),
                                    "custom item.gui." + s,
                                    voteReward.getPagedInventoryAPI().getRegistrar().kryo())
                            .build();
                    objectMap.append(Integer.parseInt(s), itemStack);
                }

                CustomItemInventory customItemInventory = new CustomItemInventory(BukkitMinecraftUtils.colorize(Objects.requireNonNull(fileManager.getVoteTopInventory().getFileConfiguration().getString("name"))), objectMap, 54);

                Inventory inventory = customItemInventory.getInventory();

                int i = 0;
                ObjectMap<String, Integer> stringIntegerObjectMap = allTimeVotes ? VoteUtils.getPlayersByAllTimeVotes() : VoteUtils.getPlayersByVotes();
                for (Map.Entry<String, Integer> entry : stringIntegerObjectMap.entrySet()) {
                    if (entry == null) {
                        continue;
                    }

                    if (inventoryList.isEmpty()) {
                        inventory = customItemInventory.getInventory();
                        inventoryList.add(inventory);
                    }

                    if (i > 45) {
                        i = 0;
                        inventory = customItemInventory.getInventory();
                        inventoryList.add(inventory);
                    }

                    for (Map.Entry<Integer, ItemStack> test : objectMap.entrySet()) {
                        if (inventory.getItem(i) != null && i == test.getKey()) {
                            i++;
                        }
                    }
                    inventory.setItem(i, ItemBuilder.buildItemFromConfig(
                                    fileManager.getVoteTopInventory().getFileConfiguration(),
                                    allTimeVotes ? "gui.player all" : "gui.player",
                                    new HashObjectMap<String, String>()
                                            .append("%votes%", String.valueOf(entry.getValue())),
                                    new HashObjectMap<String, String>()
                                            .append("%displayName%", entry.getKey()),
                                    voteReward.getPagedInventoryAPI().getRegistrar().kryo())
                            .skull(entry.getKey())
                            .build());

                    i++;
                }
                inventoryList.forEach(pagedInventory::addPage);
                pagedInventory.open(player, 0, fileManager.getVoteTopInventory().getFileConfiguration().getBoolean("animation.enabled"));
            }
        }));
    }

}
