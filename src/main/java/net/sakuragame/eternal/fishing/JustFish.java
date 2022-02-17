package net.sakuragame.eternal.fishing;

import net.sakuragame.eternal.fishing.commands.MainCommand;
import net.sakuragame.eternal.fishing.file.FileManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class JustFish extends JavaPlugin {

    @Getter private static JustFish instance;

    @Getter private FileManager fileManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        instance = this;

        fileManager = new FileManager(this);
        fileManager.init();

        getCommand("jfish").setExecutor(new MainCommand());

        long end = System.currentTimeMillis();

        getLogger().info("加载成功! 用时 %time% ms".replace("%time%", String.valueOf(end - start)));
    }

    @Override
    public void onDisable() {
        getLogger().info("卸载成功!");
    }

    public String getVersion() {
        String packet = Bukkit.getServer().getClass().getPackage().getName();
        return packet.substring(packet.lastIndexOf('.') + 1);
    }
}
