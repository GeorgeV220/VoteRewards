package com.georgev22.voterewards.command;

import com.georgev22.voterewards.utilities.UtilsX;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BukkitCommandIssuer implements CommandIssuer {

    private final CommandSender sender;

    public BukkitCommandIssuer(@NotNull CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean isPlayer() {
        return this.sender instanceof Player;
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.sender.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull Component component) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <T> @NotNull T getIssuer() {
        //noinspection unchecked
        return (T) this.sender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }

    @Override
    public boolean isOp() {
        return this.sender.isOp();
    }

    @Override
    public UUID getUniqueId() {
        if (isPlayer()) {
            return ((Player) this.sender).getUniqueId();
        } else {
            return UtilsX.generateUUID("VoidChestConsole");
        }
    }

    @Override
    public String getName() {
        return this.sender.getName();
    }
}
