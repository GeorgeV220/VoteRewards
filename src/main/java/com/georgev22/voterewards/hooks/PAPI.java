package com.georgev22.voterewards.hooks;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.utilities.Utils;
import com.georgev22.voterewards.VoteReward;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.OptionsUtil;
import com.georgev22.voterewards.utilities.configmanager.FileManager;
import com.georgev22.voterewards.utilities.player.VotePartyUtils;
import com.georgev22.voterewards.utilities.player.VoteUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PAPI extends PlaceholderExpansion {

    private final VoteReward voteReward = VoteReward.getInstance();

    @Override
    public @NotNull String getIdentifier() {
        return "voterewards";
    }

    @Override
    public String getRequiredPlugin() {
        return "VoteRewards";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GeorgeV22, Shin1gamiX, Antares (GalaxyEaterGR)";
    }

    @Override
    public @NotNull String getVersion() {
        return voteReward.getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        if (StringUtils.startsWithIgnoreCase(identifier, "top_playerName_")) {
            return VoteUtils.getTopPlayer(Integer.parseInt(identifier.split("_")[2]) - 1);
        }

        if (StringUtils.startsWithIgnoreCase(identifier, "top_playerVotes_")) {
            UUID targetUUID = Bukkit.getOfflinePlayer(VoteUtils.getTopPlayer(Integer.parseInt(identifier.split("_")[2]) - 1)).getUniqueId();
            return String.valueOf(
                    voteReward.getPlayerDataManager().getLoadedEntities().entrySet().stream()
                            .filter(uuidUserEntry -> uuidUserEntry.getKey().equals(targetUUID))
                            .map(Map.Entry::getValue)
                            .findFirst().orElse(voteReward.getPlayerDataManager().getEntity(targetUUID).join())
                            .votes()
            );
        }

        if (identifier.equalsIgnoreCase("player_votes")) {
            return String.valueOf(
                    voteReward.getPlayerDataManager().getLoadedEntities().entrySet().stream()
                            .filter(uuidUserEntry -> uuidUserEntry.getKey().equals(offlinePlayer.getUniqueId()))
                            .map(Map.Entry::getValue)
                            .findFirst().orElse(voteReward.getPlayerDataManager().getEntity(offlinePlayer.getUniqueId()).join())
                            .votes()
            );
        }
        if (identifier.equalsIgnoreCase("player_all_time_votes")) {
            return String.valueOf(
                    voteReward.getPlayerDataManager().getLoadedEntities().entrySet().stream()
                            .filter(uuidUserEntry -> uuidUserEntry.getKey().equals(offlinePlayer.getUniqueId()))
                            .map(Map.Entry::getValue)
                            .findFirst().orElse(voteReward.getPlayerDataManager().getEntity(offlinePlayer.getUniqueId()).join())
                            .totalVotes()
            );
        }
        final FileManager fm = FileManager.getInstance();
        if (identifier.equalsIgnoreCase("voteparty_total_votes")) {
            return String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes"));
        }

        if (identifier.equalsIgnoreCase("voteparty_votes_until")) {
            return String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                    - fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0));
        }
        if (identifier.equalsIgnoreCase("voteparty_votes_need")) {
            return OptionsUtil.VOTEPARTY_VOTES.getStringValue();
        }

        if (identifier.equalsIgnoreCase("voteparty_votes_full")) {
            return OptionsUtil.VOTEPARTY_PLAYERS.getBooleanValue() & VotePartyUtils.isWaitingForPlayers() ? BukkitMinecraftUtils.colorize(Utils.placeHolder(
                    MessagesUtil.VOTEPARTY_WAITING_FOR_MORE_PLAYERS_PLACEHOLDER.getMessages()[0],
                    new HashObjectMap<String, String>()
                            .append("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                            .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_PLAYERS_NEED.getIntValue())), true)) : BukkitMinecraftUtils.colorize(Utils.placeHolder(
                    MessagesUtil.VOTEPARTY_PLAYERS_FULL_PLACEHOLDER.getMessages()[0],
                    new HashObjectMap<String, String>()
                            .append("%until%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue()
                                    - fm.getData().getFileConfiguration().getInt("VoteParty-Votes", 0)))
                            .append("%total%", String.valueOf(fm.getData().getFileConfiguration().getInt("VoteParty-Votes")))
                            .append("%need%", String.valueOf(OptionsUtil.VOTEPARTY_VOTES.getIntValue())), true));
        }

        if (identifier.equalsIgnoreCase("voteparty_bar")) {
            return BukkitMinecraftUtils.getProgressBar(
                    fm.getData().getFileConfiguration().getInt("VoteParty-Votes"),
                    OptionsUtil.VOTEPARTY_VOTES.getIntValue(),
                    OptionsUtil.VOTEPARTY_BARS.getIntValue(),
                    OptionsUtil.VOTEPARTY_BAR_SYMBOL.getStringValue(),
                    OptionsUtil.VOTEPARTY_COMPLETE_COLOR.getStringValue(),
                    OptionsUtil.VOTEPARTY_NOT_COMPLETE_COLOR.getStringValue());
        }

        return null;
    }

}