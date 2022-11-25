package com.georgev22.voterewards;

import com.georgev22.library.extensions.Extension;
import com.georgev22.library.extensions.ExtensionDescriptionFile;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public interface VoteRewardImpl {

    File getDataFolder();

    Logger getLogger();

    Description getDesc();

    boolean setEnable(boolean enabled);

    boolean isEnabled();

    void saveResource(@NotNull String resourcePath, boolean replace);

    final class Description {

        private PluginDescriptionFile pluginDescriptionFile;

        private ExtensionDescriptionFile extensionDescriptionFile;

        protected Description(PluginDescriptionFile pluginDescriptionFile) {
            this.pluginDescriptionFile = pluginDescriptionFile;
        }

        protected Description(ExtensionDescriptionFile extensionDescriptionFile) {
            this.extensionDescriptionFile = extensionDescriptionFile;
        }

        public String getName() {
            return pluginDescriptionFile == null ? extensionDescriptionFile.getName() : pluginDescriptionFile.getName();
        }

        public String getVersion() {
            return pluginDescriptionFile == null ? extensionDescriptionFile.getVersion() : pluginDescriptionFile.getVersion();
        }

        public String getMain() {
            return pluginDescriptionFile == null ? extensionDescriptionFile.getMain() : pluginDescriptionFile.getMain();
        }

        public @NotNull List<String> getAuthors() {
            return pluginDescriptionFile == null ? extensionDescriptionFile.getAuthors() : pluginDescriptionFile.getAuthors();
        }
    }

}
