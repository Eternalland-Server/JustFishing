package net.sakuragame.eternal.fishing.file.sub;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import net.sakuragame.eternal.fishing.JustFish;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class ConfigFile {
    private static YamlConfiguration config;

    public static String prefix;

    public static String region;

    public static List<String> licence;
    public static List<String> rod;
    public static List<String> stosh;

    public static void init() {
        config = JustFish.getFileManager().getConfig();

        prefix = getString("prefix");

        region = config.getString("region");

        licence = config.getStringList("licence");
        rod = config.getStringList("rod");
        stosh = config.getStringList("stosh");
    }

    private static String getString(String path) {
        return MegumiUtil.onReplace(config.getString(path));
    }

    private static List<String> getStringList(String path) {
        return MegumiUtil.onReplace(config.getStringList(path));
    }
}
