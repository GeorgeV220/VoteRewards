package com.georgev22.voterewards.votereward;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public interface VoteRewardImpl {

    File getDataFolder();

    Logger getLogger();

    Description getDesc();

    void setEnable(boolean enabled);

    boolean isEnabled();

    void saveResource(@NotNull String resourcePath, boolean replace);

    final class Description {

        private final String name, version, main;
        private final List<String> authors;

        Description(String name, String version, String main, List<String> authors) {
            this.name = name;
            this.version = version;
            this.main = main;
            this.authors = authors;
        }

        public String getName() {
            return this.name;
        }

        public String getVersion() {
            return this.version;
        }

        public String getMain() {
            return this.main;
        }

        public @NotNull List<String> getAuthors() {
            return this.authors;
        }
    }

}
