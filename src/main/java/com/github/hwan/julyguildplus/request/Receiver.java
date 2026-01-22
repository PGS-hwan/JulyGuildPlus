package com.github.hwan.julyguildplus.request;

import com.github.hwan.julyguildplus.JulyGuildPlus;

import java.util.List;

public interface Receiver {
    enum Type {
        GUILD, GUILD_PLAYER, GUILD_MEMBER
    }

    default List<Request> getReceivedRequests() {
        return JulyGuildPlus.inst().getRequestManager().getReceivedRequests(this);
    }
}
