package net.sakuragame.eternal.fishing.listener;

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent;
import com.taylorswiftcn.megumi.uifactory.generate.function.Statements;
import net.sakuragame.eternal.dragoncore.network.PacketSender;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.api.event.FishAirForceEvent;
import net.sakuragame.eternal.fishing.api.event.FishCaughtEvent;
import net.sakuragame.eternal.fishing.api.event.FishStoshUseUpEvent;
import net.sakuragame.eternal.fishing.core.FishManager;
import net.sakuragame.eternal.fishing.core.FishResult;
import net.sakuragame.eternal.fishing.core.Fishery;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
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
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player player = e.getPlayer();
        PlayerFishEvent.State state = e.getState();

        if (state == PlayerFishEvent.State.FISHING) {
            FishHook hook = e.getHook();

            if (!Utils.isFishRegion(player.getLocation())) {
                player.sendMessage(ConfigFile.prefix + "请前往钓鱼场钓鱼");
                e.setCancelled(true);
                return;
            }

            if (!FishManager.sitOnChair(player)) {
                player.sendMessage(ConfigFile.prefix + "只有坐在椅子上才能钓鱼，请右键选择一把椅子");
                e.setCancelled(true);
                return;
            }

            String rod = Utils.getPlayerHeldRod(player);
            if (rod == null) return;

            if (rod.equals("fishing_rod_normal")) {
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
            forbid.remove(uuid);
            if (Fishery.consumeStosh(player)) {
                Fishery.putAutoFishingTime(player);
                Statements restart = new Statements()
                        .add("func.Component_Set('goal', 'width', '0');")
                        .add("func.Component_Set('pointer', 'x', 'track.x');")
                        .add("func.Component_Set('icon', 'visible', '0');")
                        .add("func.Function_Async_Execute('progress');");
                PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, restart.build(), false);
            }
            else {
                FishStoshUseUpEvent useUpEvent = new FishStoshUseUpEvent(player);
                useUpEvent.call();
            }
        }, 30);
    }

    private void successDispose(Player player, int pos) {
        UUID uuid = player.getUniqueId();

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

        Bukkit.getScheduler().runTaskLaterAsynchronously(JustFish.getInstance(), () -> {
            forbid.remove(uuid);
            if (Fishery.consumeStosh(player)) {
                Fishery.startFishingTask(player);
            }
            else {
                FishStoshUseUpEvent useUpEvent = new FishStoshUseUpEvent(player);
                useUpEvent.call();
            }
        }, 30);
    }

    private void airForceDispose(Player player, int pos) {
        UUID uuid = player.getUniqueId();

        Fishery.stopFishingTask(player);

        Statements statements = new Statements()
                .add("func.Component_Set('result', 'x', '" + pos + "');")
                .add("func.Component_Set('icon', 'texture', '0,0,0,0');")
                .add("func.Component_Set('icon', 'visible', '1');");
        PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, statements.build(), false);

        FishAirForceEvent event = new FishAirForceEvent(player);
        event.call();

        Bukkit.getScheduler().runTaskLaterAsynchronously(JustFish.getInstance(), () -> {
            forbid.remove(uuid);
            if (Fishery.consumeStosh(player)) {
                Fishery.startFishingTask(player);
            }
            else {
                FishStoshUseUpEvent useUpEvent = new FishStoshUseUpEvent(player);
                useUpEvent.call();
            }
        }, 30);
    }
}
