package net.sakuragame.eternal.fishing.core;

import lombok.Getter;
import net.sakuragame.eternal.jungle.JungleAPI;
import net.sakuragame.eternal.jungle.stats.StatsType;

@Getter
public enum FishStats {

    Count("fish:count", "钓鱼总数");

    private final String identifier;
    private final String displayName;

    FishStats(String identifier, String displayName) {
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public StatsType getStatsType() {
        return JungleAPI.getStatsTypeManager().getStatsType(this.getIdentifier());
    }

    public static void register() {
        for (FishStats stats : values()) {
            JungleAPI.createStatsType(stats.getIdentifier(), stats.getDisplayName());
        }
    }
}
