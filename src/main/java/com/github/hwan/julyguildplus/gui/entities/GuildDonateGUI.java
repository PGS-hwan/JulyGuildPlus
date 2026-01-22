package com.github.hwan.julyguildplus.gui.entities;

import com.github.hwan.julyguildplus.DebugMessage;
import com.github.hwan.julyguildplus.config.gui.IndexConfigGUI;
import com.github.hwan.julyguildplus.guild.Guild;
import com.github.hwan.julyguildplus.logger.JulyGuildPlusLogger;
import com.github.hwan.julyguildplus.util.Util;
import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.config.gui.item.GUIItemManager;
import com.github.hwan.julyguildplus.gui.BaseConfirmGUI;
import com.github.hwan.julyguildplus.gui.BasePlayerGUI;
import com.github.hwan.julyguildplus.gui.GUI;
import com.github.hwan.julyguildplus.guild.GuildBank;
import com.github.hwan.julyguildplus.guild.member.GuildMember;
import com.github.hwan.julyguildplus.placeholder.PlaceholderContainer;
import com.github.hwan.julyguildplus.thirdparty.economy.PlayerPointsEconomy;
import com.github.hwan.julyguildplus.thirdparty.economy.VaultEconomy;
import com.github.hwan.julylib.inventory.ItemListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class GuildDonateGUI extends BasePlayerGUI {
    private enum PayType {
        MONEY, POINTS
    }

    private JulyGuildPlus plugin = JulyGuildPlus.inst();
    private final GuildMember guildMember;
    private final Guild guild;
    private final Player bukkitPlayer = getBukkitPlayer();
    private final ConfigurationSection thisGUISection = plugin.getGUIYaml("GuildDonateGUI");
    private final ConfigurationSection thisLangSection = plugin.getLangYaml().getConfigurationSection("GuildDonateGUI");
    private final PlayerPointsEconomy playerPointsEconomy = plugin.getPlayerPointsEconomy();
    private final VaultEconomy vaultEconomy = plugin.getVaultEconomy();

    protected GuildDonateGUI(@Nullable GUI lastGUI, @NotNull GuildMember guildMember) {
        super(lastGUI, Type.DONATE, guildMember.getGuildPlayer());

        this.guildMember = guildMember;
        this.guild = guildMember.getGuild();
    }


    @Override
    public boolean canUse() {
        return guildMember.isValid();
    }

    @Override
    public Inventory createInventory() {
        IndexConfigGUI.Builder guiBuilder = new IndexConfigGUI.Builder()
                .fromConfig(thisGUISection, bukkitPlayer);

        JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "items.back");
        guiBuilder.item(GUIItemManager.getIndexItem(thisGUISection.getConfigurationSection("items.back"), bukkitPlayer), new ItemListener() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (canBack()) {
                    back();
                }
            }
        });
        JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "items.back");

        if (thisGUISection.contains("donate_items")) {
            for (String itemName : thisGUISection.getConfigurationSection("donate_items").getKeys(false)) {
                ConfigurationSection itemSection = thisGUISection.getConfigurationSection("donate_items").getConfigurationSection(itemName);
                ConfigurationSection donateItemSection = itemSection.getConfigurationSection("donate");

                PayType payType = PayType.valueOf(donateItemSection.getString("pay_type"));
                double price = donateItemSection.getInt("price");
                double reward = donateItemSection.getDouble("reward");
                ConfigurationSection confirmGUISection = donateItemSection.getConfigurationSection("ConfirmGUI");

                JulyGuildPlusLogger.debug(DebugMessage.BEGIN_GUI_LOAD_ITEM, "donate_items." + itemName);
                guiBuilder.item(GUIItemManager.getIndexItem(itemSection, bukkitPlayer, new PlaceholderContainer()
                        .add("price", price)
                        .add("reward", reward)), new ItemListener() {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (payType == PayType.POINTS) {
                            if (playerPointsEconomy == null) {
                                Util.sendMsg(bukkitPlayer, "&cPlayerPoints 未启用.");
                                reopen(40L);
                                return;
                            }

                            if (!playerPointsEconomy.has(bukkitPlayer, (int) price)) {
                                Util.sendMsg(bukkitPlayer, thisLangSection.getString("points.not_enough"), new PlaceholderContainer()
                                        .add("need", price - playerPointsEconomy.getBalance(bukkitPlayer)));
                                reopen(40);
                                return;
                            }

                            new BaseConfirmGUI(GuildDonateGUI.this, guildPlayer, confirmGUISection, new PlaceholderContainer()
                                    .add("reward", reward)
                                    .add("price", price)) {
                                @Override
                                public boolean canUse() {
                                    return guildMember.isValid() && playerPointsEconomy.has(bukkitPlayer, (int) price);
                                }

                                @Override
                                public void onConfirm() {
                                    playerPointsEconomy.withdraw(bukkitPlayer, (int) price);
                                    guild.getGuildBank().deposit(GuildBank.BalanceType.GMONEY, new BigDecimal(reward));
                                    guildMember.addDonated(GuildBank.BalanceType.GMONEY, reward);
                                    Util.sendMsg(bukkitPlayer, thisLangSection.getString("points.success"), new PlaceholderContainer()
                                            .add("reward", reward)
                                            .add("price", price));
                                    close();
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            getLastGUI().open();
                                        }
                                    }.runTaskLater(plugin, 40L);
                                }

                                @Override
                                public void onCancel() {
                                    if (canBack()) {
                                        back();
                                    }
                                }
                            }.open();

                            return;
                        }

                        if (payType == PayType.MONEY) {
                            if (!vaultEconomy.has(bukkitPlayer, price)) {
                                Util.sendMsg(bukkitPlayer, thisLangSection.getString("money.not_enough"), new PlaceholderContainer()
                                        .add("need", price - vaultEconomy.getBalance(bukkitPlayer)));
                                reopen(40);
                                return;
                            }

                            new BaseConfirmGUI(GuildDonateGUI.this, guildPlayer, confirmGUISection, new PlaceholderContainer()
                                    .add("reward", reward)
                                    .add("price", price)) {
                                @Override
                                public boolean canUse() {
                                    return guildMember.isValid() && vaultEconomy.has(bukkitPlayer, price);
                                }

                                @Override
                                public void onConfirm() {
                                    vaultEconomy.withdraw(bukkitPlayer, price);
                                    guild.getGuildBank().deposit(GuildBank.BalanceType.GMONEY, new BigDecimal(reward));
                                    guildMember.addDonated(GuildBank.BalanceType.GMONEY, reward);
                                    Util.sendMsg(bukkitPlayer, thisLangSection.getString("money.success"), new PlaceholderContainer()
                                            .add("reward", reward)
                                            .add("price", price));
                                    close();
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            getLastGUI().open();
                                        }
                                    }.runTaskLater(plugin, 40L);
                                }

                                @Override
                                public void onCancel() {
                                    if (canBack()) {
                                        back();
                                    }
                                }
                            }.open();
                        }
                    }
                });
                JulyGuildPlusLogger.debug(DebugMessage.END_GUI_LOAD_ITEM, "donate_items." + itemName);
            }
        }

        return guiBuilder.build();
    }
}
