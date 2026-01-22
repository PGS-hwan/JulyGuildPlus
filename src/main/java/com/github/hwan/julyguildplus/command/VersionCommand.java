package com.github.hwan.julyguildplus.command;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.util.Util;
import com.github.hwan.julylib.commandv2.JulyCommand;
import com.github.hwan.julylib.commandv2.MainCommand;
import com.github.hwan.julylib.commandv2.SubCommand;
import org.bukkit.command.CommandSender;

@MainCommand(firstArg = "info", description = "插件版本")
public class VersionCommand implements JulyCommand {

    @SubCommand(firstArg = "version", description = "插件版本", length = 0)
    public void onVersion(CommandSender cs, String[] args) {
        Util.sendMsg(cs, "&f插件版本: " + JulyGuildPlus.inst().getDescription().getVersion() + ".");
    }
}
