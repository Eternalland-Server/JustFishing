package net.sakuragame.eternal.fishing.listener;

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent;
import com.taylorswiftcn.megumi.uifactory.generate.function.Statements;
import ink.ptms.zaphkiel.ZaphkielAPI;
import net.sakuragame.eternal.dragoncore.network.PacketSender;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.api.event.FishAirForceEvent;
import net.sakuragame.eternal.fishing.api.event.FishCaughtEvent;
import net.sakuragame.eternal.fishing.api.event.FishStoshUseUpEvent;
import net.sakuragame.eternal.fishing.core.FishManager;
import net.sakuragame.eternal.fishing.core.FishResult;
import net.sakuragame.eternal.fishing.core.Fishery;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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

            if (!FishManager.sitOnChair(player)) {
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
            return;
        }

        if (state == PlayerFishEvent.State.CAUGHT_FISH || state == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Item item = (Item) e.getCaught();
            item.setItemStack(new ItemStack(Material.AIR));
        }

        Fishery.stopFishing(player);
    }

    @EventHandler
    public void onResult(UIFCompSubmitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!e.getScreenID().equals(Fishery.SCREEN_ID)) return;
        if (!e.getCompID().equals("result")) return;

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

        if (!Fishery.inFishing(player)) return;
        Fishery.stopFishingTask(player);

        Pair<String, ItemStack> caught = getCaughtFish(player);
        if (caught == null) return;

        String fishID = caught.getKey();
        ItemStack fishItem = caught.getValue();
        String texture = FishManager.getTexture(fishID);

        if (fishItem == null) return;

        Statements statements = new Statements()
                .add("func.Component_Set('icon', 'texture', '" + texture + "');")
                .add("func.Component_Set('icon', 'visible', '1');");
        PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, statements.build(), false);

        FishCaughtEvent event = new FishCaughtEvent(player, fishID, fishItem);
        event.call();

        Bukkit.getScheduler().runTaskLaterAsynchronously(JustFish.getInstance(), () -> {
            forbid.remove(uuid);
            if (Fishery.consumeStosh(player)) {
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

        Pair<String, ItemStack> caught = getCaughtFish(player);
        if (caught == null) return;

        String fishID = caught.getKey();
        ItemStack fishItem = caught.getValue();
        String texture = FishManager.getTexture(fishID);

        if (fishItem == null) return;

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

    private Pair<String, ItemStack> getCaughtFish(Player player) {
        String rod = Utils.getPlayerHeldRod(player);
        String useStosh = Fishery.getUseStosh(player);
        if (rod == null || useStosh == null) return null;

        String fish = FishManager.caughtFish(rod, useStosh);

        ItemStack item = ZaphkielAPI.INSTANCE.getItemStack(fish, player);
        int amount = FishManager.getFishMultiple(Fishery.getUseLicence(player));
        if (item == null) return null;

        item.setAmount(amount);

        return new Pair<>(fish, item);
    }
}
