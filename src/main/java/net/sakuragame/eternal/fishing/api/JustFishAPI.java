package net.sakuragame.eternal.fishing.api;

import net.sakuragame.eternal.fishing.core.Fishery;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JustFishAPI {

    public static boolean inFishing(Player player) {
        return inFishing(player.getUniqueId());
    }

    public static boolean inFishing(UUID uuid) {
        return Fishery.inFishing(uuid);
    }
}
