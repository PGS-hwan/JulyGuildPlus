package com.github.hwan.julyguildplus.request.entities;

import com.github.hwan.julyguildplus.guild.Guild;
import com.github.hwan.julyguildplus.config.setting.MainSettings;
import com.github.hwan.julyguildplus.player.GuildPlayer;
import com.github.hwan.julyguildplus.request.BaseRequest;
import org.jetbrains.annotations.NotNull;

public class JoinRequest extends BaseRequest<GuildPlayer, Guild> {
    public JoinRequest() {}

    public JoinRequest(@NotNull GuildPlayer sender, @NotNull Guild receiver) {
        super(sender, receiver);
    }

    @Override
    public Type getType() {
        return Type.JOIN;
    }

    @Override
    public boolean isValid() {
        return (System.currentTimeMillis() - getCreationTime()) / 1000L < MainSettings.getGuildRequestJoinTimeout() && !getSender().isInGuild() && getReceiver().isValid();
    }
}
