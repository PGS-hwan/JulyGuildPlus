package com.github.hwan.julyguildplus.gui;

import com.github.hwan.julyguildplus.DebugMessage;
import com.github.hwan.julyguildplus.config.gui.IndexConfigGUI;
import com.github.hwan.julyguildplus.config.gui.item.GUIItemManager;
import com.github.hwan.julyguildplus.logger.JulyGuildPlusLogger;
import com.github.hwan.julyguildplus.placeholder.PlaceholderContainer;
import com.github.hwan.julyguildplus.player.GuildPlayer;
import com.github.hwan.julylib.inventory.ItemListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseConfirmGUI extends BasePlayerGUI {
    private final ConfigurationSection section;
    private final Player bukkitPlayer = getBukkitPlayer();
    private final PlaceholderContainer confirmPlaceholderContainer;

    protected BaseConfirmGUI(@Nullable GUI lastGUI, @NotNull GuildPlayer guildPlayer, @NotNull ConfigurationSection section) {
        this(lastGUI, guildPlayer, section, null);
    }

    protected BaseConfirmGUI(@Nullable GUI lastGUI, @NotNull GuildPlayer guildPlayer, @NotNull ConfigurationSection section, @Nullable PlaceholderContainer confirmPlaceholderContainer) {
        super(lastGUI, Type.CONFIRM, guildPlayer);

        this.section = section;
        this.confirmPlaceholderContainer = confirmPlaceholderContainer;
    }

    public PlaceholderContainer getConfirmPlaceholderContainer() {
        return confirmPlaceholderContainer;
    }

    @Override
    public abstract boolean canUse();

    public abstract void onConfirm();

    public abstract void onCancel();

    @Override
    public Inventory createInventory() {
        IndexConfigGUI.Builder guiBuilder = new IndexConfigGUI.Builder();

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_BASIC);
        guiBuilder.fromConfig(section, confirmPlaceholderContainer);
        JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_BASIC);

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.cancel");
        guiBuilder.item(GUIItemManager.getIndexItem(section.getConfigurationSection("items.cancel"), bukkitPlayer), new ItemListener() {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        onCancel();
                    }
                });
        JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.cancel");

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.confirm");
        guiBuilder.item(GUIItemManager.getIndexItem(section.getConfigurationSection("items.confirm"), bukkitPlayer, confirmPlaceholderContainer), new ItemListener() {
            @Override
            public void onClick(InventoryClickEvent event) {
                onConfirm();
            }
        });
        JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.confirm");

        return guiBuilder.build();
    }
}
