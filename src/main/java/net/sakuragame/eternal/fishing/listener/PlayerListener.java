package net.sakuragame.eternal.fishing.listener;

import net.sakuragame.eternal.fishing.api.event.FishSeatEnterEvent;
import net.sakuragame.eternal.fishing.api.event.FishSeatLeaveEvent;
import net.sakuragame.eternal.fishing.core.FishManager;
import net.sakuragame.eternal.fishing.core.Fishery;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        Fishery.clearData(uuid);
        FishManager.removeChair(uuid);
    }

    @EventHandler
    public void onInteractStair(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block chairBlock = e.getClickedBlock();
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        if (chairBlock == null) return;
        if (!ConfigFile.pubChair.contains(chairBlock.getType().name())) return;
        if (FishManager.inChair(player)) return;

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
        if (!e.isSneaking()) return;
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
