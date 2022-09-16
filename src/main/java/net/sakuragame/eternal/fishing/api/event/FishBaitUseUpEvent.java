package net.sakuragame.eternal.fishing.api.event;

import org.bukkit.entity.Player;

public class FishBaitUseUpEvent extends JustEvent {
    public FishBaitUseUpEvent(Player who) {
        super(who);
    }
}
