package net.sakuragame.eternal.fishing.listener;

import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.api.JustFishAPI;
import net.sakuragame.eternal.fishing.api.event.FishSeatEnterEvent;
import net.sakuragame.eternal.fishing.api.event.FishSeatLeaveEvent;
import net.sakuragame.eternal.fishing.core.FishAccount;
import net.sakuragame.eternal.fishing.core.FishManager;
import net.sakuragame.eternal.fishing.core.Fishery;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onFist(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        UUID uuid = e.getUniqueId();
        JustFish.getFishManager().loadAccount(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSecond(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        JustFish.getFishManager().removeAccount(e.getUniqueId());
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        FishAccount account = JustFishAPI.getAccount(player);

        if (account == null) return;

        e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        e.setKickMessage("账户数据加载有误，请重新进入游戏。");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        Fishery.clearData(uuid);
        FishManager.removeChair(uuid);
        JustFish.getFishManager().removeAccount(uuid);
    }

    @EventHandler
    public void onInteractStair(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block chairBlock = e.getClickedBlock();
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK) return;

        if (chairBlock == null) return;
        if (!chairBlock.getType().name().endsWith("_STAIRS")) return;

        FishSeatEnterEvent.Pre preEvent = new FishSeatEnterEvent.Pre(player);
        preEvent.call();
        if (preEvent.isCancelled()) return;

        FishManager.removeChair(player.getUniqueId());

        Vector seatPos = new Vector(0.5, 0.2, 0.5);
        Location seat = chairBlock.getLocation();

        Entity entity = player.getWorld().spawn(
                seat.clone().add(seatPos),
                ArmorStand.class,
                stand -> {
                    stand.setVisible(false);
                    stand.setGravity(false);
                    stand.setInvulnerable(true);
                    stand.setMarker(true);
                    stand.setCollidable(false);
                }
        );

        entity.addPassenger(player);
        FishManager.setChair(player, entity);

        e.setCancelled(true);

        FishSeatEnterEvent.Post postEvent = new FishSeatEnterEvent.Post(player);
        postEvent.call();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        FishManager.removeChair(player.getUniqueId());

        FishSeatLeaveEvent event = new FishSeatLeaveEvent(player);
        event.call();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        if (!Fishery.inFishing(player)) return;

        e.setCancelled(true);
    }
}
