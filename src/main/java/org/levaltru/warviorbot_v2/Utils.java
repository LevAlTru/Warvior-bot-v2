package org.levaltru.warviorbot_v2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;
import java.util.function.Function;

import static org.levaltru.warviorbot_v2.Warviorbot_v2.*;

public class Utils {

    public static final String NULL_STRING = "`null`";
    private static FileConfiguration config;

    public static void init() {
        Warviorbot_v2.send("loading configs");
        if (config == null) {
            config = Warviorbot_v2.plugin.getConfig();
            save();
        }
        Warviorbot_v2.send("finished loading configs");
    }

    public static void save() {
        for (ConfigThings value : ConfigThings.values()) config.set(value.configName, value.value);
        Warviorbot_v2.plugin.saveConfig();
    }

    public static void setSetting(ConfigThings setting, String string) {
        setting.value = string;
        save();
    }

    public static boolean addConnection(String nickname, final Member member, final Member odmin) {
        if (!canAddWhitelist(odmin)) return sendDM(odmin, "Вы не одмин или хотя бы даже не хелпер чтобы добавлять в вайтлист >=d");

        if (isPlayer(member))
            return sendDM(odmin, "Игрок " + member.getAsMention() + " уже имеет роль игрока\nесли игрок все ровно не может зайти на сервер заставьте <@300626304221446145> (← создатель бота) или других выше стоящих работать или хотя бы проверить конфиги сервера");

        if (member.getUser().isBot())
            return sendDM(odmin, "Запрещаю добавлять ботов в вайтлист (" + member.getAsMention() + ")");

        if (nickname == null) {
            return sendDM(odmin, "Ник is null");
        }

        HashMap<String, String> hashMap = getConnectionHashMap();
        if (whitelisted(member)) {
            sendDM(odmin,
                    member.getAsMention() + " уже находится в белом списке.     юзер: " + member.getAsMention() + "     ник: " + getNickFromMember(member) +
                    "\nfun fact: это сообщение __НЕ ДОЛЖНО ПОЯВЛЯТСЯ__. aka свяжитесь с <@300626304221446145> потому что так быть не должно" +
                    "\nНо роль игрока была выдана потому что повторюсь, __так быть не должно__");
            return false;
        }

        String nicknameLowercased = nickname.toLowerCase();
        if (whitelisted(nicknameLowercased)) {
            sendDM(odmin, "Ник человека " + member.getAsMention() + " уже находится в белом списке.     ник: " + nicknameLowercased + "     владелец: " + jda.getUserById(getKeyByValue(hashMap, nicknameLowercased)).getAsMention());
            return false;
        }

        member.getGuild().addRoleToMember(member, jda.getRoleById(ConfigThings.PLAYER_ROLE.getAsString())).queue();
        return addOrReplaceConnection(nickname, member, odmin);
    }

