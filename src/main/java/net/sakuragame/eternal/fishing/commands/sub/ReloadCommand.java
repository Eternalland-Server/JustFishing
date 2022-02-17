package net.sakuragame.eternal.fishing.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import net.sakuragame.eternal.fishing.commands.CommandPerms;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    @Override
    public String getIdentifier() {
        return "reload";
    }

    @Override
    public void perform(CommandSender CommandSender, String[] Strings) {

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
