package com.georgev22.voterewards.votereward;

import lombok.Getter;
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

    @Getter
    final class Description {

        private final String name, version, main;
        private final List<String> authors;

        Description(String name, String version, String main, List<String> authors) {
            this.name = name;
            this.version = version;
            this.main = main;
            this.authors = authors;
        }
    }

}
