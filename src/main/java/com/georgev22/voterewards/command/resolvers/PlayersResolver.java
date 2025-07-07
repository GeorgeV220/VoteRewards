package com.georgev22.voterewards.command.resolvers;

import com.georgev22.voterewards.command.CommandIssuer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class PlayersResolver implements ArgumentResolver {
    @Override
    public List<String> resolve(@NotNull CommandIssuer commandIssuer, String... args) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
