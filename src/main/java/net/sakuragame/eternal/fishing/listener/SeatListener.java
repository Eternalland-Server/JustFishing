package net.sakuragame.eternal.fishing.listener;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.api.event.FishSeatEnterEvent;
import net.sakuragame.eternal.fishing.api.event.FishSeatLeaveEvent;
import net.sakuragame.eternal.fishing.core.Fishery;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class SeatListener implements Listener {

    @EventHandler
    public void onPostEnter(FishSeatEnterEvent.Post e) {
        Player player = e.getPlayer();
        player.sendTitle("", "§3§l右键鱼竿开始钓鱼", 10, 20, 10);
    }

    @EventHandler
    public void onLeave(FishSeatLeaveEvent e) {
        Player player = e.getPlayer();
        if (!Fishery.inFishing(player)) return;
        Fishery.stopFishing(player);
        JustFish.getFishManager().saveAccount(player);
    }

    @EventHandler
    public void onPreEnter(FishSeatEnterEvent.Pre e) {
        Player player = e.getPlayer();

        String licence = getFishLicence(player);
        if (licence == null) {
            player.sendMessage(ConfigFile.prefix + "你没有钓鱼许可证，请将许可证带在身上");
            e.setCancelled(true);
            return;
        }
        Fishery.setUseLicence(player, licence);
    }

    private String getFishLicence(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (MegumiUtil.isEmpty(item)) continue;
            String id = Utils.getZapItemID(item);
            if (id == null) continue;
            if (ConfigFile.licence.contains(id)) {
                return id;
            }
        }

        return null;
    }
}
