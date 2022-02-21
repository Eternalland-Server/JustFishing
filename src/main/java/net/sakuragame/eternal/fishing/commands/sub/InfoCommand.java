package net.sakuragame.eternal.fishing.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import net.sakuragame.eternal.fishing.api.JustFishAPI;
import net.sakuragame.eternal.fishing.core.FishAccount;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
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
        FishAccount account = JustFishAPI.getAccount(player);
        player.sendMessage(ConfigFile.prefix + "上钩次数: §a" + account.getAmount());
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