    public static boolean addOrReplaceConnection(String nickname, final Member member, final Member odmin) {
        if (nickname == null) {
            return sendDM(odmin, "Ник is null");
        }
        boolean whitelisted = false;
        String nick = NULL_STRING;
        if (whitelisted(member)) {
            whitelisted = true;
            nick = getNickFromMember(member).orElse(nick);
        }
        try {
            guild.modifyNickname(member, nickname).queue(); // Users with higher role will make this like go crazy (this is a case with WarD and with Blin (if he still has the role))
        } catch (HierarchyException e) {}
        nickname = nickname.toLowerCase();

        config.set("data.connections." + member.getId(), nickname);
        if (!whitelisted) {
            jda.getChannelById(TextChannel.class, ConfigThings.WHITELIST_LOG_CHANNEL.getAsString()).sendMessageEmbeds(new EmbedBuilder().setColor(0x00FF00)
                    .setTitle("Whitelisting")
                    .addField("Одмин:", odmin.getAsMention(), true)
                    .addField("Принятый:", member.getAsMention(), true)
                    .addField("Ник принятого:", nickname, true).build()).queue();
            sendDM(member.getUser(), it -> it.sendMessage(new MessageCreateBuilder()
                            .addEmbeds(new EmbedBuilder()
                                    .setColor(0x88FF00)
                                    .setTitle("ВАС ПРИНЯЛИ!!!")
                                    .setDescription("После вашей поданной заявки, наши жестокие надзиратели (а именно " + odmin.getAsMention() + ") подумали что вы подходите для нашего милого, маленького [Майнкрафт сообщества](https://discord.gg/kkmHGXkFNV).")
                                    .setThumbnail("https://cdn.discordapp.com/attachments/872104343682285589/1242136045068226640/warbot_avatar_assept.png?ex=664cbcd6&is=664b6b56&hm=0d67a229680fc8c5f84300a2f5e549a60c546da5db1a70ab26de1eb83225e11d&")
                                    .setFooter("Приятной игры :3")
                                    .build()
                            )
                            .build()
                    ));
        } else {
            String finalNickname = nickname;
            String finalNick = nick;
            jda.getChannelById(TextChannel.class, ConfigThings.WHITELIST_LOG_CHANNEL.getAsString()).sendMessageEmbeds(new EmbedBuilder().setColor(0xAAFF00)
                    .setTitle("Whitelisting (changing)")
                    .addField("Одмин:", odmin.getAsMention(), true)
                    .addField("Подопытный:", member.getAsMention(), true)
                    .addField("Прошлый ник:", nick, true)
                    .addField("Нынешний ник:", nickname, true).build()).queue();
            sendDM(member.getUser(), it -> it.sendMessage(new MessageCreateBuilder()
                    .addEmbeds(new EmbedBuilder()
                            .setColor(0xFFaa00)
                            .setTitle("Вам сменили ник")
                            .setDescription("Ваш ник был изменен. Причина изменения ника может разнится, но в большинстве случаев в нике просто опечатываются.")
                            .setThumbnail("https://cdn.discordapp.com/attachments/872104343682285589/1242165886765371435/warbot_avatar_warnin.png?ex=664cd8a1&is=664b8721&hm=9140c38c92a96fdee1d760d23a33e4874eed3d4e0d777ba259750ba0b75879a5&")
                            .setFooter("(не важно большие ли буквы в нике или маленькие)")
                            .addField("Прошлый ник:", finalNick, true)
                            .addField("Нынешний ник:", finalNickname, true)
                            .addField("Кто поменял:", odmin.getAsMention(), true)
                            .build()
                    )
                    .build()
            ));
        }

        save();
        setPlayerRoles(member);
        return true;
    }

