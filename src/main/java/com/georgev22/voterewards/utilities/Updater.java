package com.georgev22.voterewards.utilities;

import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Updater {

    private final VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();
    private final String localVersion = voteRewardPlugin.getDescription().getVersion();
    private String onlineVersion;

    public Updater() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(voteRewardPlugin, () -> {
            voteRewardPlugin.getLogger().info("Checking for Updates ... ");
            try {
                onlineVersion = getOnlineVersion();
            } catch (Exception ex) {

                MinecraftUtils.debug(voteRewardPlugin, "Failed to check for an update on Git.", "Either Git or you are offline or are slow to respond.");

                ex.printStackTrace();

                return;

            }
            if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 0) {
                MinecraftUtils.debug(voteRewardPlugin, "You are running the newest build.");
            } else if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 1) {
                MinecraftUtils.debug(voteRewardPlugin,
                        "New stable version available!",
                        "Version: " + onlineVersion + ". You are running version: " + localVersion,
                        "Update at: https://github.com/GeorgeV220/VoteRewards/releases/");
            } else {
                MinecraftUtils.debug(voteRewardPlugin, "You are currently using the " + localVersion + " version which is under development.",
                        "Your version is " + localVersion,
                        "Latest released version is " + onlineVersion,
                        "If you have problems contact me on discord or github. Thank you for testing this version");
            }

        }, 20L, 20 * 7200);
    }

    public Updater(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(voteRewardPlugin, () -> {
            MinecraftUtils.msg(player, "&e&lUpdater &8» &6Checking for Updates ...");
            try {
                onlineVersion = getOnlineVersion();
            } catch (Exception ex) {
                MinecraftUtils.msg(player, "&e&lUpdater &8» &cFailed to check for an update on Git.");
                MinecraftUtils.msg(player, "&e&lUpdater &8» &cEither Git or you are offline or are slow to respond.");

                ex.printStackTrace();

                return;

            }
            if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 0) {
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6You are running the newest build.");
            } else if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 1) {
                MinecraftUtils.msg(player,
                        "&e&lUpdater &8» &6New version available!");
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Version: &c"
                        + onlineVersion + ". &6You are running version: &c" + localVersion);
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Update at: https://github.com/GeorgeV220/VoteRewards/releases/");

            } else {
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6You are currently using the &c" + localVersion + " &6version which is under development. If you have problems contact me on discord or github");
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Your version is &c" + localVersion);
                MinecraftUtils.msg(player, "&e&lUpdater &8» &6Latest released version is &c" + onlineVersion);
            }
        });
    }


    private int compareVersions(@NotNull String version1, @NotNull String version2) {
        if (version1.contains("alpha") | version1.contains("beta")) {
            return -1;
        }

        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++) {
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }

    private @NotNull String getOnlineVersion() throws IOException {
        System.setProperty("http.agent", "Chrome");
        HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.github.com/repos/GeorgeV220/VoteRewards/tags").openConnection();

        con.setDoOutput(true);

        con.setRequestMethod("GET");

        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        String jsonText = sb.toString();
        JsonElement jsonElement = JsonParser.parseString(jsonText);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        return jsonArray.get(0).getAsJsonObject().get("name").getAsString().replace("\"", "");
    }

}