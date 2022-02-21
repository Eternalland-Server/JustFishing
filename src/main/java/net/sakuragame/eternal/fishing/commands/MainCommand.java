package net.sakuragame.eternal.fishing.commands;

import com.taylorswiftcn.justwei.commands.JustCommand;
import net.sakuragame.eternal.fishing.commands.sub.HelpCommand;
import net.sakuragame.eternal.fishing.commands.sub.InfoCommand;
import net.sakuragame.eternal.fishing.commands.sub.ReloadCommand;

public class MainCommand extends JustCommand {

    public MainCommand() {
        super(new HelpCommand());
        register(new InfoCommand());
        register(new ReloadCommand());
    }
}
