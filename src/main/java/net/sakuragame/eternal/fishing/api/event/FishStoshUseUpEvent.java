package net.sakuragame.eternal.fishing.api.event;

import org.bukkit.entity.Player;

public class FishStoshUseUpEvent extends JustEvent {
    public FishStoshUseUpEvent(Player who) {
        super(who);
    }
}
