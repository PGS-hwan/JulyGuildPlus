package com.github.hwan.julyguildplus.command;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.gui.entities.MainGUI;
import com.github.hwan.julyguildplus.player.GuildPlayerManager;
import com.github.hwan.julylib.commandv2.JulyCommand;
import com.github.hwan.julylib.commandv2.MainCommand;
import com.github.hwan.julylib.commandv2.SenderType;
import com.github.hwan.julylib.commandv2.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@MainCommand(firstArg = "gui", description = "打开主界面")
public class GUICommand implements JulyCommand {
    private JulyGuildPlus plugin = JulyGuildPlus.inst();
    private GuildPlayerManager guildPlayerManager = plugin.getGuildPlayerManager();

    @SubCommand(firstArg = "main", description = "打开主界面", length = 0, senders = SenderType.PLAYER)
    public void onGui(CommandSender cs, String[] args) {
        new MainGUI(guildPlayerManager.getGuildPlayer((Player) cs)).open();
    }
}
