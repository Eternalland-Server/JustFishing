package net.sakuragame.eternal.fishing.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import net.sakuragame.eternal.fishing.JustFish;
import net.sakuragame.eternal.fishing.commands.CommandPerms;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    @Override
    public String getIdentifier() {
        return "reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        JustFish.getInstance().reload();
        sender.sendMessage(ConfigFile.prefix + "重载成功");
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String getPermission() {
        return CommandPerms.ADMIN.getNode();
    }
}
