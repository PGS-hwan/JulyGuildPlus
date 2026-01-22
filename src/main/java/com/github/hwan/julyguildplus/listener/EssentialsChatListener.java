package com.github.hwan.julyguildplus.listener;

import com.github.hwan.julyguildplus.JulyGuildPlus;
import com.github.hwan.julyguildplus.LangHelper;
import com.github.hwan.julyguildplus.config.setting.MainSettings;
import com.github.hwan.julyguildplus.guild.CacheGuildManager;
import com.github.hwan.julyguildplus.guild.Guild;
import com.github.hwan.julyguildplus.guild.GuildBank;
import com.github.hwan.julyguildplus.guild.member.GuildMember;
import com.github.hwan.julyguildplus.player.GuildPlayer;
import com.github.hwan.julyguildplus.player.GuildPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 用于支持 Essentials 插件聊天
 */
public class EssentialsChatListener implements Listener {
	private CacheGuildManager cacheGuildManager = JulyGuildPlus.inst().getCacheGuildManager();
	private GuildPlayerManager guildPlayerManager = JulyGuildPlus.inst().getGuildPlayerManager();

	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String originalFormat = event.getFormat();
		StringBuilder finalFormat = new StringBuilder();

		for (int i = 0; i < originalFormat.length(); i++) {
			char c = originalFormat.charAt(i);

			if (c == '<') {
				int left = i;
				int right = originalFormat.indexOf(">", i);

				if (right == -1) {
					finalFormat.append(c);
					continue;
				}

				String tmp = originalFormat.substring(left, right + 1);

				if (!tmp.startsWith("<guild_")) {
					finalFormat.append(c);
					continue;
				}

				String placeholder = tmp.substring("<guild_".length(), tmp.length() - 1);

				i = right;

				finalFormat.append(Optional.ofNullable(handlePlaceholder(player, placeholder)).orElse(tmp));
			} else {
				finalFormat.append(c);
			}
		}

		event.setFormat(finalFormat.toString());
	}

	private String handlePlaceholder(@NotNull Player player, @NotNull String params) {
		GuildPlayer guildPlayer = guildPlayerManager.getGuildPlayer(player);
		Guild guild = guildPlayer.getGuild();
		boolean isInGuild = guild != null;

		if (params.equalsIgnoreCase("is_in_guild")) {
			return String.valueOf(isInGuild);
		}

		if (!isInGuild) {
			return MainSettings.getGuildEssChatNotStr();
		}

		GuildMember guildMember = guild.getMember(guildPlayer);
		GuildBank guildBank = guild.getGuildBank();

		switch (params.toLowerCase()) {
			case "name":
				return guild.getName();
			case "member_position":
				return LangHelper.Global.getPositionName(guildMember.getPosition());
			case "member_donated_gmoney":
				return LangHelper.Global.getDateTimeFormat().format(guildMember.getDonated(GuildBank.BalanceType.GMONEY));
			case "member_join_time":
				return LangHelper.Global.getDateTimeFormat().format(guildMember.getJoinTime());
			case "ranking":
				return String.valueOf(cacheGuildManager.getRanking(guild));
			case "owner":
				return guild.getOwner().getName();
			case "member_count":
				return String.valueOf(guild.getMemberCount());
			case "max_member_count":
				return String.valueOf(guild.getMaxMemberCount());
			case "creation_time":
				return LangHelper.Global.getDateTimeFormat().format(guild.getCreateTime());
			case "bank_gmoney":
				return guildBank.getBalance(GuildBank.BalanceType.GMONEY).toString();
			case "online_member_count":
				return String.valueOf(guild.getOnlineMembers().size());
		}

		return null;
	}
}
