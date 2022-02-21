package net.sakuragame.eternal.fishing.file;

import com.taylorswiftcn.justwei.file.JustConfiguration;
import com.taylorswiftcn.justwei.util.MegumiUtil;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.file.sub.MessageFile;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;


public class FileManager extends JustConfiguration {

    private final JustFish plugin;
    @Getter private YamlConfiguration config;
    @Getter private YamlConfiguration message;
    @Getter private YamlConfiguration setting;

    @Getter private YamlConfiguration fishManual;
    @Getter private YamlConfiguration fishAuto;

    public FileManager(JustFish plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void init() {
        config = initFile("config.yml");
        message = initFile("message.yml");
        setting = initFile("setting.yml");

        ConfigFile.init();
        MessageFile.init();
        initUIFile();
    }

    private void initUIFile() {
        File file = new File(plugin.getDataFolder(), "ui");
        if (!file.exists()) {
            createUIFile("fish_manual.yml");
            createUIFile("fish_auto.yml");
        }

        File manual = new File(file, "fish_manual.yml");
        File auto = new File(file, "fish_auto.yml");
        fishManual = YamlConfiguration.loadConfiguration(manual);
        fishAuto = YamlConfiguration.loadConfiguration(auto);
    }

    private void createUIFile(String fileName) {
        File file = new File(plugin.getDataFolder(), "ui/" + fileName);
        file.getParentFile().mkdirs();
        MegumiUtil.copyFile(plugin.getResource(fileName), file);
    }
}
