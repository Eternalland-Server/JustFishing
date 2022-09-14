package net.sakuragame.eternal.fishing.core;

import ink.ptms.zaphkiel.ZaphkielAPI;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FishManager {

    private static Map<String, String> texture;
    private static Map<String, List<Pair<String, Double>>> weight;
    private static Map<String, Pair<Integer, Integer>> exp;

    private final static Map<UUID, Entity> chairs = new HashMap<>();

    public FishManager() {
        this.loadTexture();
        this.loadWeight();
        this.loadExp();
    }

    public static Pair<String, ItemStack> caughtFish(String rod, String bait) {
        List<Pair<String, Double>> pool = weight.get(bait);
        String fish = Utils.lottery(pool);
        ItemStack item = ZaphkielAPI.INSTANCE.getRegisteredItem().get(fish).buildItemStack(null);
        if (rod.equals("fishing_rod_plus") && Math.random() > 0.5) {
            item.setAmount(2);
        }

        return new Pair<>(fish, item);
    }

    public static String getTexture(String fish) {
        return texture.get(fish);
    }

    public static void setChair(Player player, Entity entity) {
        chairs.put(player.getUniqueId(), entity);
    }

    public static boolean inChair(Player player) {
        return chairs.containsKey(player.getUniqueId());
    }

    public static void removeChair(UUID uuid) {
        Entity entity = chairs.remove(uuid);
       if (entity == null) return;
       entity.remove();
    }

    public static boolean sitOnChair(Player player) {
        UUID uuid = player.getUniqueId();
        Entity chair = chairs.get(uuid);
        if (chair == null) return false;

        for (Entity passenger : chair.getPassengers()) {
            if (!passenger.getUniqueId().equals(uuid)) continue;
            return true;
        }

        return false;
    }

    public static double getFishExp(String id) {
        if (!exp.containsKey(id)) return -1;
        Pair<Integer, Integer> scope = exp.get(id);
        return scope.getKey() + (scope.getValue() * Math.random());
    }

    private void loadTexture() {
        texture = new HashMap<>();
        YamlConfiguration yaml = JustFish.getFileManager().getSetting();
        ConfigurationSection section = yaml.getConfigurationSection("texture");
        if (section == null) return;
        for (String s : section.getKeys(false)) {
            String id = section.getString(s);
            texture.put(s, id);
        }
    }

    private void loadWeight() {
        weight = new HashMap<>();
        YamlConfiguration yaml = JustFish.getFileManager().getSetting();
        ConfigurationSection section = yaml.getConfigurationSection("weight");
        if (section == null) return;

        for (String s : section.getKeys(false)) {
            ConfigurationSection bait = section.getConfigurationSection(s);
            if (bait == null) continue;

            List<Pair<String, Double>> pairs = new ArrayList<>();
            for (String key : bait.getKeys(false)) {
                double value = bait.getDouble(key);
                pairs.add(new Pair<>(key, value));
            }

            weight.put(s, pairs);
        }
    }

    private void loadExp() {
        exp = new HashMap<>();
        YamlConfiguration yaml = JustFish.getFileManager().getSetting();
        ConfigurationSection section = yaml.getConfigurationSection("exp");
        if (section == null) return;

        for (String s : section.getKeys(false)) {
            String args = section.getString(s);
            int min = Integer.parseInt(args.split(" ", 2)[0]);
            int max = Integer.parseInt(args.split(" ", 2)[1]);
            exp.put(s, new Pair<>(min, max));
        }
    }
}
