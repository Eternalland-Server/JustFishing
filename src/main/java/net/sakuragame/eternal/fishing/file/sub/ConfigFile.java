package net.sakuragame.eternal.fishing.file.sub;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import net.sakuragame.eternal.fishing.JustFish;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigFile {
    private static YamlConfiguration config;

    public static String prefix;

    public static int autoFishingTime;

    public static List<String> licence;
    public static List<String> rod;
    public static List<String> pubChair;

    public static Map<String, String> broadcastItem;

    public static void init() {
        config = JustFish.getFileManager().getConfig();

        prefix = getString("prefix");

        autoFishingTime = config.getInt("auto-fishing-time");

        licence = config.getStringList("licence");
        rod = config.getStringList("rod");
        pubChair = config.getStringList("public-chair");
        loadBroadcastItem();
    }

    private static String getString(String path) {
        return MegumiUtil.onReplace(config.getString(path));
    }

    private static List<String> getStringList(String path) {
        return MegumiUtil.onReplace(config.getStringList(path));
    }

    private static void loadBroadcastItem() {
        broadcastItem = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("broadcast-item");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String s = section.getString(key);
            broadcastItem.put(key, s.toUpperCase());
        }
    }
}
