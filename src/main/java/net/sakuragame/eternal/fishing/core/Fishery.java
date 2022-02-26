package net.sakuragame.eternal.fishing.core;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import ink.ptms.zaphkiel.ZaphkielAPI;
import net.sakuragame.eternal.dragoncore.config.FolderType;
import net.sakuragame.eternal.dragoncore.network.PacketSender;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.core.task.ManualFishing;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.util.HookUtils;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Fishery {

    private final static JustFish plugin = JustFish.getInstance();
    private final static Map<UUID, ManualFishing> scheduled = new HashMap<>();

    private final static Map<UUID, String> useStosh = new HashMap<>();
    private final static Map<UUID, String> useLicence = new HashMap<>();

    private final static Map<UUID, Long> autoFishingTime = new HashMap<>();

    private final static List<UUID> inFishing = new ArrayList<>();

    public final static String SCREEN_ID = "fish";

    public static boolean manualFishing(Player player, FishHook hook) {
        if (consumeStosh(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String state = HookUtils.getState(hook);
                    if (state == null) {
                        cancel();
                        return;
                    }

                    if (!state.equals("BOBBING")) return;

                    startManualFishing(player);
                    cancel();
                }
            }.runTaskTimerAsynchronously(JustFish.getInstance(), 0, 2);
            return true;
        }
        else {
            player.sendMessage(ConfigFile.prefix + "鱼饵不足，请将鱼饵放在鱼竿右边一格开始钓鱼!");
            return false;
        }
    }

    public static boolean autoFishing(Player player, FishHook hook) {
        if (consumeStosh(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String state = HookUtils.getState(hook);
                    if (state == null) {
                        cancel();
                        return;
                    }

                    if (!state.equals("BOBBING")) return;

                    putAutoFishingTime(player);
                    startAutoFishing(player);
                    cancel();
                }
            }.runTaskTimerAsynchronously(JustFish.getInstance(), 0, 2);

            return true;
        }
        else {
            player.sendMessage(ConfigFile.prefix + "鱼饵不足，请将鱼饵放在鱼竿右边一格开始钓鱼!");
            return false;
        }
    }

    public static void startFishingTask(Player player) {
        int goal = Utils.randomInt(70);
        ManualFishing task = new ManualFishing(player, goal, 20);
        task.runTaskTimer(plugin, 10, 10);

        scheduled.put(player.getUniqueId(), task);
    }

    public static void stopFishingTask(Player player) {
        stopFishingTask(player.getUniqueId());
    }

    public static void stopFishingTask(UUID uuid) {
        ManualFishing task = scheduled.remove(uuid);
        if (task == null) return;
        task.cancel();
    }

    public static void putAutoFishingTime(Player player) {
        autoFishingTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static long getAutoFishingTime(Player player) {
        return autoFishingTime.getOrDefault(player.getUniqueId(), 0L);
    }

    public static boolean consumeStosh(Player player) {
        int i = player.getInventory().getHeldItemSlot();
        if (i == 8) return false;

        int rawSlot = i + 1;

        ItemStack item = player.getInventory().getItem(rawSlot);
        if (MegumiUtil.isEmpty(item)) return false;

        String stosh = Utils.getZapItemID(item);
        if (stosh == null) return false;
        if (!ConfigFile.stosh.contains(stosh)) return false;

        int amount = FishManager.getStoshConsume(getUseLicence(player));
        if (item.getAmount() < amount) return false;

        item.setAmount(item.getAmount() - amount);

        useStosh.put(player.getUniqueId(), stosh);
        return true;
    }

    public static String getUseStosh(Player player) {
        return useStosh.remove(player.getUniqueId());
    }

    public static void setUseLicence(Player player, String licence) {
        useLicence.put(player.getUniqueId(), licence);
    }

    public static String getUseLicence(Player player) {
        return useLicence.get(player.getUniqueId());
    }

    public static Pair<String, ItemStack> getCaughtFish(Player player) {
        String rod = Utils.getPlayerHeldRod(player);
        String useStosh = getUseStosh(player);
        if (rod == null || useStosh == null) return null;

        String fish = FishManager.caughtFish(rod, useStosh);

        ItemStack item = ZaphkielAPI.INSTANCE.getItemStack(fish, player);
        int amount = FishManager.getFishMultiple(getUseLicence(player));
        if (item == null) return null;

        item.setAmount(amount);

        return new Pair<>(fish, item);
    }

    public static void startManualFishing(Player player) {
        inFishing.add(player.getUniqueId());
        PacketSender.sendYaml(player, FolderType.Gui, SCREEN_ID, JustFish.getFileManager().getFishManual());
        PacketSender.sendOpenHud(player, SCREEN_ID);
        startFishingTask(player);
    }

    public static void startAutoFishing(Player player) {
        inFishing.add(player.getUniqueId());
        PacketSender.sendYaml(player, FolderType.Gui, SCREEN_ID, JustFish.getFileManager().getFishAuto());
        PacketSender.sendOpenHud(player, SCREEN_ID);
    }

    public static void stopFishing(Player player) {
        stopFishingTask(player);
        inFishing.remove(player.getUniqueId());
        PacketSender.sendYaml(player, FolderType.Gui, SCREEN_ID, new YamlConfiguration());
        PacketSender.sendOpenHud(player, SCREEN_ID);
    }

    public static boolean inFishing(Player player) {
        return inFishing(player.getUniqueId());
    }

    public static boolean inFishing(UUID uuid) {
        return inFishing.contains(uuid);
    }

    public static void clearData(UUID uuid) {
        stopFishingTask(uuid);
        useStosh.remove(uuid);
        useLicence.remove(uuid);
        autoFishingTime.remove(uuid);
        inFishing.remove(uuid);
    }

}
