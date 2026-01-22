package com.github.hwan.julyguildplus.task;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.request.RequestManager;
import org.bukkit.scheduler.BukkitRunnable;

public class RequestCleanTask extends BukkitRunnable {
    private static JulyGuildPlus plugin = JulyGuildPlus.inst();
    private static RequestManager requestManager = plugin.getRequestManager();

    @Override
    public void run() {
        requestManager.getRequests().forEach(request -> {
            if (!request.isValid()) {
                requestManager.deleteRequest(request);
            }
        });
    }
}
