package net.sakuragame.eternal.fishing.file.sub;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import net.sakuragame.eternal.fishing.JustFish;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class ConfigFile {
    private static YamlConfiguration config;

    public static String prefix;

    public static void init() {
        config = JustFish.getInstance().getFileManager().getConfig();

        prefix = getString("prefix");
    }

    private static String getString(String path) {
        return MegumiUtil.onReplace(config.getString(path));
    }

    private static List<String> getStringList(String path) {
        return MegumiUtil.onReplace(config.getStringList(path));
    }
}
