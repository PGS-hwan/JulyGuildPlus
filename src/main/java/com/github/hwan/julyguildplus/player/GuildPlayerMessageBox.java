package com.github.hwan.julyguildplus.player;

import com.github.hwan.julyguildplus.messagebox.YamlMessageBox;

public class GuildPlayerMessageBox extends YamlMessageBox {
    private GuildPlayer guildPlayer;

    public GuildPlayerMessageBox(GuildPlayer guildPlayer) {
        super(guildPlayer.getYaml().getConfigurationSection("message_box"));

        this.guildPlayer = guildPlayer;
    }

    public GuildPlayer getGuildPlayer() {
        return guildPlayer;
    }

    @Override
    public void save() {
        guildPlayer.save();
    }
}
