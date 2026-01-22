package com.github.hwan.julyguildplus.gui.entities;

import com.github.hwan.julyguildplus.DebugMessage;
import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.config.gui.IndexConfigGUI;
import com.github.hwan.julyguildplus.config.gui.item.GUIItemManager;
import com.github.hwan.julyguildplus.gui.GUI;
import com.github.hwan.julyguildplus.gui.BasePageableGUI;
import com.github.hwan.julyguildplus.guild.Guild;
import com.github.hwan.julyguildplus.guild.member.GuildMember;
import com.github.hwan.julyguildplus.guild.member.GuildPermission;
import com.github.hwan.julyguildplus.logger.JulyGuildPlusLogger;
import com.github.hwan.julyguildplus.placeholder.PlaceholderContainer;
import com.github.hwan.julyguildplus.player.GuildPlayer;
import com.github.hwan.julyguildplus.util.Util;
import com.github.hwan.julylib.inventory.InventoryListener;
import com.github.hwan.julylib.inventory.ItemListener;
import com.github.hwan.julylib.item.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GuildMemberListGUI extends BasePageableGUI {
    private enum ViewerType {PLAYER, MANAGER}
    private static final List<GuildPermission> MANAGER_GUILD_PERMISSIONS = Arrays.asList(GuildPermission.MEMBER_KICK, GuildPermission.MANAGE_PERMISSION);
    private final JulyGuildPlus plugin = JulyGuildPlus.inst();
    private final ViewerType viewerType;
    private final Guild guild;
    private final Player bukkitPlayer = getBukkitPlayer();
    private ConfigurationSection thisGUISection;
    private List<Integer> itemIndexes;
    private int itemIndexCount;
    private List<GuildMember> members;
    private int memberCount;

    public GuildMemberListGUI(@Nullable GUI lastGUI, @NotNull Guild guild, @NotNull GuildMember guildMember) {
        this(lastGUI, guild, guildMember.getGuildPlayer());
    }

    public GuildMemberListGUI(@Nullable GUI lastGUI, @NotNull Guild guild, @NotNull GuildPlayer guildPlayer) {
        super(lastGUI, Type.MEMBER_LIST, guildPlayer);

        this.guild = guild;

        GuildMember member = guild.getMember(guildPlayer);

        out:
        if (member == null) {
            viewerType = ViewerType.PLAYER;
        } else {
            for (GuildPermission guildPermission : MANAGER_GUILD_PERMISSIONS) {
                if (member.hasPermission(guildPermission)) {
                    viewerType = ViewerType.MANAGER;
                    break out;
                }
            }

            viewerType = ViewerType.PLAYER;
        }

        this.thisGUISection = plugin.getGUIYaml("GuildMemberListGUI").getConfigurationSection(viewerType.name().toLowerCase());

        JulyGuildPlusLogger.debug("开始: 加载 'items.member.indexes'.");
        this.itemIndexes = Util.getIndexes(thisGUISection.getString( "items.member.indexes")); // 得到所有可供公会设置的位置
        JulyGuildPlusLogger.debug("结束: 加载 'items.member.indexes'.");

        this.itemIndexCount = itemIndexes.size();
    }

    @Override
    public void update() {
        this.members = guild.getMembers();
        this.members.sort(Comparator.comparingLong(GuildMember::getJoinTime));
        this.memberCount = members.size();

        setPageCount(memberCount % itemIndexCount == 0 ? memberCount / itemIndexCount : memberCount / itemIndexCount + 1);
    }

    @Override
    public Inventory createInventory() {
        Map<Integer, GuildMember> indexMap = new HashMap<>();
        IndexConfigGUI.Builder guiBuilder = new IndexConfigGUI.Builder();

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_BASIC);
        guiBuilder.fromConfig(thisGUISection, bukkitPlayer, new PlaceholderContainer()
                        .add("page", getCurrentPage() + 1)
                        .add("total_page", getPageCount()));
        JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_BASIC);

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.page_items");
        guiBuilder.pageItems(thisGUISection.getConfigurationSection("items.page_items"), this);
        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.page_items");

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.back");
        guiBuilder.item(GUIItemManager.getIndexItem(thisGUISection.getConfigurationSection("items.back"), bukkitPlayer, new PlaceholderContainer().addGuildPlaceholders(guild)), new ItemListener() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (canBack()) {
                    back();
                }
            }
        });
        JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.back");


        if (viewerType == ViewerType.MANAGER) {
            guiBuilder.listener(new InventoryListener() {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            int slot = event.getRawSlot();

                            if (indexMap.containsKey(slot)) {
                                GuildMember guildMember = indexMap.get(slot);

                                if (!guildMember.isValid()) {
                                    reopen();
                                    return;
                                }

                                close();

                                new GuildMemberManageGUI(GuildMemberListGUI.this, GuildMemberListGUI.this.guild.getMember(guildPlayer), guildMember).open();
                            }
                        }
                    });
        }

        if (getPageCount() > 0) {
            int memberCounter = getCurrentPage() * itemIndexes.size();
            int loopCount = memberCount - memberCounter < itemIndexCount ? memberCount - memberCounter : itemIndexCount;

            for (int i = 0; i < loopCount; i++) {
                GuildMember guildMember = members.get(memberCounter++);
                JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.member.icon");
                ItemBuilder itemBuilder = GUIItemManager.getItemBuilder(thisGUISection.getConfigurationSection("items.member.icon"), bukkitPlayer, new PlaceholderContainer()
                        .addGuildPlaceholders(guild)
                        .addGuildMemberPlaceholders(guildMember));
                JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.member.icon");

                // 管理模式
                if (viewerType == ViewerType.MANAGER) {
                    indexMap.put(itemIndexes.get(i), guildMember);
                }

                guiBuilder.item(itemIndexes.get(i), itemBuilder.build());
            }
        }

        return guiBuilder.build();
    }

    @Override
    public boolean canUse() {
        return guild.isValid();
    }
}
