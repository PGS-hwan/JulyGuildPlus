package com.github.hwan.julyguildplus.task;

import com.github.hwan.julyguildplus.logger.JulyGuildPlusLogger;
import org.bukkit.scheduler.BukkitRunnable;

public class LoggerSaveTask extends BukkitRunnable {
	@Override
	public void run() {

		JulyGuildPlusLogger.flushWriter();
	}
}
