package com.github.hwan.julyguildplus;

import com.github.hwan.julyguildplus.command.GUICommand;
import com.github.hwan.julyguildplus.command.VersionCommand;
import com.github.hwan.julyguildplus.command.AdminCommand;
import com.github.hwan.julyguildplus.config.Shop;
import com.github.hwan.julyguildplus.config.setting.MainSettings;
import com.github.hwan.julyguildplus.guild.CacheGuildManager;
import com.github.hwan.julyguildplus.guild.GuildManager;
import com.github.hwan.julyguildplus.listener.*;
import com.github.hwan.julyguildplus.logger.JulyGuildPlusLogger;
import com.github.hwan.julyguildplus.player.GuildPlayer;
import com.github.hwan.julyguildplus.player.GuildPlayerManager;
import com.github.hwan.julyguildplus.request.RequestManager;
import com.github.hwan.julyguildplus.task.LoggerSaveTask;
import com.github.hwan.julyguildplus.task.RequestCleanTask;
import com.github.hwan.julyguildplus.thirdparty.PlaceholderAPIExpansion;
import com.github.hwan.julyguildplus.thirdparty.economy.PlayerPointsEconomy;
import com.github.hwan.julyguildplus.thirdparty.economy.VaultEconomy;
import com.github.hwan.julyguildplus.util.Util;
import com.github.hwan.julylib.JulyLib;
import com.github.hwan.julylib.commandv2.JulyCommandHandler;
import com.github.hwan.julylib.config.JulyConfig;
import com.github.hwan.julylib.utils.FileUtil;
import com.github.hwan.julylib.utils.YamlUtil;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 强制依赖：Vault
 * 软依赖：PlaceholderAPI, PlayerPoints
 */
public class JulyGuildPlus extends JavaPlugin {
    private final String[] GUI_RESOURCES = new String[] {
            "GuildCreateGUI.yml",
            "GuildInfoGUI.yml",
            "GuildMemberListGUI.yml",
            "GuildMineGUI.yml",
            "GuildDonateGUI.yml",
            "GuildJoinCheckGUI.yml",
            "GuildMemberManageGUI.yml",
            "GuildIconRepositoryGUI.yml",
            "MainGUI.yml" }; // GUI资源文件
    private final String[] CONFIG_RESOURCES = new String[] { "settings.yml", "lang.yml" }; // 根资源文件
    private final String[] SHOP_RESOURCES = new String[] { "Shop1.yml", "Shop2.yml" };
    private final String[] DEPEND_PLUGINS = new String[] { "Vault" };

    private static JulyGuildPlus instance;

    private Object placeholderAPIExpansion;
    private GuildManager guildManager;
    private GuildPlayerManager guildPlayerManager;
    private CacheGuildManager cacheGuildManager;
    private RequestManager requestManager;

    private JulyCommandHandler julyCommandHandler;
    private VaultEconomy vaultEconomy;
    private PlayerPointsEconomy playerPointsEconomy;
    private PluginManager pluginManager;

    private YamlConfiguration langYaml;
    private Map<String, YamlConfiguration> guiYamlMap = new HashMap<>();
    private Map<String, Shop> shopYamlMap = new HashMap<>();

    private File getConfigFolder() {
        return new File(getDataFolder(), "config");
    }

    private File getConfigFile(@NotNull String fileName) {
        return new File(getConfigFolder(), fileName);
    }

    private File getGUIFolder() {
        return new File(getDataFolder(), "config" + File.separator + "gui");
    }

    private File getGUIFile(@NotNull String fileName) {
        return new File(getGUIFolder(), fileName);
    }

    @Override
    public void onEnable() {
        instance = this;

        // 初始化 JulyLib
        JulyLib.initialize(this);

        JulyGuildPlusLogger.init();
        JulyGuildPlusLogger.info("插件版本: " + getDescription().getVersion() + ".");

        for (String pluginName : DEPEND_PLUGINS) {
            if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                JulyGuildPlusLogger.info("前置插件 " + pluginName + " 未被加载, 插件将被卸载!");
                setEnabled(false);
                return;
            }
        }

        this.pluginManager = Bukkit.getPluginManager();
        this.guildPlayerManager = new GuildPlayerManager();
        this.guildManager = new GuildManager();
        this.cacheGuildManager = new CacheGuildManager();
        this.requestManager = new RequestManager();
        this.julyCommandHandler = new JulyCommandHandler();

        loadConfig();

        if (MainSettings.isMetricsEnabled()) {
            int pluginId = 28963;
            new Metrics(this, pluginId);
            JulyGuildPlusLogger.info("bStats统计: 已启用");
        }

        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            this.placeholderAPIExpansion = new PlaceholderAPIExpansion();

