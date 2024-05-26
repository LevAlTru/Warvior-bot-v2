package org.levaltru.warviorbot_v2;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.levaltru.warviorbot_v2.MinecraftCommands.WbanCommand;
import org.levaltru.warviorbot_v2.MinecraftCommands.WgetbanCommand;
import org.levaltru.warviorbot_v2.MinecraftCommands.WpardonCommand;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Warviorbot_v2 extends JavaPlugin {

    public static JavaPlugin plugin;
    public static JDA jda;
    public static Guild guild;

    @Override
    public void onEnable() {
        try {
            plugin = this;
            Utils.init();

            try {
                send(Component.text("Creating bot...").color(NamedTextColor.YELLOW));
                jda = JDABuilder.createDefault(Utils.ConfigThings.TOKEN.getAsString())
                        .enableIntents(EnumSet.allOf(GatewayIntent.class))
                        .setChunkingFilter(ChunkingFilter.ALL)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .addEventListeners(new BotEvents())
                        .setStatus(OnlineStatus.ONLINE)
                        .setActivity(Activity.customStatus(Utils.ConfigThings.BOT_STATUS.getAsString()))
                        .build();
                send(Component.text("Waiting for a response from discord...").color(NamedTextColor.YELLOW));
                jda.awaitReady();
                //send(Component.text("Changing profile things...").color(NamedTextColor.YELLOW));
                //try {
                //    jda.getSelfUser().getManager().setAvatar(Icon.from(new File("D:\\!Main\\warvior_related_stuff\\warbot avatar.png"))).queue();
                //} catch (IOException e) {
                //    throw new RuntimeException(e);
                //}
                //jda.getChannelById(TextChannel.class, Utils.ConfigThings.WHITELIST_LOG_CHANNEL.value).getGuild().getSelfMember().modifyNickname("Cлуга Народа").queue();
                //jda.awaitReady();
                send(Component.text("Bot was successfully turned on!").color(NamedTextColor.GREEN));
                send(Component.text("Adding bot stuff...").color(NamedTextColor.YELLOW));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            guild = Objects.requireNonNull(jda.getChannelById(TextChannel.class, Utils.ConfigThings.WHITELIST_LOG_CHANNEL.getAsString())).getGuild();
            guild.getSelfMember().modifyNickname("Слуга народа").queue();

            try {
                guild.updateCommands().addCommands(
                        new CommandDataImpl(Command.Type.USER, "Добавить в вайтлист")
                                .setDefaultPermissions(DefaultMemberPermissions.DISABLED),


                        new CommandDataImpl("ban_user", "Для того чтобы забанить человека на сервере и выдать роли в Дискорде")
                                .addOption(OptionType.USER, "user", "Кого забанить", true)
                                .addOption(OptionType.NUMBER, "duration", "На сколько минут", true)
                                .addOption(OptionType.STRING, "reason", "Причина", false)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),

                        new CommandDataImpl("ban_nickname", "Для того чтобы забанить человека на сервере и выдать роли в Дискорде")
                                .addOption(OptionType.STRING, "nickname", "Кого забанить", true)
                                .addOption(OptionType.NUMBER, "duration", "На сколько минут", true)
                                .addOption(OptionType.STRING, "reason", "Причина", false)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),

                        new CommandDataImpl("pardon_user", "Для того чтобы разбанить человека на сервере и в Дискорде")
                                .addOption(OptionType.USER, "user", "Кого разбанить", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),

                        new CommandDataImpl("pardon_nickname", "Для того чтобы разбанить человека на сервере и в Дискорде")
                                .addOption(OptionType.STRING, "nickname", "Кого разбанить", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),

                        new CommandDataImpl("get_ban_user", "Для того чтобы узнать за что и на сколько забанили человека")
                                .addOption(OptionType.USER, "user", "Кого посмотреть", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),

                        new CommandDataImpl("get_ban_nickname", "Для того чтобы узнать за что и на сколько забанили человека")
                                .addOption(OptionType.STRING, "nickname", "Кого посмотреть", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),


                        new CommandDataImpl("say", "Чтобы сказать что то как бот")
                                .addOption(OptionType.STRING, "text", "Что сказать", true)
                                .addOption(OptionType.STRING, "replying", "Кому ответить", false)
                                .addOption(OptionType.BOOLEAN, "ping", "Пингануть ответчика?", false)
                                .setDefaultPermissions(DefaultMemberPermissions.DISABLED),

                        new CommandDataImpl("say_modal", "Чтобы сказать что то как бот (modal)")
                                .addOption(OptionType.STRING, "json", "Что сказать", true)
                                .addOption(OptionType.STRING, "replying", "Кому ответить", false)
                                .addOption(OptionType.BOOLEAN, "ping", "Пингануть ответчика?", false)
                                .setDefaultPermissions(DefaultMemberPermissions.DISABLED),


                        new CommandDataImpl("get_connections", "Получить людей находящихся в датабазе")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),

                        new CommandDataImpl("get_bans", "Получить людей находящихся в датабазе")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),



                        new CommandDataImpl("lock", "Чтобы заблокировать людей от захода на сервер")
                                .addOption(OptionType.BOOLEAN, "boolean", "Включить или выключить", false)
                                .setDefaultPermissions(DefaultMemberPermissions.DISABLED),

                        new CommandDataImpl("lock_add", "Чтобы разблокировать некоторых людей для захода на сервер")
                                .addOption(OptionType.STRING, "nickname", "Кого добавить", true)
                                .setDefaultPermissions(DefaultMemberPermissions.DISABLED),

                        new CommandDataImpl("lock_remove", "Чтобы убрать разблокировку некоторых людей для захода на сервер")
                                .addOption(OptionType.STRING, "nickname", "Кого добавить", true)
                                .setDefaultPermissions(DefaultMemberPermissions.DISABLED),

                        new CommandDataImpl("lock_list", "Получить список людей в lockлисте")
                                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)

                ).queue();
                jda.updateCommands().queue();

                send(Component.text("Commands are turned on successfully!").color(NamedTextColor.GREEN));
            } catch (Exception e) {
                send(Component.text("Commands caught an exception!\n" + e.getMessage()).color(NamedTextColor.RED));
            }

            if (Bukkit.hasWhitelist()) {
                Bukkit.setWhitelist(false);
                send(Component.text("Whitelist was turned on, but for proper work of the 'whitelist feature' it was turned off.").color(NamedTextColor.GOLD));
            }

            Bukkit.getPluginManager().registerEvents(new ServerEvents(), this);
            Objects.requireNonNull(getCommand("wban")).setExecutor(new WbanCommand());
            Objects.requireNonNull(getCommand("wpardon")).setExecutor(new WpardonCommand());
            Objects.requireNonNull(getCommand("wgetban")).setExecutor(new WgetbanCommand());

            send(Component.text("Checking players roles...").color(NamedTextColor.YELLOW));

            checkPlayersRoles();

            send(Component.text("Done giving roles ").color(NamedTextColor.GREEN).append(Component.text("(they might show up later if number of people was more than 5).").color(NamedTextColor.GOLD)));
            send(Component.text("Registering \"checking players roles\" task to run every 2 minutes...").color(NamedTextColor.YELLOW));

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    checkPlayersRoles();
                }
            }, 120000, 120000);

            send(Component.text("Everything is done!").color(NamedTextColor.GREEN));

            //ConfigUtils.sendDM(jda.getUserById(300626304221446145L), thisChannel -> thisChannel.sendMessageEmbeds(new EmbedBuilder()
            //        .addField(new MessageEmbed.Field("dude", "fuck yourself", true))
            //        .setColor(0xFFFFFF)
            //        .setDescription("||<- cum color||")
            //        .build()
            //));
        } catch (Exception e) {
            for (int i = 0; i < 25; i++)
                Bukkit.getLogger().severe("CAUGHT AN ERROR. FOR SAFETY REASONS SERVER IS NOT GOING TO START!!!");
            Bukkit.shutdown();
        }
    }

    @Override
    public void onDisable() {
        Utils.save();
    }

    public static void checkPlayersRoles() {

        Role player_role = jda.getRoleById(Utils.ConfigThings.PLAYER_ROLE.getAsString());
        Role banned_role = jda.getRoleById(Utils.ConfigThings.BANNED_ROLE.getAsString());
        assert player_role != null : "checkPlayersRoles player_role -> NULL";
        assert banned_role != null : "checkPlayersRoles banned_role -> NULL";
        guild.loadMembers().onSuccess(members -> {
            int i = 0;
            for (Member member : members) {
                if (member == null) continue;

                //if (!member.getRoles().contains(player_role)) {
                //    Utils.setPlayerRoles(member);                       // REMOVE LATER WHEN THE SERVER STARTS
                //}
                //if (true) continue;

                Optional<String> nickFromMember = Utils.getNickFromMember(member);
                List<Role> roles = member.getRoles();
                if (!nickFromMember.isPresent()) {
                    if (roles.contains(banned_role) || roles.contains(player_role)) {
                        Utils.setNoneRoles(member);
                        i++;
                    }
                    continue;
                }
                String nickname = nickFromMember.get();
                if (Utils.isBanned(nickname)) {
                    if (!roles.contains(banned_role) || roles.contains(player_role)) {
                        Utils.setBanRoles(member);
                        i++;
                    }
                } else if (Utils.isPlayer(member)) {
                    if (!Utils.getReason(nickname).equals(Utils.NULL_STRING)) {
                        Utils.removeBan(nickname);
                    } else if (roles.contains(banned_role) || !roles.contains(player_role)) {
                        Utils.setPlayerRoles(member);
                        i++;
                    }
                } else {
                    if (roles.contains(banned_role) || roles.contains(player_role)) {
                        Utils.setNoneRoles(member);
                        i++;                                       // Just if something goes wrong
                    }
                }
            }
            if (i > 0) send(Component.text(i + " people with incorrect roles, reassigning...").color(NamedTextColor.GOLD));
        });
    }

    public static void send(Component text) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[").color(NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(plugin.getName()).color(NamedTextColor.YELLOW))
                .append(Component.text("] ").color(NamedTextColor.LIGHT_PURPLE))
                .append(text));
    }

    public static void send(String text) {
        send(Component.text(text).color(NamedTextColor.WHITE));
    }

    public static boolean sendMessageAndReturnFalse(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message).color(NamedTextColor.RED));
        return false;
    }
}
