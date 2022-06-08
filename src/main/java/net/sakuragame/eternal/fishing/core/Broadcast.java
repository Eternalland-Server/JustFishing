package net.sakuragame.eternal.fishing.core;

import net.sakuragame.eternal.justmessage.JustMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Broadcast {

    A(new String[] {"⒜ §a恭喜玩家 §7", "<player>", " §a钓起 ", "<item>", "§a ，大家一起祝贺TA吧！"}),
    B(new String[] {"⒝ §a恭喜玩家 §7", "<player>",  "§a钓起 ", "<item>", "§a ，大家一起祝贺TA吧！"}),
    C(new String[] {"⒠ §a恭喜玩家 §7", "<player>",  "§a钓起 ", "<item>", "§a ，大家一起祝贺TA吧！"}),
    D(new String[] {"⒟ §a恭喜玩家 §7", "<player>",  "§a人品爆发!钓起 ", "<item>", "§a ，大家一起祝贺TA吧！"});


    private final String[] args;

    Broadcast(String[] args) {
        this.args = args;
    }

    public void send(Player player, ItemStack item) {
        String[] params = this.args.clone();
        params[1] = player.getName();
        JustMessage.getChatManager().sendAll(item, params);
    }
}
