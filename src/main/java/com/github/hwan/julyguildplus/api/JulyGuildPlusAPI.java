package com.github.hwan.julyguildplus.api;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.guild.GuildManager;
import com.github.hwan.julyguildplus.player.GuildPlayerManager;

public class JulyGuildPlusAPI {
    public static GuildManager getGuildManager() {
        return JulyGuildPlus.inst().getGuildManager();
    }

    public static GuildPlayerManager getGuildPlayerManager() {
        return JulyGuildPlus.inst().getGuildPlayerManager();
    }
}
