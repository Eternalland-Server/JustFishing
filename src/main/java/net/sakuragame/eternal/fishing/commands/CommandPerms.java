package net.sakuragame.eternal.fishing.commands;

import lombok.Getter;

public enum CommandPerms {

    USER("justfishing.user"),
    ADMIN("justfishing.admin");

    @Getter
    private final String node;

    CommandPerms(String node) {
        this.node = node;
    }
}