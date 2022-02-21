package net.sakuragame.eternal.fishing.api.event;

import org.bukkit.entity.Player;

public class FishSeatLeaveEvent extends JustEvent {

    public FishSeatLeaveEvent(Player who) {
        super(who);
    }
}
