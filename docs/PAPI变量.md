# 🔌 PlaceholderAPI 变量

## 📊 公会变量

| 变量名 | 返回值 |
|--------|--------|
| `%guild_name%` | 公会名称 |
| `%guild_owner%` | 公会会长名 |
| `%guild_ranking%` | 公会排名 |
| `%guild_member_count%` | 当前成员数量 |
| `%guild_max_member_count%` | 最大成员数量 |
| `%guild_bank_gmoney%` | 公会币储备 |
| `%guild_online_member_count%` | 在线成员数量 |
| `%guild_creation_time%` | 公会创建时间 |

## 👤 成员变量

| 变量名 | 返回值 |
|--------|--------|
| `%guild_member_position%` | 成员职位（成员/副会长/会长） |
| `%guild_member_donated_gmoney%` | 成员已赞助的公会币数量 |
| `%guild_member_join_time%` | 成员加入时间 |

## 💬 聊天集成

### 在 TrChat 中使用
1. 编辑 `plugins/TrChat/formats/default.yml`
2. 在 `prefix` 或 `msg` 中插入变量：

```yaml
part-before-player:
  text: '[公会：%guild_name%]'
```

### 在 Essentials 中使用
1. 编辑 plugins/Essentials/config.yml
2. 修改 format 字段：

```yaml
format: '<[DISPLAYNAME]> [%guild_name%] [MESSAGE]'
```

> 💡 提示: 所有 PlaceholderAPI 变量表里的变量都能使用，只需要把 %内容% 改成 <内容>