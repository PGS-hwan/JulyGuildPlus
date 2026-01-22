package com.github.hwan.julyguildplus.guild;

import com.github.hwan.julyguildplus.messagebox.YamlMessageBox;
import org.jetbrains.annotations.NotNull;

public class GuildMessageBox extends YamlMessageBox {
    private Guild guild;

    public GuildMessageBox(@NotNull Guild guild) {
        super(guild.getYaml().getConfigurationSection("message_box"));

        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }

    @Override
    public void save() {
        guild.save();
    }
}
