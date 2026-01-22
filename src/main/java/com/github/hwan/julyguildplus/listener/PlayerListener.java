package com.github.hwan.julyguildplus.listener;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.player.GuildPlayer;
import com.github.hwan.julyguildplus.player.GuildPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private GuildPlayerManager guildPlayerManager = JulyGuildPlus.inst().getGuildPlayerManager();

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GuildPlayer guildPlayer = guildPlayerManager.getGuildPlayer(player);

        guildPlayer.setKnownName(player.getName());
    }
}
