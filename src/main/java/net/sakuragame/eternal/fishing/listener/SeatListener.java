package net.sakuragame.eternal.fishing.listener;

import net.sakuragame.eternal.fishing.api.event.FishSeatEnterEvent;
import net.sakuragame.eternal.fishing.api.event.FishSeatLeaveEvent;
import net.sakuragame.eternal.fishing.core.Fishery;
import net.sakuragame.eternal.kirratherm.event.PlayerThermQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class SeatListener implements Listener {

    @EventHandler
    public void onPostEnter(FishSeatEnterEvent.Post e) {
        Player player = e.getPlayer();
        player.sendTitle("", "§3§l右键鱼竿开始钓鱼", 10, 20, 10);
    }

    @EventHandler
    public void onLeave(FishSeatLeaveEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Fishery.inFishing(uuid)) return;
        Fishery.stopFishing(player);
        Fishery.clearData(uuid);
    }

    @EventHandler
    public void onQuit(PlayerThermQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Fishery.inFishing(uuid)) return;
        Fishery.stopFishing(player);
        Fishery.clearData(uuid);
    }
}
