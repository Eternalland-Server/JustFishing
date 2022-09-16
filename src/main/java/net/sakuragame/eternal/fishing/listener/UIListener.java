package net.sakuragame.eternal.fishing.listener;

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent;
import com.taylorswiftcn.megumi.uifactory.generate.function.Statements;
import net.sakuragame.eternal.dragoncore.network.PacketSender;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.api.event.FishAirForceEvent;
import net.sakuragame.eternal.fishing.api.event.FishCaughtEvent;
import net.sakuragame.eternal.fishing.api.event.FishBaitUseUpEvent;
import net.sakuragame.eternal.fishing.core.FishManager;
import net.sakuragame.eternal.fishing.core.FishResult;
import net.sakuragame.eternal.fishing.core.Fishery;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.util.Utils;
import net.sakuragame.eternal.justmessage.api.MessageAPI;
import net.sakuragame.eternal.kirratherm.KirraThermAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UIListener implements Listener {

    private final List<UUID> forbid = new ArrayList<>();

    @EventHandler
    public void onSwap(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        if (!Fishery.inFishing(player)) return;

        Fishery.stopFishing(player);
        MessageAPI.sendActionTip(player, "&c&l已停止钓鱼");
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        Inventory gui = e.getInventory();

        if (!(gui.getType() == InventoryType.PLAYER || gui.getType() == InventoryType.CRAFTING)) return;
        if (player.getInventory().getHeldItemSlot() != e.getSlot()) return;
        if (!Fishery.inFishing(player)) return;

        Fishery.stopFishing(player);
        player.sendTitle("§3收杆喽~", "§7已停止钓鱼", 5, 20, 5);

        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFish(PlayerFishEvent e) {
        Player player = e.getPlayer();
        PlayerFishEvent.State state = e.getState();

        if (e.isCancelled()) e.setCancelled(false);

        if (state == PlayerFishEvent.State.FISHING) {
            FishHook hook = e.getHook();

            boolean selfChair = KirraThermAPI.INSTANCE.isPlayerOnSeat(player);

            if (!(FishManager.sitOnChair(player) || selfChair)) {
                player.sendMessage(ConfigFile.prefix + "请右键选择选择一把公共椅子或者自带椅子进行钓鱼");
                e.setCancelled(true);
                return;
            }

            String rod = Utils.getPlayerHeldRod(player);
            if (rod == null) return;

            if (!selfChair) {
                if (!Fishery.manualFishing(player, hook)) {
                    e.setCancelled(true);
                    return;
                }
            }
            else {
                if (!Fishery.autoFishing(player, hook)) {
                    e.setCancelled(true);
                    return;
                }
            }

            player.sendTitle("", "§3§l开始钓鱼!", 5, 20, 5);
            return;
        }

        if (state == PlayerFishEvent.State.BITE) {
            e.setCancelled(true);
            return;
        }

        if (state == PlayerFishEvent.State.CAUGHT_FISH || state == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Item item = (Item) e.getCaught();
            item.setItemStack(new ItemStack(Material.AIR));
            return;
        }

        Fishery.stopFishing(player);
        player.sendTitle("§3收杆喽~", "§7已停止钓鱼", 5, 20, 5);
        forbid.remove(player.getUniqueId());
    }

    @EventHandler
    public void onResult(UIFCompSubmitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!e.getScreenID().equals(Fishery.SCREEN_ID)) return;
        if (!e.getCompID().equals("result")) return;

        if (!Fishery.inFishing(uuid)) return;
        if (forbid.contains(uuid)) return;

        FishResult result = FishResult.match(e.getParams().getParamI(0));
        if (result == null) return;

        forbid.add(uuid);

        // auto fishing
        if (result == FishResult.Auto) {
            autoDispose(player);
            return;
        }

        // manual fishing
        int pos = e.getParams().getParamI(1);

        if (result == FishResult.Success) {
            successDispose(player, pos);
            return;
        }

        if (result == FishResult.AirForce) {
            airForceDispose(player, pos);
        }
    }

    private void autoDispose(Player player) {
        UUID uuid = player.getUniqueId();
        if (!Fishery.inFishing(uuid)) return;

        long startTime = Fishery.getAutoFishingTime(player);
        if (startTime != 0) {
            long now = System.currentTimeMillis();
            if (now - startTime < ConfigFile.autoFishingTime * 1000L - 500) {
                forbid.remove(uuid);
                return;
            }
        }

        Pair<String, ItemStack> caught = Fishery.getCaughtFish(player);
        if (caught == null) return;

        String fishID = caught.getKey();
        ItemStack fishItem = caught.getValue();
        String texture = FishManager.getTexture(fishID);

        if (fishItem == null) return;

        player.getInventory().addItem(fishItem);

        Statements statements = new Statements()
                .add("func.Component_Set('icon', 'texture', '" + texture + "');")
                .add("func.Component_Set('icon', 'visible', '1');");
        PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, statements.build(), false);

        FishCaughtEvent event = new FishCaughtEvent(player, fishID, fishItem);
        event.call();

        Bukkit.getScheduler().runTaskLaterAsynchronously(JustFish.getInstance(), () -> {
            if (!Fishery.inFishing(uuid)) return;

            forbid.remove(uuid);
            if (Fishery.checkBait(player)) {
                Fishery.putAutoFishingTime(player);
                Statements restart = new Statements()
                        .add("func.Component_Set('goal', 'width', '0');")
                        .add("func.Component_Set('pointer', 'x', 'track.x');")
                        .add("func.Component_Set('icon', 'visible', '0');")
                        .add("func.Function_Async_Execute('progress');");
                PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, restart.build(), false);
            }
            else {
                FishBaitUseUpEvent useUpEvent = new FishBaitUseUpEvent(player);
                useUpEvent.call();
            }
        }, 30);
    }

    private void successDispose(Player player, int pos) {
        UUID uuid = player.getUniqueId();
        if (!Fishery.inFishing(uuid)) return;

        Fishery.stopFishingTask(player);

        Pair<String, ItemStack> caught = Fishery.getCaughtFish(player);
        if (caught == null) return;

        String fishID = caught.getKey();
        ItemStack fishItem = caught.getValue();
        String texture = FishManager.getTexture(fishID);

        if (fishItem == null) return;

        player.getInventory().addItem(fishItem);

        Statements statements = new Statements()
                .add("func.Component_Set('result', 'x', '" + pos + "');")
                .add("func.Component_Set('icon', 'texture', '" + texture + "');")
                .add("func.Component_Set('icon', 'visible', '1');");
        PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, statements.build(), false);

        FishCaughtEvent event = new FishCaughtEvent(player, fishID, fishItem);
        event.call();

        Bukkit.getScheduler().runTaskLater(JustFish.getInstance(), () -> {
            if (!Fishery.inFishing(uuid)) return;

            forbid.remove(uuid);
            if (Fishery.checkBait(player)) {
                Fishery.startFishingTask(player);
            }
            else {
                FishBaitUseUpEvent useUpEvent = new FishBaitUseUpEvent(player);
                useUpEvent.call();
            }
        }, 30);
    }

    private void airForceDispose(Player player, int pos) {
        UUID uuid = player.getUniqueId();
        if (!Fishery.inFishing(uuid)) return;

        Fishery.stopFishingTask(player);
        Fishery.consumeBait(player);

        Statements statements = new Statements()
                .add("func.Component_Set('result', 'x', '" + pos + "');")
                .add("func.Component_Set('icon', 'texture', '0,0,0,0');")
                .add("func.Component_Set('icon', 'visible', '1');");
        PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, statements.build(), false);

        FishAirForceEvent event = new FishAirForceEvent(player);
        event.call();

        Bukkit.getScheduler().runTaskLater(JustFish.getInstance(), () -> {
            if (!Fishery.inFishing(uuid)) return;

            forbid.remove(uuid);
            if (Fishery.checkBait(player)) {
                Fishery.startFishingTask(player);
            }
            else {
                FishBaitUseUpEvent useUpEvent = new FishBaitUseUpEvent(player);
                useUpEvent.call();
            }
        }, 30);
    }
}
