package com.georgev22.voterewards.utilities.configmanager;

import com.georgev22.library.yaml.configmanager.CFG;
import com.georgev22.voterewards.VoteReward;

import java.io.File;
import java.util.logging.Logger;

public final class FileManager {

    private static FileManager instance;

    public static FileManager getInstance() {
        return instance == null ? instance = new FileManager() : instance;
    }

    private final VoteReward voteReward = VoteReward.getInstance();

    private CFG config;
    private CFG data;
    private CFG messages;
    private CFG voteInventory;
    private CFG voteTopInventory;
    private CFG discord;

    private FileManager() {
    }

    public void loadFiles(Logger logger, Class<?> clazz) throws Exception {
        this.messages = new CFG("messages", voteReward.getDataFolder(), false, false, logger, clazz);
        this.config = new CFG("config", voteReward.getDataFolder(), true, true, logger, clazz);
        this.data = new CFG("data", voteReward.getDataFolder(), true, false, logger, clazz);
        this.discord = new CFG("discord", voteReward.getDataFolder(), true, true, logger, clazz);
        File inventoryFolder = new File(voteReward.getDataFolder(), "inventories");
        if (inventoryFolder.exists()) {
            inventoryFolder.mkdirs();
        }
        this.voteInventory = new CFG("vote", inventoryFolder, true, true, logger, clazz);
        this.voteTopInventory = new CFG("votetop", inventoryFolder, true, true, logger, clazz);
    }

    public CFG getMessages() {
        return messages;
    }

    public CFG getConfig() {
        return config;
    }

    public CFG getData() {
        return data;
    }

    public CFG getVoteInventory() {
        return voteInventory;
    }

    public CFG getVoteTopInventory() {
        return voteTopInventory;
    }

    public CFG getDiscord() {
        return discord;
    }
}
