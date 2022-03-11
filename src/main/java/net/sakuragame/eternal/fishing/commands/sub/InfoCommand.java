package net.sakuragame.eternal.fishing.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import net.sakuragame.eternal.fishing.core.FishStats;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import net.sakuragame.eternal.jungle.JungleAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends SubCommand {

    @Override
    public String getIdentifier() {
        return "info";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Player player = getPlayer();
        int count = JungleAPI.getStatsValue(player.getUniqueId(), FishStats.Count.getIdentifier(), false);
        player.sendMessage(ConfigFile.prefix + "上钩次数: §a" + count);
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String getPermission() {
        return null;
    }
}
