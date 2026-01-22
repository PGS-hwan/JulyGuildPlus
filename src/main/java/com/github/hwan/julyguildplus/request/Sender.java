package com.github.hwan.julyguildplus.request;

import com.github.hwan.julyguildplus.JulyGuildPlus;

import java.util.List;

public interface Sender {
    enum Type {
        GUILD, GUILD_PLAYER, GUILD_MEMBER
    }

    default List<Request> getSentRequests() {
        return JulyGuildPlus.inst().getRequestManager().getSentRequests(this);
    }
}
