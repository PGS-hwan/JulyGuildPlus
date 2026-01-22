package com.github.hwan.julyguildplus.gui.entities;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.DebugMessage;
import com.github.hwan.julyguildplus.config.gui.IndexConfigGUI;
import com.github.hwan.julyguildplus.config.gui.item.GUIItemManager;
import com.github.hwan.julyguildplus.config.setting.MainSettings;
import com.github.hwan.julyguildplus.gui.BasePageableGUI;
import com.github.hwan.julyguildplus.guild.Guild;
import com.github.hwan.julyguildplus.guild.GuildIcon;
import com.github.hwan.julyguildplus.logger.JulyGuildPlusLogger;
import com.github.hwan.julyguildplus.placeholder.PlaceholderContainer;
import com.github.hwan.julyguildplus.player.GuildPlayer;
import com.github.hwan.julyguildplus.util.Util;
import com.github.hwan.julylib.chat.ChatInterceptor;
import com.github.hwan.julylib.chat.ChatListener;
import com.github.hwan.julylib.inventory.InventoryListener;
import com.github.hwan.julylib.inventory.ItemListener;
import com.github.hwan.julylib.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
返回时强制更新，手动强制更新
 */

/**
 * 主GUI
 * @version 1.0.0
 */
public class MainGUI extends BasePageableGUI {
    private final JulyGuildPlus plugin = JulyGuildPlus.inst();
    private final Player bukkitPlayer = guildPlayer.getBukkitPlayer();
    private final String playerName = bukkitPlayer.getName();
    private final ConfigurationSection thisGUISection = plugin.getGUIYaml("MainGUI");
    private final ConfigurationSection thisLangSection = plugin.getLangYaml().getConfigurationSection("MainGUI");
    private final List<Integer> itemIndexes; // 得到所有可供公会设置的位置
    private final int itemIndexCount;
    private List<Guild> guilds;
    private int guildCount;

    public MainGUI(@NotNull GuildPlayer guildPlayer) {
        super(null, Type.MAIN, guildPlayer);

        JulyGuildPlusLogger.debug("加载 'items.guild.indexes'.");
        this.itemIndexes = Util.getIndexes(thisGUISection.getString("items.guild.indexes"));
        this.itemIndexCount = itemIndexes.size();
    }

    @Override
    public void update() {
        this.guilds = plugin.getCacheGuildManager().getSortedGuilds();
        this.guildCount = guilds.size();

        setPageCount(guildCount % itemIndexCount == 0 ? guildCount / itemIndexCount : guildCount / itemIndexCount + 1);
    }

