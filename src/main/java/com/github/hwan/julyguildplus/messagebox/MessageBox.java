package com.github.hwan.julyguildplus.messagebox;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface MessageBox {
    Collection<Message> getMessages();
    void removeMessage(@NotNull Message message);
    void sendMessage(@NotNull String message);
}
