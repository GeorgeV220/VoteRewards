package com.georgev22.voterewards;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.georgev22.voterewards.votereward.VoteRewardPlugin;
import org.junit.jupiter.api.*;

import java.util.Arrays;

public class VoteRewardTest {

    private static ServerMock serverMock;

    @BeforeAll
    public static void load() {
        serverMock = MockBukkit.mock();
        MockBukkit.createMockPlugin("Votifier");
        MockBukkit.load(VoteRewardPlugin.class);
        Arrays.stream(serverMock.getPluginManager().getPlugins()).forEach(plugin -> {
            serverMock.getLogger().info("Plugin: " + plugin.getName());
        });
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test some static Getters")
    void testGetters() {
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance());
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getFileManager());
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getPlugin());
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getDataFolder());
    }

    @Test
    @DisplayName("Verify that config files were loaded")
    void testConfigs() {
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getFileManager().getConfig());
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getFileManager().getVoteInventory());
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getFileManager().getData());
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getFileManager().getDiscord());
        Assertions.assertNotNull(VoteRewardPlugin.getVoteRewardInstance().getFileManager().getMessages());
    }

}
