package net.sakuragame.eternal.fishing.core;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import ink.ptms.zaphkiel.ZaphkielAPI;
import ink.ptms.zaphkiel.api.ItemStream;
import ink.ptms.zaphkiel.taboolib.module.nms.ItemTag;
import ink.ptms.zaphkiel.taboolib.module.nms.ItemTagData;
import net.sakuragame.eternal.dragoncore.config.FolderType;
import net.sakuragame.eternal.dragoncore.network.PacketSender;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.core.task.ManualFishing;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.util.HookUtils;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Fishery {

    private final static JustFish plugin = JustFish.getInstance();
    private final static Map<UUID, ManualFishing> scheduled = new HashMap<>();
    private final static Map<UUID, Long> autoFishingTime = new HashMap<>();

    private final static List<UUID> inFishing = new ArrayList<>();

    public final static String SCREEN_ID = "fish";
    private final static String NBT_NODE = "fish.bait";

    public static boolean manualFishing(Player player, FishHook hook) {
        if (checkBait(player)) {
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
            player.sendMessage(ConfigFile.prefix + "鱼饵用完了!");
            return false;
        }
    }

    public static boolean autoFishing(Player player, FishHook hook) {
        if (checkBait(player)) {
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
            player.sendMessage(ConfigFile.prefix + "鱼饵用完了!");
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
        Bukkit.getScheduler().cancelTask(task.getTaskId());
    }

    public static void putAutoFishingTime(Player player) {
        autoFishingTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static long getAutoFishingTime(Player player) {
        return autoFishingTime.getOrDefault(player.getUniqueId(), 0L);
    }

    public static boolean checkBait(Player player) {
        ItemStack rodItem = player.getInventory().getItemInMainHand();
        ItemStream stream = ZaphkielAPI.INSTANCE.read(rodItem);
        ItemTag tag = stream.getZaphkielData();
        ItemTagData data = tag.getDeep(NBT_NODE);
        return data != null;
    }

    public static void consumeBait(Player player) {
        ItemStack rodItem = player.getInventory().getItemInMainHand();
        ItemStream rodStream = ZaphkielAPI.INSTANCE.read(rodItem);
        ItemTag tag = rodStream.getZaphkielData();

        ItemTagData data = tag.getDeep(NBT_NODE);
        if (data == null) return;

        String s = data.asString();
        String bait = s.split("\\|", 2)[0];
        int amount = Integer.parseInt(s.split("\\|", 2)[1]);
        amount--;
        if (amount <= 0) tag.removeDeep(NBT_NODE);
        else tag.putDeep(NBT_NODE, bait + "|" + amount);
        player.getInventory().setItemInMainHand(rodStream.rebuildToItemStack(player));
    }

    public static Pair<String, ItemStack> getCaughtFish(Player player) {
        ItemStack rodItem = player.getInventory().getItemInMainHand();
        ItemStream rodStream = ZaphkielAPI.INSTANCE.read(rodItem);
        ItemTag tag = rodStream.getZaphkielData();

        String rod = rodStream.getZaphkielName();
        ItemTagData data = tag.getDeep(NBT_NODE);
        if (data == null) return null;

        String s = data.asString();
        String bait = s.split("\\|", 2)[0];
        int amount = Integer.parseInt(s.split("\\|", 2)[1]);
        amount--;
        if (amount <= 0) tag.removeDeep(NBT_NODE);
        else tag.putDeep(NBT_NODE, bait + "|" + amount);
        player.getInventory().setItemInMainHand(rodStream.rebuildToItemStack(player));

        return FishManager.caughtFish(rod, bait);
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
        autoFishingTime.remove(uuid);
        inFishing.remove(uuid);
    }

}
