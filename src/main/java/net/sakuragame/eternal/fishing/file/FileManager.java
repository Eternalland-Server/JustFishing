package net.sakuragame.eternal.fishing.file;

import com.taylorswiftcn.justwei.file.JustConfiguration;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.file.sub.MessageFile;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;


public class FileManager extends JustConfiguration {

    @Getter private YamlConfiguration config;
    @Getter private YamlConfiguration message;

    public FileManager(JustFish plugin) {
        super(plugin);
    }

    public void init() {
        config = initFile("config.yml");
        message = initFile("message.yml");

        ConfigFile.init();
        MessageFile.init();
    }
}
