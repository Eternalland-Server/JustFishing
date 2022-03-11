package net.sakuragame.eternal.fishing.core;

import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class FishManager {

    private static Map<String, Pair<Integer, Integer>> licence;
    private static Map<String, String> texture;
    private static Map<String, Map<String, List<Pair<String, Double>>>> weight;

    private final static Map<UUID, Entity> chairs = new HashMap<>();

    public FishManager() {
        this.loadLicence();
        this.loadTexture();
        this.loadWeight();
    }

    public static int getStoshConsume(String licenceID) {
        return licence.get(licenceID).getKey();
    }

    public static int getFishMultiple(String licenceID) {
        return licence.get(licenceID).getValue();
    }

    public static String caughtFish(String rod, String stosh) {
        List<Pair<String, Double>> pool = weight.get(rod).get(stosh);
        return Utils.lottery(pool);
    }

    public static String getTexture(String fish) {
        return texture.get(fish);
    }

    public static void setChair(Player player, Entity entity) {
        chairs.put(player.getUniqueId(), entity);
    }

    public static Entity getChair(Player player) {
        return chairs.get(player.getUniqueId());
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

    private void loadLicence() {
        licence = new HashMap<>();
        YamlConfiguration yaml = JustFish.getFileManager().getSetting();
        ConfigurationSection section = yaml.getConfigurationSection("licence");
        if (section == null) return;

        for (String s : section.getKeys(false)) {
            int stosh = section.getInt(s + ".stosh");
            int fish = section.getInt(s + ".fish");
            licence.put(s, new Pair<>(stosh, fish));
        }
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
        ConfigurationSection rod = yaml.getConfigurationSection("weight");
        if (rod == null) return;

        for (String s : rod.getKeys(false)) {
            Map<String, List<Pair<String, Double>>> stoshMap = new HashMap<>();
            ConfigurationSection stosh = rod.getConfigurationSection(s);
            if (stosh == null) continue;

            for (String key : stosh.getKeys(false)) {
                List<Pair<String, Double>> pairs = new ArrayList<>();
                ConfigurationSection fish = stosh.getConfigurationSection(key);
                if (fish == null) continue;

                for (String target : fish.getKeys(false)) {
                    double value = fish.getDouble(target);
                    pairs.add(new Pair<>(target, value));
                }
                stoshMap.put(key, pairs);
            }

            weight.put(s, stoshMap);
        }
    }
}