    @Override
    public Inventory createInventory() {
        Map<Integer, Guild> indexMap = new HashMap<>(); // slot 对应的公会uuid
        IndexConfigGUI.Builder guiBuilder = new IndexConfigGUI.Builder();

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_BASIC);
        guiBuilder.fromConfig(thisGUISection, bukkitPlayer, new PlaceholderContainer()
                        .add("page", String.valueOf(getCurrentPage() + 1))
                        .add("total_page", String.valueOf(getPageCount())));
        JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_BASIC);

        guiBuilder
                .colored()
                .listener(new InventoryListener() {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        int slot = event.getRawSlot();

                        if (indexMap.containsKey(slot)) {
                            close();

                            Guild guild = indexMap.get(slot);

                            if (!guild.isValid()) {
                                reopen();
                                return;
                            }

                            if (guild.isMember(guildPlayer)) {
                                new GuildMineGUI(MainGUI.this, guild.getMember(guildPlayer)).open();
                            } else {
                                new GuildInfoGUI(MainGUI.this, guildPlayer, guild).open();
                            }
                        }
                    }
                });


        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.page_items");
        guiBuilder.pageItems(thisGUISection.getConfigurationSection("items.page_items"), this);


        if (guildPlayer.isInGuild()) {
			JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.my_guild");
            guiBuilder.item(GUIItemManager.getIndexItem(thisGUISection.getConfigurationSection("items.my_guild"), bukkitPlayer, new PlaceholderContainer()
                    .add("%PLAYER%", playerName)), new ItemListener() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    close();
                    new GuildMineGUI(MainGUI.this, guildPlayer.getGuild().getMember(guildPlayer)).open();
                }
            });
            JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.my_guild");
        } else {
            JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.create_guild");
            guiBuilder.item(GUIItemManager.getIndexItem(thisGUISection.getConfigurationSection("items.create_guild"), bukkitPlayer), new ItemListener() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    close();
                    Util.sendMsg(bukkitPlayer, thisLangSection.getString("create.input.tip"), new PlaceholderContainer()
                            .add("cancel_str", MainSettings.getGuildCreateInputCancelStr()));
                    new ChatInterceptor.Builder()
                            .plugin(plugin)
                            .player(bukkitPlayer)
                            .onlyFirst(true)
                            .timeout(MainSettings.getGuildCreateInputWaitSec())
                            .chatListener(new ChatListener() {
                                @Override
                                public void onChat(AsyncPlayerChatEvent event) {
                                    String msg = event.getMessage();

                                    if (msg.equalsIgnoreCase(MainSettings.getGuildCreateInputCancelStr())) {
                                        Util.sendMsg(bukkitPlayer, thisLangSection.getString("create.input.cancelled"));
                                        return;
                                    }

                                    String guildName = ChatColor.translateAlternateColorCodes('&', msg);

                                    if (MainSettings.isGuildCreateNoDuplicationName()) {
                                        for (Guild guild : plugin.getGuildManager().getGuilds()) {
                                            if (guild.getName().equalsIgnoreCase(guildName)) {
                                                Util.sendMsg(bukkitPlayer, thisLangSection.getString("create.input.no_duplication_name"));
                                                return;
                                            }
                                        }
                                    }

                                    if (!guildName.matches(MainSettings.getGuildCreateNameRegex())) {
                                        Util.sendMsg(bukkitPlayer, thisLangSection.getString("create.input.regex_not_match"));
                                        return;
                                    }

                                    if (guildName.contains("§") && !bukkitPlayer.hasPermission("JulyGuildPlus.create.colored")) {
                                        Util.sendMsg(bukkitPlayer, thisLangSection.getString("create.input.no_colored_name_permission"));
                                        return;
                                    }

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            new GuildCreateGUI(MainGUI.this, guildPlayer, guildName).open();
                                        }
                                    }.runTask(plugin);
                                }

                                @Override
                                public void onTimeout(AsyncPlayerChatEvent event) {
                                    Util.sendMsg(bukkitPlayer, thisLangSection.getString("create.input.timeout"));
                                }
                            }).build().register();
                }
            });
            JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.create_guild");
        }

        int guildCounter = getCurrentPage() * itemIndexCount;
        int loopCount = guildCount - guildCounter < itemIndexCount ? guildCount - guildCounter : itemIndexCount; // 循环次数，根据当前能够显示的数量决定

        if (getPageCount() > 0) {
            // 公会图标
            for (int i = 0; i < loopCount; i++) {
                Guild guild = guilds.get(guildCounter++);
                JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.guild.icon");
                ItemBuilder itemBuilder = GUIItemManager.getItemBuilder(thisGUISection.getConfigurationSection("items.guild.icon"), bukkitPlayer, new PlaceholderContainer().addGuildPlaceholders(guild));
                JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.guild.icon");
                GuildIcon guildIcon = guild.getCurrentIcon();

                if (guildIcon != null) {
                    itemBuilder.material(guildIcon.getMaterial());
                    itemBuilder.durability(guildIcon.getDurability());

                    String firstLore = guildIcon.getFirstLore();

                    if (firstLore != null && !firstLore.equals("")) {
                        itemBuilder.insertLore(0, guildIcon.getFirstLore());
                    }
                } else {
                    itemBuilder.material(MainSettings.getGuildIconDefaultMaterial());
                    itemBuilder.durability(MainSettings.getGuildIconDefaultDurability());

                    String firstLore = MainSettings.getGuildIconDefaultFirstLore();

                    if (firstLore != null && !firstLore.equals("")) {
                        itemBuilder.insertLore(0, MainSettings.getGuildIconDefaultFirstLore());
                    }
                }

                indexMap.put(itemIndexes.get(i), guild);
                guiBuilder.item(itemIndexes.get(i), itemBuilder.build());
            }
        }
        guiBuilder.item(GUIItemManager.getIndexItem(thisGUISection.getConfigurationSection("items.back")), new ItemListener() {
            @Override
            public void onClick(InventoryClickEvent event) {
                close();

                String cmd = thisGUISection.getString("items.back.command");

                if (cmd == null) {
                    throw new RuntimeException("items.back.command 不能为空.");
                }

                String sender = thisGUISection.getString("items.back.sender");

                if (sender == null) {
                    throw new RuntimeException("items.back.sender 不能为空.");
                } else if (sender.equalsIgnoreCase("PLAYER")) {
                    bukkitPlayer.performCommand(cmd.replace("<player>", bukkitPlayer.getName()));
                } else if (sender.equalsIgnoreCase("CONSOLE")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>", bukkitPlayer.getName()));
                } else {
                    throw new RuntimeException("items.back.sender 不合法");
                }
            }
        });

        return guiBuilder.build();
    }

    @Override
    public boolean canUse() {
        return true;
    }
}