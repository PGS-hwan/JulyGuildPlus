package com.github.hwan.julyguildplus.messagebox;

import java.util.UUID;

public interface Message {
    long getCreationTime();
    String getMessage();
    UUID getUuid();
}
