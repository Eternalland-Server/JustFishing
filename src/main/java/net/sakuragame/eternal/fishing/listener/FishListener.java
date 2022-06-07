package net.sakuragame.eternal.fishing.listener;

import net.sakuragame.eternal.fishing.api.event.FishAirForceEvent;
import net.sakuragame.eternal.fishing.api.event.FishCaughtEvent;
import net.sakuragame.eternal.fishing.api.event.FishStoshUseUpEvent;
import net.sakuragame.eternal.fishing.core.FishStats;
import net.sakuragame.eternal.fishing.core.Fishery;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.fishing.util.Scheduler;
import net.sakuragame.eternal.jungle.JungleAPI;
import net.sakuragame.eternal.justmessage.JustMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class FishListener implements Listener {

    @EventHandler
    public void onCaught(FishCaughtEvent e) {
        Player player = e.getPlayer();
        String fishID = e.getFishID();
        ItemStack fishItem = e.getFishItem();

        Scheduler.runAsync(() -> JungleAPI.addStatsValue(player.getUniqueId(), FishStats.Count.getIdentifier(), 1));

        if (ConfigFile.broadcastItem.contains(fishID)) {
            JustMessage.getChatManager().sendAll(
                    fishItem,
                    new String[] {
                            "§e恭喜玩家 §7" + player.getName() + " §e钓起 ",
                            "<item>",
                            "§e ，大家一起祝贺TA吧！"
                    }
            );
        }

        player.sendTitle("§3§l上钩了!", "§e§l+ " + fishItem.getItemMeta().getDisplayName() + " §c§lx" + fishItem.getAmount(), 5, 20, 5);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
    }

    @EventHandler
    public void onAirForce(FishAirForceEvent e) {
        Player player = e.getPlayer();

        player.sendTitle("§c§l脱钩了~", "", 5, 20, 5);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
    }

    @EventHandler
    public void onUseUp(FishStoshUseUpEvent e) {
        Player player = e.getPlayer();

        player.sendTitle("§c§l鱼饵用完了!", "", 5, 20, 5);
        Fishery.stopFishing(player);
    }
}
