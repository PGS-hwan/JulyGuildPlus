package com.github.hwan.julyguildplus;

import com.github.hwan.julyguildplus.guild.member.GuildMember;
import com.github.hwan.julyguildplus.guild.member.GuildPosition;
import com.github.hwan.julyguildplus.placeholder.PlaceholderContainer;
import com.github.hwan.julyguildplus.placeholder.PlaceholderText;
import com.github.hwan.julylib.message.DateTimeUnit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class LangHelper {
    public static class Global {
        public static DateTimeUnit getDateTimeUnit() {
            ConfigurationSection section = JulyGuildPlus.inst().getLangYaml().getConfigurationSection("Global");

            return new DateTimeUnit(section.getString("year"), section.getString("month"), section.getString("day"), section.getString("hour"), section.getString("minute"), section.getString("second"));
        }

        public static SimpleDateFormat getDateTimeFormat() {
            return new SimpleDateFormat(JulyGuildPlus.inst().getLangYaml().getString("Global.date_time_format"));
        }

        public static String getPrefix() {
            return JulyGuildPlus.inst().getLangYaml().getString("Global.prefix");
        }

        public static String getNickName(@NotNull GuildMember guildMember) {
            ConfigurationSection langSection = JulyGuildPlus.inst().getLangYaml();
            String format = langSection.getString("Global.nick_name");

            return PlaceholderText.replacePlaceholders(format, new PlaceholderContainer()
                    .add("PERMISSION", langSection.getString("Guild.Position." + guildMember.getPosition().name().toLowerCase()))
                    .add("NAME", guildMember.getName()));
        }

        public static String getPositionName(@NotNull GuildPosition guildPosition) {
            return JulyGuildPlus.inst().getLangYaml().getString("Guild.Position." + guildPosition.name().toLowerCase());
        }
    }
}
