package com.georgev22.voterewards.command;

import com.georgev22.voterewards.utilities.CustomData;

public class CommandContext {
    private final CustomData customData = new CustomData();

    public CustomData getData() {
        return customData;
    }
}