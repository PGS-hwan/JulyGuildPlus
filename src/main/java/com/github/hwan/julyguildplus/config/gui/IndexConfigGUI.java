package com.github.hwan.julyguildplus.config.gui;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.config.gui.item.GUIItemManager;
import com.github.hwan.julyguildplus.config.gui.item.IndexItem;
import com.github.hwan.julyguildplus.config.setting.MainSettings;
import com.github.hwan.julyguildplus.gui.BasePageableGUI;
import com.github.hwan.julyguildplus.placeholder.PlaceholderContainer;
import com.github.hwan.julyguildplus.placeholder.PlaceholderText;
import com.github.hwan.julyguildplus.util.Util;
import com.github.hwan.julylib.inventory.InventoryBuilder;
import com.github.hwan.julylib.inventory.ItemListener;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 提供了各种方法：支持了PAPI变量，公会变量，内部变量
 */
public class IndexConfigGUI {
    public static class Builder extends InventoryBuilder {
        public Builder fromConfig(@NotNull ConfigurationSection section, @Nullable PlaceholderContainer placeholderContainer) {
            return fromConfig(section, null, placeholderContainer);
        }

        public Builder fromConfig(@NotNull ConfigurationSection section, @Nullable Player papiPlayer) {
            return fromConfig(section, papiPlayer, null);
        }

        // 实现方法
        public Builder fromConfig(@NotNull ConfigurationSection section, @Nullable Player papiPlayer, @Nullable PlaceholderContainer placeholderContainer) {
            row(section.getInt( "row"));

            String finalTitle = section.getString("title");

            if (placeholderContainer != null) {
                finalTitle = PlaceholderText.replacePlaceholders(section.getString("title"), placeholderContainer);
            }

            if (JulyGuildPlus.inst().isPlaceHolderAPIEnabled() && papiPlayer != null) {
                finalTitle = PlaceholderAPI.setPlaceholders(papiPlayer, finalTitle);
            }

            title(finalTitle);
            colored(section.getBoolean("colored", MainSettings.isGuildGuiDefaultColored()));

            // 其他物品
            if (section.contains("other_items")) {
                setOtherItems(section.getConfigurationSection("other_items"), papiPlayer, placeholderContainer);
            }

            return this;
        }

        private Builder setOtherItems(@NotNull ConfigurationSection section, @Nullable Player papiPlayer, @Nullable PlaceholderContainer placeholderContainer) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection keySection = section.getConfigurationSection(key);

                if (keySection.contains("index")) {
                    item(GUIItemManager.getIndexItem(keySection, papiPlayer, placeholderContainer));
                } else {
                    for (int index : Util.getIndexes(keySection.getString("indexes"))) {
                        item(new IndexItem(index, GUIItemManager.getItemBuilder(keySection.getConfigurationSection("icon"), papiPlayer, placeholderContainer)));
                    }
                }
            }

            return this;
        }

        public Builder pageItems(@NotNull ConfigurationSection section, @NotNull BasePageableGUI basePageableGUI) {
            item(GUIItemManager.getIndexItem(section.getConfigurationSection("precious_page").getConfigurationSection(basePageableGUI.hasPreciousPage() ? "have" : "not_have")), !basePageableGUI.hasPreciousPage() ? null : new ItemListener() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    basePageableGUI.previousPage();
                }
            });
            item(GUIItemManager.getIndexItem(section.getConfigurationSection("next_page").getConfigurationSection(basePageableGUI.hasNextPage() ? "have" : "not_have")), !basePageableGUI.hasNextPage() ? null : new ItemListener() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    basePageableGUI.nextPage();
                }
            });

            return this;
        }

        /**
         * 应用索引物品
         * @param item 物品
         * @param itemListener 物品监听器
         * @return
         */
        public Builder item(@Nullable IndexItem item, @Nullable ItemListener itemListener) {
            if (item != null) {
                item(item.getIndex(), item.getItemBuilder().build(), itemListener);
            }

            return this;
        }

        /**
         * 应用索引物品
         * @param item 物品
         * @return
         */
        public Builder item(@Nullable IndexItem item) {
            item(item, null);
            return this;
        }
    }
}
