package net.sakuragame.eternal.fishing;

import lombok.Getter;
import net.sakuragame.eternal.fishing.commands.MainCommand;
import net.sakuragame.eternal.fishing.core.FishManager;
import net.sakuragame.eternal.fishing.core.FishStats;
import net.sakuragame.eternal.fishing.file.FileManager;
import net.sakuragame.eternal.fishing.listener.FishListener;
import net.sakuragame.eternal.fishing.listener.SeatListener;
import net.sakuragame.eternal.fishing.listener.UIListener;
import net.sakuragame.eternal.fishing.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class JustFish extends JavaPlugin {

    @Getter private static JustFish instance;

    @Getter private static FileManager fileManager;
    @Getter private static FishManager fishManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        instance = this;

        fileManager = new FileManager(this);
        fileManager.init();

        fishManager = new FishManager();

        FishStats.register();

        registerListener(new PlayerListener());
        registerListener(new UIListener());
        registerListener(new SeatListener());
        registerListener(new FishListener());
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

    public void reload() {
        fileManager.init();
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