            if (!((PlaceholderAPIExpansion) placeholderAPIExpansion).register()) {
                JulyGuildPlusLogger.error("PlaceholderAPI: Hook失败!");
            } else {
                JulyGuildPlusLogger.info("PlaceholderAPI: Hook成功!");
            }
        }

        if (!pluginManager.isPluginEnabled("Vault")) {
            JulyGuildPlusLogger.error("Vault: 未启用, 插件将被卸载!");
            setEnabled(false);
            return;
        } else {
            Economy tmp = setupEconomy();

            if (tmp == null) {
                JulyGuildPlusLogger.error("Vault: Hook失败, 插件将被卸载!");
                setEnabled(false);
                return;
            }

            this.vaultEconomy = new VaultEconomy(tmp);
            JulyGuildPlusLogger.info("Vault: Hook成功!");
        }

        if (pluginManager.isPluginEnabled("PlayerPoints")) {
            this.playerPointsEconomy = new PlayerPointsEconomy(
                    ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI());
            JulyGuildPlusLogger.info("PlayerPoints: Hook成功!");
        } else {
            JulyGuildPlusLogger.warning("PlayerPoints: 未启用");
        }

        guildManager.loadGuilds();
        requestManager.loadRequests();
        cacheGuildManager.startTask();

        julyCommandHandler
                .setCommandFormat(LangHelper.Global.getPrefix() + langYaml.getString("Command.command_format"));
        julyCommandHandler
                .setSubCommandFormat(LangHelper.Global.getPrefix() + langYaml.getString("Command.sub_command_format"));
        julyCommandHandler.setNoneMessage(LangHelper.Global.getPrefix() + langYaml.getString("Command.none"));
        julyCommandHandler.setNoPermissionMessage(LangHelper.Global.getPrefix() + langYaml.getString("Command.no_per"));
        julyCommandHandler.setOnlyConsoleCanUseMessage(
                LangHelper.Global.getPrefix() + langYaml.getString("Command.only_console_can_use"));
        julyCommandHandler.setOnlyPlayerCanUseMessage(
                LangHelper.Global.getPrefix() + langYaml.getString("Command.only_player_can_use"));

        getCommand("jgp").setExecutor(julyCommandHandler);

        registerCommands();
        registerListeners();
        runTasks();

        JulyGuildPlusLogger.info("载入了 " + guildManager.getGuilds().size() + "个 公会.");
        JulyGuildPlusLogger.info("载入了 " + requestManager.getRequests().size() + "个 请求.");
        JulyGuildPlusLogger.info("插件初始化完毕.");
    }

    @Override
    public void onDisable() {
        if (guildPlayerManager != null) {
            for (GuildPlayer guildPlayer : guildPlayerManager.getOnlineGuildPlayers()) {
                if (guildPlayer.getUsingGUI() != null) {
                    guildPlayer.getUsingGUI().close();
                }
            }
        }

        if (placeholderAPIExpansion != null) {
            // PlaceholderAPI.unregisterExpansion((PlaceholderExpansion)
            // placeholderAPIExpansion);
        }

        // 关闭 JulyLib
        JulyLib.shutdown();

        Bukkit.getScheduler().cancelTasks(this);
        JulyGuildPlusLogger.info("插件被卸载.");

        if (JulyGuildPlusLogger.isWriterEnabled()) {
            JulyGuildPlusLogger.closeWriters();
        }
    }

    public boolean isPlaceHolderAPIEnabled() {
        return pluginManager.isPluginEnabled("PlaceholderAPI");
    }

    private void runTasks() {
        new LoggerSaveTask().runTaskTimer(this, 0L, 20L);
        new RequestCleanTask().runTaskTimer(this, 0L, 20L * 60L);
    }

    public CacheGuildManager getCacheGuildManager() {
        return cacheGuildManager;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    private void registerListeners() {
        pluginManager.registerEvents(new GUIListener(), this);
        pluginManager.registerEvents(new MemberDamageListener(), this);
        pluginManager.registerEvents(new TeleportListener(), this);
        pluginManager.registerEvents(new TpAllListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new EssentialsChatListener(), this);
    }

    private Economy setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (economyProvider != null) {
            return economyProvider.getProvider();
        }

        return null;
    }

    private void registerCommands() {
        julyCommandHandler.registerCommand(new GUICommand());
        julyCommandHandler.registerCommand(new VersionCommand());
        julyCommandHandler.registerCommand(new AdminCommand());
    }

    /**
     * 重载配置文件
     */
    public void reloadPlugin() {
        guiYamlMap.clear();
        loadConfig();
        getGuildPlayerManager().getOnlineGuildPlayers().forEach(guildPlayer -> {
            if (guildPlayer.isUsingGUI()) {
                guildPlayer.closeInventory();
                Util.sendMsg(guildPlayer.getBukkitPlayer(), "&c插件配置重载被迫关闭GUI.");
            }
        });
        HandlerList.unregisterAll(this);
        registerListeners();
    }

    /**
     * 载入配置
     */
    private void loadConfig() {
        File defaultFolder = new File(getDataFolder(), "defaults");

        if (!defaultFolder.exists() && !defaultFolder.mkdirs()) {
            throw new RuntimeException("创建文件夹失败: " + defaultFolder);
        }

        File noticeFile = new File(defaultFolder, "不要在这个目录编辑配置文件 这是默认配置文件 供参考用");

        try {
            if (!noticeFile.exists() && !noticeFile.createNewFile()) {
                throw new RuntimeException("创建文件失败: " + noticeFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String fileName : CONFIG_RESOURCES) {
            File file = getConfigFile(fileName);
            String resourceName = "resources/" + fileName;

            FileUtil.saveResourceFile(this, resourceName, new File(defaultFolder, fileName + ".default"), true); // 保存一份默认的
            FileUtil.saveResourceFile(this, resourceName, file, false); // 创建文件如果不存在

            YamlConfiguration yaml = YamlUtil.loadYaml(file, StandardCharsets.UTF_8);
            Set<String> changes = YamlUtil.completeSection(yaml, YamlConfiguration.loadConfiguration(
                    new InputStreamReader(getResource("resources/" + fileName), StandardCharsets.UTF_8)));

            if (!changes.isEmpty()) {
                YamlUtil.saveYaml(yaml, file, StandardCharsets.UTF_8);
                changes.forEach(
                        s -> JulyGuildPlusLogger.warning("文件 " + file.getAbsolutePath() + " 节点 " + s + " 被补全."));
            }
        }

        for (String fileName : GUI_RESOURCES) {
            String resourceName = "resources/gui/" + fileName;
            File guiFile = getGUIFile(fileName);

            FileUtil.saveResourceFile(this, resourceName,
                    new File(defaultFolder, "gui" + File.separator + fileName + ".default"), true); // 保存一份默认的
            FileUtil.saveResourceFile(this, resourceName, guiFile, false);
        }

        for (String fileName : SHOP_RESOURCES) {
            String resourceName = "resources/shop/" + fileName;

            FileUtil.saveResourceFile(this, resourceName,
                    new File(defaultFolder, "shop" + File.separator + fileName + ".default"), true); // 保存一份默认的
            FileUtil.saveResourceFile(this, resourceName, getShopFile(fileName), false);
        }

        File configFile = getConfigFile("settings.yml");
        YamlConfiguration configYaml;

        try {
            configYaml = YamlUtil.loadYaml(configFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("读取文件异常: " + configFile.getAbsolutePath(), e);
        }

        JulyConfig.loadConfig(configYaml, MainSettings.class);

        for (String fileName : GUI_RESOURCES) {
            File guiFile = getGUIFile(fileName);
            YamlConfiguration guiYaml;

            try {
                guiYaml = YamlUtil.loadYaml(guiFile, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("读取文件异常: " + guiFile.getAbsolutePath(), e);
            }

            guiYamlMap.put(FileUtil.getFileName(guiFile), guiYaml);
        }

        File[] shopFiles = new File(getDataFolder(), "config" + File.separator + "shop").listFiles();

        // noinspection ConstantConditions
        for (File shopFile : shopFiles) {
            YamlConfiguration shopYaml;

            try {
                shopYaml = YamlUtil.loadYaml(shopFile, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("读取文件异常: " + shopFile.getAbsolutePath(), e);
            }

            String shopName = shopYaml.getString("name");

            if (shopName == null) {
                throw new RuntimeException("name 未设置: " + shopFile.getAbsolutePath());
            }

            shopYamlMap.put(shopName, new Shop(shopName, shopYaml));
        }

        File langFile = getConfigFile("lang.yml");

        try {
            this.langYaml = YamlUtil.loadYaml(langFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("读取文件异常: " + langFile.getAbsolutePath(), e);
        }
    }

    public File getShopFile(@NotNull String fileName) {
        return new File(getDataFolder(), "config" + File.separator + "shop" + File.separator + fileName);
    }

    public Shop getShop(@NotNull String shopName) {
        return shopYamlMap.get(shopName);
    }

    public YamlConfiguration getLangYaml() {
        return langYaml;
    }

    public YamlConfiguration getGUIYaml(@NotNull String guiName) {
        return guiYamlMap.get(guiName);
    }

    public VaultEconomy getVaultEconomy() {
        return vaultEconomy;
    }

    public PlayerPointsEconomy getPlayerPointsEconomy() {
        return playerPointsEconomy;
    }

    public boolean isVaultEconomyHooked() {
        return vaultEconomy != null;
    }

    public boolean isPlayerPointsHooked() {
        return playerPointsEconomy != null;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public GuildPlayerManager getGuildPlayerManager() {
        return guildPlayerManager;
    }

    public static JulyGuildPlus inst() {
        return instance;
    }
}