    public static boolean addBan(String username, final double hours, @Nullable String reason, Member odmin) {
        if (!canBan(odmin)) {
            return sendDM(odmin, "Вы не одмин чтобы банить людей >=d");
        }
        if (username == null) {
            return sendDM(odmin, "Ник is null");
        }
        if (isBanned(username)) {
            return sendDM(odmin, "Человек уже забанен. Если вы хотите забанить еще раз (например чтобы поменять причину бана) то используйте pardon и потом еще раз ban");
        }

        username = username.toLowerCase();
        if (reason == null || reason.isEmpty()) reason = "Не указана";

        if (hours >= 0) config.set("data.banlist." + username + ".until", countUntil(hours));
        else config.set("data.banlist." + username + ".until", -1);
        config.set("data.banlist." + username + ".reason", reason);

        String odminMention = Optional.ofNullable(odmin).map(thisOdmin -> thisOdmin.getAsMention()).orElse(NULL_STRING);
        Optional<Member> memberFromNick = getMemberFromNick(username);
        String asMentionNick = memberFromNick.map(thisMember -> thisMember.getAsMention()).orElse(NULL_STRING);

        String timeString = ServerEvents.remainingTime(username);
        jda.getChannelById(TextChannel.class, ConfigThings.WHITELIST_LOG_CHANNEL.getAsString()).sendMessageEmbeds(new EmbedBuilder().setColor(0xFF0000)
                .setTitle("Banning")
                .addField("Одмин:", odminMention, true)
                .addField("Забаненный:", asMentionNick, true)
                .addField("Ник заб.:", username, true)
                .addField("На:", timeString, true)
                .addField("Причина:", reason, true).build()).queue();
        String finalReason = reason;
        memberFromNick.ifPresent(member -> sendDM(member.getUser(), it -> it.sendMessage(new MessageCreateBuilder()
                .addEmbeds(new EmbedBuilder()
                        .setColor(0xFFaa00)
                        .setTitle("Вы словили по лицу (вас забанили)")
                        .setThumbnail("https://cdn.discordapp.com/attachments/872104343682285589/1242189404802383913/warbot_avatar_nuh.png?ex=664cee88&is=664b9d08&hm=a510d9b4ffec4e93d25d95cbe20c46bb0aa20b65e318ef441f546661aed3156f&")
                        .addField("На:", timeString + " ("+ TimeFormat.RELATIVE.now().plus(calcHours(hours)) +")", true)
                        .addField("Причина:", finalReason, true)
                        .build()
                )
                .build()
        )));

        save();
        memberFromNick.ifPresent(Utils::setBanRoles);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(username)) {
                Bukkit.getScheduler().runTask(Warviorbot_v2.plugin, () -> player.kick(Component.text("Вы сделали что то плохое ;-;").color(TextColor.color(16733525))));
                break;
            }
        }
        return true;
    }

    public static boolean addBan(net.dv8tion.jda.api.entities.Member member, double hours, @Nullable String reason, net.dv8tion.jda.api.entities.Member odmin) {
        return addBan(getNickFromMember(member).orElse(null), hours, reason, odmin);
    }

    public static boolean removeBan(String username, Member odmin) {
        if (!canBan(odmin)) {
            return sendDM(odmin, "Вы не одмин чтобы разбанить людей >=d");
        }
        if (!isBanned(username)) {
            return sendDM(odmin, "Человек не в бане");
        }
        String reason = getReason(username);

        username = username.toLowerCase();
        config.set("data.banlist." + username, null);

        String odminMention = Optional.ofNullable(odmin).map(IMentionable::getAsMention).orElse(NULL_STRING);
        Optional<Member> memberFromNick = getMemberFromNick(username);
        String asMentionNick = memberFromNick.map(IMentionable::getAsMention).orElse(NULL_STRING);

        jda.getChannelById(TextChannel.class, ConfigThings.WHITELIST_LOG_CHANNEL.getAsString()).sendMessageEmbeds(new EmbedBuilder().setColor(0xFFFF00)
                .setTitle("Unbanning")
                .addField("Одмин:", odminMention, true)
                .addField("Разбаненный:", asMentionNick, true)
                .addField("Ник забаненного:", username, true).build()).queue();
        memberFromNick.ifPresent(it -> {
            setPlayerRoles(it);
            sendDM(it.getUser(), it2 -> {
                return it2.sendMessage(MessageCreateData.fromEmbeds(new EmbedBuilder()
                        .setTitle("Вас разбанили")
                        .setDescription("Вы были разбанены на сервере под именем \"Warvior\" на котором вы получили бан.\nВас разбанили раньше времени конца вашего бана")
                        .addField("Вы были забанены по причине:", reason, true)
                        .setThumbnail("https://cdn.discordapp.com/attachments/872104343682285589/1242218525498806505/warbot_avatar_green.png?ex=664d09a7&is=664bb827&hm=69a3ef5b7ef8d145af1d28c4e1791fe33f55e3ff6a533a73855e3beb78982012&")
                        .setColor(0xcc0000)
                        .build()
                ));
            });
        });
        save();
        return true;
    }

    public static boolean removeBan(String username) {
        String finalUsername = username.toLowerCase();
        String reason = getReason(finalUsername);
        if (!config.contains("data.banlist." + finalUsername)) return false;
        config.set("data.banlist." + finalUsername, null);

        Optional<Member> memberFromNick = getMemberFromNick(finalUsername);
        String asMentionNick = memberFromNick.map(IMentionable::getAsMention).orElse(NULL_STRING);

        jda.getChannelById(TextChannel.class, ConfigThings.WHITELIST_LOG_CHANNEL.getAsString()).sendMessageEmbeds(new EmbedBuilder().setColor(0xFFFF00)
                .setTitle("Unbanning (system)")
                .addField("Разбаненный:", asMentionNick, true)
                .addField("Ник забаненного:", finalUsername, true).build()).queue();
        memberFromNick.ifPresent(it -> {
            setPlayerRoles(it);
            sendDM(it.getUser(), it2 -> {
                return it2.sendMessage(MessageCreateData.fromEmbeds(new EmbedBuilder()
                        .setTitle("Ваш бан истек")
                        .setDescription("Вы были разбанены на сервере под именем \"Warvior\", на котором вы получили бан.")
                        .addField("Вы были забанены по причине:", reason, true)
                        .setThumbnail("https://cdn.discordapp.com/attachments/872104343682285589/1242218525498806505/warbot_avatar_green.png?ex=664d09a7&is=664bb827&hm=69a3ef5b7ef8d145af1d28c4e1791fe33f55e3ff6a533a73855e3beb78982012&")
                        .setColor(0xcc0000)
                        .build()
                ));
            });
        });
        save();
        return true;
    }

    public static boolean removeBan(Member member, Member odmin) {
        return removeBan(getNickFromMember(member).orElse(""), odmin);
    }

    private static long countUntil(double hours) {
        return (System.currentTimeMillis() + calcHours(hours));
    }

    private static long calcHours(double hours) {
        return (long) (hours * 3_600_000) + 30000;
    }

    /**
     * @return Returns 0 if person wasn't banned at least once
     */
    public static long getUntil(String username) {
        if (username == null) return 0;
        return config.getLong("data.banlist." + username.toLowerCase() + ".until", 0);
    }

    /**
     * @return Returns NULL_STRING if person wasn't banned at least once
     */
    public static String getReason(String username) {
        if (username == null) return NULL_STRING;
        return config.getString("data.banlist." + username.toLowerCase() + ".reason", NULL_STRING);
    }

    public static Optional<String> getNickFromMember(net.dv8tion.jda.api.entities.Member member) {
        if (member == null) Optional.empty();
        Map<String, String> values = getConnectionHashMap();
        String string = values.get(member.getId());
        return Optional.ofNullable(string);
    }

    public static Optional<Member> getMemberFromNick(String nickname) {
        Map<String, String> values = getConnectionHashMap();
        String keyByValue = getKeyByValue(values, nickname.toLowerCase());
        if (keyByValue == null) Optional.empty();
        return Optional.ofNullable(guild.getMemberById(keyByValue));
    }

    @NotNull
    public static HashMap<String, String> getConnectionHashMap() {
        ConfigurationSection section = config.getConfigurationSection("data.connections");
        HashMap<String, String> stringMap = new HashMap<>();
        if (section != null) {
            HashMap<String, Object> values = (HashMap<String, Object>) section.getValues(false);
            values.forEach((id, nickname) -> stringMap.put(id, (String) nickname));
        }
        return stringMap;
    }

    @NotNull
    public static Collection<String> getBannedNicks() {
        ConfigurationSection section = config.getConfigurationSection("data.banlist");
        if (section != null) return section.getValues(false).keySet();
        return Collections.emptyList();
    }

    public static boolean sendDM(net.dv8tion.jda.api.entities.User user, Function<PrivateChannel, MessageCreateAction> message) {
        user.openPrivateChannel().flatMap(message).queue();
        return false;
    }

    public static boolean sendDM(net.dv8tion.jda.api.entities.User user, String string) {
        return sendDM(user, thisChannel -> thisChannel.sendMessage(string));
    }

    public static boolean sendDM(net.dv8tion.jda.api.entities.Member member, String string) {
        return sendDM(member.getUser(), string);
    }



    public static void setBanRoles(Member member) {
        member.getGuild().addRoleToMember(member, Objects.requireNonNull(jda.getRoleById(ConfigThings.BANNED_ROLE.getAsString()))).queue();
        member.getGuild().removeRoleFromMember(member, Objects.requireNonNull(jda.getRoleById(ConfigThings.PLAYER_ROLE.getAsString()))).queue();
    }

    public static void setPlayerRoles(Member member) {
        member.getGuild().addRoleToMember(member, Objects.requireNonNull(jda.getRoleById(ConfigThings.PLAYER_ROLE.getAsString()))).queue();
        member.getGuild().removeRoleFromMember(member, Objects.requireNonNull(jda.getRoleById(ConfigThings.BANNED_ROLE.getAsString()))).queue();
    }

    public static void setNoneRoles(Member member) {
        member.getGuild().removeRoleFromMember(member, Objects.requireNonNull(jda.getRoleById(ConfigThings.PLAYER_ROLE.getAsString()))).queue();
        member.getGuild().removeRoleFromMember(member, Objects.requireNonNull(jda.getRoleById(ConfigThings.BANNED_ROLE.getAsString()))).queue();
    }



    public static boolean isBanned(String nickname) {
        if (nickname == null || nickname.isEmpty()) return false;
        long until = getUntil(nickname.toLowerCase());
        return System.currentTimeMillis() < until || until < 0;
    }

    public static boolean isBanned(Member member) {
        return isBanned(getNickFromMember(member).orElse(null));
    }

    public static boolean isPlayer(String nickname) {
        if (nickname == null) return false;
        nickname = nickname.toLowerCase();
        if (whitelisted(nickname)) {
            if (isBanned(nickname)) return false;
            Optional<Member> memberFromNick = Utils.getMemberFromNick(nickname);
            if (memberFromNick.isPresent()) {
                Member user = memberFromNick.get();
                if (!user.getRoles().contains(ConfigThings.PLAYER_ROLE.value)) user.getGuild().addRoleToMember(user, jda.getRoleById(ConfigThings.PLAYER_ROLE.getAsString()));
            }
            return true;
        }
        return false;
    }

    public static boolean whitelisted(String nickname) {
        return getConnectionHashMap().containsValue(nickname.toLowerCase());
    }

    public static boolean whitelisted(Member member) {
        return getConnectionHashMap().containsKey(member.getId());
    }

    public static boolean whitelistedID(String id) {
        return getConnectionHashMap().containsKey(id);
    }

    public static boolean isPlayer(Member member) {
        return isPlayer(getNickFromMember(member).orElse(null));
    }

    public static boolean canBan(Member odmin) {
        return odmin.getRoles().contains(jda.getRoleById(ConfigThings.ADMIN_ROLE.getAsString()));
    }

    public static boolean canAddWhitelist(Member odmin) {
        return odmin.getRoles().contains(jda.getRoleById(ConfigThings.WHITELIST_ADDERS_ROLE.getAsString())) || canBan(odmin);
    }



    public static @NotNull String getBanDiscordTimestemp(String nick) {
        return TimeFormat.RELATIVE.now().plus(Utils.getUntil(nick) - System.currentTimeMillis()).toString();
    }


    static void addToLockList(String nickname) {
        List<String> asArrayString = ConfigThings.LOCKED_LIST.getAsArrayString();
        asArrayString.add(nickname.toLowerCase());
        ConfigThings.LOCKED_LIST.value = asArrayString;
    }

    static void removeFromLockList(String nickname) {
        List<String> asArrayString = ConfigThings.LOCKED_LIST.getAsArrayString();
        asArrayString.remove(nickname.toLowerCase());
        ConfigThings.LOCKED_LIST.value = asArrayString;
    }

    static boolean isInLockList(String nickname) {
        return ConfigThings.LOCKED_LIST.getAsArrayString().contains(nickname.toLowerCase());
    }


    @Nullable
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) return entry.getKey();
        }
        return null;
    }



    // ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ loads automatically when plugins turns on ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ //
    public enum ConfigThings {
        // bot something something
        TOKEN("bot-token"),
        BOT_STATUS("bot-status", "Приглядываю за ВарВиором"),

        // roles
        ADMIN_ROLE("bot-admin-role"),
        WHITELIST_ADDERS_ROLE("bot-whitelist-adders-role"),
        PLAYER_ROLE("bot-player-role"),
        BANNED_ROLE("bot-banned-role"),

        // channels
        WHITELIST_LOG_CHANNEL("bot-whitelist-log-channel"),

        // locking things
        IS_LOCKED("locked", true),
        LOCKED_LIST("data.locklist", new ArrayList<String>())

        ;
        private final String configName;
        Object value;

        ConfigThings(String configName, @Nullable Object defaultValue) {
            this.configName = configName;

            if (config.contains(configName)) this.value = config.get(configName);
            else if (defaultValue == null) {
                for (int i = 0; i < 10; i++) send(Component.text("PLEASE SET " + configName + " OR THE BOT WILL NOT WORK!!!").color(NamedTextColor.RED));
                this.value = "null";
            } else this.value = defaultValue;
        }

        ConfigThings(String configName) { this(configName, null); }

        String getAsString() {
            return value.toString();
        }

        boolean getAsBoolean() {
            return (boolean) value;
        }

        List<String> getAsArrayString() {
            return (List<String>) value;
        }

    }
}