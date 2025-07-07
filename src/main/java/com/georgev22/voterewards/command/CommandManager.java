package com.georgev22.voterewards.command;

import com.georgev22.voterewards.command.annotation.CommandAlias;
import com.georgev22.voterewards.command.annotation.Subcommand;
import com.georgev22.voterewards.command.resolvers.ArgumentResolver;
import com.georgev22.voterewards.votereward.VoteRewardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

public class CommandManager {

    private final List<Consumer<CommandIssuer>> globalPreprocessors = new ArrayList<>();
    private final List<BiConsumer<CommandIssuer, CommandContext>> globalPostprocessors = new ArrayList<>();
    private final VoteRewardPlugin plugin = VoteRewardPlugin.getInstance();
    private final Map<String, ArgumentResolver> resolvers = new HashMap<>();
    private static CommandManager instance;

    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    private CommandManager() {
    }

    public void registerCommand(@NotNull BaseCommand command) {
        try {
            registerCommand0(command);

            for (Class<?> innerClass : command.getClass().getDeclaredClasses()) {
                if (!BaseCommand.class.isAssignableFrom(innerClass)) continue;
                if (!innerClass.isAnnotationPresent(Subcommand.class)) continue;

                BaseCommand subcommand = (BaseCommand) innerClass.getDeclaredConstructor().newInstance();
                command.addSubcommand(subcommand);

                if (innerClass.isAnnotationPresent(CommandAlias.class)) {
                    registerCommand0(subcommand);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register command: " + command.getClass().getName(), e);
        }
    }

    private void registerCommand0(@NotNull BaseCommand command) throws ReflectiveOperationException {
        registerWithBukkit(command);
    }

    private void registerWithBukkit(@NotNull BaseCommand command) throws ReflectiveOperationException {
        CommandMap commandMap;
        try {
            commandMap = plugin.getServer().getCommandMap();
        } catch (Exception e) {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        }

        for (String alias : command.getAliases(false)) {
            PluginCommandWrapper wrapper = new PluginCommandWrapper(command, alias);
            commandMap.register(plugin.getName(), wrapper);
        }
    }

    public void addGlobalPreprocessor(Consumer<CommandIssuer> preprocessor) {
        globalPreprocessors.add(preprocessor);
    }

    public void addGlobalPostprocessor(BiConsumer<CommandIssuer, CommandContext> postprocessor) {
        globalPostprocessors.add(postprocessor);
    }

    public List<BiConsumer<CommandIssuer, CommandContext>> getGlobalPostprocessors() {
        return globalPostprocessors;
    }

    public List<Consumer<CommandIssuer>> getGlobalPreprocessors() {
        return globalPreprocessors;
    }

    public ArgumentResolver getResolver(@NotNull String key) {
        return resolvers.get(key.toLowerCase());
    }

    public void addResolver(@NotNull String key, ArgumentResolver resolver) {
        resolvers.put(key.toLowerCase(), resolver);
    }
}