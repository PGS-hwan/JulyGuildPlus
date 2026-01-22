package com.github.hwan.julyguildplus.command;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.logger.JulyGuildPlusLogger;
import com.github.hwan.julyguildplus.logger.LoggerLevel;
import com.github.hwan.julyguildplus.util.Util;
import com.github.hwan.julylib.commandv2.JulyCommand;
import com.github.hwan.julylib.commandv2.MainCommand;
import com.github.hwan.julylib.commandv2.SenderType;
import com.github.hwan.julylib.commandv2.SubCommand;
import com.github.hwan.julylib.utils.ItemUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@MainCommand(firstArg = "admin", description = "管理员命令")
public class AdminCommand implements JulyCommand {
	private JulyGuildPlus plugin = JulyGuildPlus.inst();

	@SubCommand(firstArg = "getItemInfo", description = "获得物品信息", length = 0, senders = SenderType.PLAYER, permission = "JulyGuildPlus.admin")
	public void getItemInfo(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		ItemStack itemStack = player.getItemInHand();

		if (!ItemUtil.isValid(itemStack)) {
			Util.sendMsg(sender, "&c物品不合法.");
			return;
		}

		Util.sendMsg(sender, "&fmaterial = " + itemStack.getType().name());
		Util.sendMsg(sender, "&fdurability = " + itemStack.getDurability());
	}

	@SubCommand(firstArg = "debug", description = "切换 logger 级别为 DEBUG/INFO.", length = 0, permission = "JulyGuildPlus.admin")
	public void toggleDebug(CommandSender sender, String[] args) {
		if (JulyGuildPlusLogger.getLevel() == LoggerLevel.DEBUG) {
			JulyGuildPlusLogger.setLevel(LoggerLevel.INFO);
			Util.sendMsg(sender, "当前 logger 级别: INFO.");
		} else {
			JulyGuildPlusLogger.setLevel(LoggerLevel.DEBUG);
			Util.sendMsg(sender, "当前 logger 级别: DEBUG.");
			Util.sendMsg(sender, "&e这会在后台显示更多的信息来帮助你排查错误.");
		}
	}

	@SubCommand(firstArg = "reload", description = "重载插件配置", length = 0, permission = "JulyGuildPlus.admin")
	public void onReload(CommandSender cs, String[] args) {
		plugin.reloadPlugin();
		Util.sendMsg(cs, "&f配置重载完毕.");
	}
}
