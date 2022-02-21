package net.sakuragame.eternal.fishing.api.event;

import org.bukkit.entity.Player;

public class FishSeatEnterEvent {

    public static class Pre extends JustEvent {
        public Pre(Player who) {
            super(who);
        }
    }

    public static class Post extends JustEvent {
        public Post(Player who) {
            super(who);
        }
    }
}
