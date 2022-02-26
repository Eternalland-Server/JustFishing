package net.sakuragame.eternal.fishing.core;

import lombok.Getter;

@Getter
public enum FishResult {

    AirForce(0),
    Success(1),
    Auto(2);

    private final int id;


    FishResult(int id) {
        this.id = id;
    }

    public static FishResult match(int id) {
        for (FishResult result : values()) {
            if (result.getId() != id) continue;

            return result;
        }

        return null;
    }

}
