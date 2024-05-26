package org.levaltru.warviorbot_v2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.interactions.component.TextInputImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static org.levaltru.warviorbot_v2.Warviorbot_v2.jda;

public class BotEvents extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (checkPermitAndSendMessage(event.getMember(), Utils::canBan, event.reply(""))) {

            switch (event.getName()) {
                case "ban_user": {

                    Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();
                    assert member != null : "ban_user -> member is NULL";
                    double duration = Objects.requireNonNull(event.getOption("duration")).getAsDouble();
                    String reason = "неуказанна";
                    if (event.getOption("reason") != null)
                        reason = Objects.requireNonNull(event.getOption("reason")).getAsString();

                    if (Utils.addBan(member, duration, reason, event.getMember())) {
                        String nick = Utils.getNickFromMember(member).orElse("`null`");
                        event.reply(member.getAsMention() + " (`" + nick + "`) был забанен на `" + ServerEvents.remainingTime(nick) + "`.").setEphemeral(true).queue();
                    } else event.reply("Ошибка, посмотрите в личку для подробностей").setEphemeral(true).queue();

                    break;
                }
                case "ban_nickname": {

                    String nickname = Objects.requireNonNull(event.getOption("nickname")).getAsString();
                    double duration = Objects.requireNonNull(event.getOption("duration")).getAsDouble();
                    String reason = "неуказанна";
                    if (event.getOption("reason") != null)
                        reason = Objects.requireNonNull(event.getOption("reason")).getAsString();

                    if (Utils.addBan(nickname, duration, reason, event.getMember())) {
                        String asMention = Utils.getMemberFromNick(nickname).map(IMentionable::getAsMention).orElse("`null`");
                        event.reply(asMention + " (`" + nickname + "`) был забанен на `" + ServerEvents.remainingTime(nickname) + "`.").setEphemeral(true).queue();
                    } else event.reply("Ошибка, посмотрите в личку для подробностей").setEphemeral(true).queue();

                    break;
                }
                case "pardon_user": {

                    Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();
                    assert member != null : "pardon_user -> member is NULL";
                    Optional<String> nickname = Utils.getNickFromMember(member);
                    if (!nickname.isPresent()) {
                        event.reply(member.getAsMention() + " не найден").setEphemeral(true).queue();
                        return;
                    }
                    if (Utils.removeBan(member, event.getMember()))
                        event.reply(member.getAsMention() + " (`" + nickname.get() + "`) был разбанен.").setEphemeral(true).queue();

                    break;
                }
                case "pardon_nickname": {

                    String nickname = Objects.requireNonNull(event.getOption("nickname")).getAsString();
                    String asMention = Utils.getMemberFromNick(nickname).map(IMentionable::getAsMention).orElse("`null`");

                    if (Utils.isBanned(nickname)) {
                        Utils.removeBan(nickname, event.getMember());
                        event.reply(asMention + " (`" + nickname + "`) был разбанен.").setEphemeral(true).queue();
                    } else event.reply(asMention + " (`" + nickname + "`) не забанен (может истек срок бана)").queue();

                    break;
                }
                case "get_ban_user": {
                    Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();
                    assert member != null : "get_ban_user -> member is NULL";
                    Optional<String> optionalNickname = Utils.getNickFromMember(member);
                    if (!optionalNickname.isPresent()) {
                        event.reply(member.getAsMention() + " не найден").setEphemeral(true).queue();
                        return;
                    }
                    String nickname = optionalNickname.get();
                    if (Utils.isBanned(member))
                        event.reply(member.getAsMention() + " (`" + nickname + "`) забанен за `" + Utils.getReason(nickname) + "` истекает через " + Utils.getBanDiscordTimestemp(nickname)).setEphemeral(true).queue();
                    else
                        event.reply(member.getAsMention() + " (`" + nickname + "`) не забанен (может истек срок бана)").setEphemeral(true).queue();

                    break;
                }
                case "get_ban_nickname": {

                    String nickname = Objects.requireNonNull(event.getOption("nickname")).getAsString();
                    String asMention = Utils.getMemberFromNick(nickname).map(IMentionable::getAsMention).orElse("`null`");

                    if (Utils.isBanned(nickname))
                        event.reply(asMention + " (`" + nickname + "`) забанен за `" + Utils.getReason(nickname) + "` истекает через " + Utils.getBanDiscordTimestemp(nickname)).setEphemeral(true).queue();
                    else event.reply(asMention + " (`" + nickname + "`) не забанен").setEphemeral(true).queue();

                    break;
                }
                case "say": {

                    TextChannel textChannel = event.getChannel().asTextChannel();
                    String text = Objects.requireNonNull(event.getOption("text")).getAsString();
                    Optional<OptionMapping> replying = Optional.ofNullable(event.getOption("replying"));
                    Optional<OptionMapping> ping = Optional.ofNullable(event.getOption("ping"));
                    Member sayer = event.getMember();
                    assert sayer != null;

                    if (text.length() > 2000) event.reply("Текст слишком большой").setEphemeral(true).queue();

                    event.deferReply(true).queue();
                    MessageCreateAction messageCreateAction = textChannel.sendMessage(text);
                    try {
                        messageCreateAction.setMessageReference(replying.map(OptionMapping::getAsString).orElse(null)).mentionRepliedUser(ping.map(OptionMapping::getAsBoolean).orElse(false));
                    } catch (IllegalArgumentException e) {
                        event.reply("Id на сообщение решил отказать (проверьте id сообщения)").setEphemeral(true).queue();
                        return;
                    }

                    event.getHook().deleteOriginal().queue();
                    Message message = messageCreateAction.complete();

                    Objects.requireNonNull(jda.getChannelById(TextChannel.class, Utils.ConfigThings.WHITELIST_LOG_CHANNEL.getAsString()))
                            .sendMessageEmbeds(new EmbedBuilder().setColor(0x0088FF)
                                    .setTitle("Saying")
                                    .addField("Кто сказал:", sayer.getAsMention(), true)
                                    .addField("Что сказал:", text, true)
                                    .addField("Где сказал:", message.getJumpUrl(), true)
                                    .build()
                            ).queue();
                    break;
                }
                case "say_modal": {

                    TextChannel textChannel = event.getChannel().asTextChannel();
                    String json = Objects.requireNonNull(event.getOption("json")).getAsString();
                    Optional<OptionMapping> replying = Optional.ofNullable(event.getOption("replying"));
                    Optional<OptionMapping> ping = Optional.ofNullable(event.getOption("ping"));
                    Member sayer = event.getMember();
                    assert sayer != null;

                    try {
                        DataObject data = DataObject.fromJson(json);
                        MessageCreateBuilder builder = new MessageCreateBuilder();

                        builder.setContent(data.getString("content", ""));

                        for (int i = 0; i < data.getArray("embeds").length(); i++)
                            builder.addEmbeds(EmbedBuilder.fromData(data.getArray("embeds").getObject(i)).build());

                        MessageCreateAction messageCreateAction = textChannel.sendMessage(builder.build());
                        event.deferReply(true).queue();
                        try {
                            messageCreateAction.setMessageReference(replying.map(OptionMapping::getAsString).orElse(null)).mentionRepliedUser(ping.map(OptionMapping::getAsBoolean).orElse(false));
                        } catch (IllegalArgumentException e) {
                            event.reply("Id на сообщение решил отказать (проверьте id сообщения)").setEphemeral(true).queue();
                            return;
                        }

                        event.getHook().deleteOriginal().queue();
                        Message message = messageCreateAction.complete();

                        Objects.requireNonNull(jda.getChannelById(TextChannel.class, Utils.ConfigThings.WHITELIST_LOG_CHANNEL.getAsString()))
                                .sendMessageEmbeds(new EmbedBuilder().setColor(0x0088FF)
                                        .setTitle("Saying (json)")
                                        .addField("Кто сказал:", sayer.getAsMention(), true)
                                        .addField("Где сказал:", message.getJumpUrl(), true)
                                        .build()
                                ).queue();
                    } catch (ParsingException e) {
                        event.reply("Json какой то не такой (проверьте json)").setEphemeral(true).queue();
                        return;
                    }
                    break;
                }
                case "get_connections": {
                    HashMap<String, String> hashMap = Utils.getConnectionHashMap();
                    if (hashMap.isEmpty()) {
                        event.reply("Нету никаких людей в датабазе").setEphemeral(true).queue();
                        break;
                    }
                    ArrayList<EmbedBuilder> embedBuilder = new ArrayList<>();

                    hashMap.forEach((id, nick) -> {
                        int i = -1;
                        do {
                            i++;
                            if (embedBuilder.size() <= i) embedBuilder.add(new EmbedBuilder());
                            if (embedBuilder.get(i).getFields().size() < 25) {
                                embedBuilder.get(i).addField(new MessageEmbed.Field(nick, "<@" + id + ">", true));
                                break;
                            }
                        } while (embedBuilder.get(i).getFields().size() >= 25);
                    });

                    event.replyEmbeds(embedBuilder.get(0).build()).setEphemeral(true).queue();
                    event.getHook().setEphemeral(true);
                    for (int j = 1; j < embedBuilder.size(); j++)
                        event.getHook().sendMessageEmbeds(embedBuilder.get(j).build()).queue();
                    break;
                }
                case "get_bans": {
                    Collection<String> list = Utils.getBannedNicks();
                    if (list.isEmpty()) {
                        event.reply("Нету никаких забаненный людей в датабазе").setEphemeral(true).queue();
                        break;
                    }
                    ArrayList<EmbedBuilder> embedBuilder = new ArrayList<>();

                    list.forEach(nick -> {
                        int i = -1;
                        do {
                            i++;
                            if (embedBuilder.size() <= i) embedBuilder.add(new EmbedBuilder());
                            if (embedBuilder.get(i).getFields().size() < 25) {
                                embedBuilder.get(i).addField(new MessageEmbed.Field(nick, Utils.getMemberFromNick(nick).map(IMentionable::getAsMention).orElse("`null`")
                                        + " за `" + Utils.getReason(nick)
                                        + "` истекает через " + Utils.getBanDiscordTimestemp(nick), true));
                                break;
                            }
                        } while (embedBuilder.get(i).getFields().size() >= 25);
                    });

                    event.replyEmbeds(embedBuilder.get(0).build()).setEphemeral(true).queue();
                    event.getHook().setEphemeral(true);
                    for (int j = 1; j < embedBuilder.size(); j++)
                        event.getHook().sendMessageEmbeds(embedBuilder.get(j).build()).queue();
                    break;

                }
                case "lock": {

                    Optional<Boolean> bool = Optional.ofNullable(event.getOption("boolean")).map(OptionMapping::getAsBoolean);

                    if (bool.isPresent()) {
                        Utils.ConfigThings.IS_LOCKED.value = bool.get();
                        event.reply("Поменял значение на " + bool.get()).setEphemeral(true).queue();
                    } else {
                        event.reply("Нынешние значение " + Utils.ConfigThings.IS_LOCKED.getAsBoolean()).setEphemeral(true).queue();
                    }

                    break;
                }
                case "lock_add": {

                    String nickname = event.getOption("nickname").getAsString();
                    nickname = nickname.toLowerCase();

                    Utils.addToLockList(nickname);
                    event.reply("Добавил `" + nickname + "` в lockлист").setEphemeral(true).queue();

                    break;
                }
                case "lock_remove": {

                    String nickname = event.getOption("nickname").getAsString();
                    nickname = nickname.toLowerCase();

                    Utils.removeFromLockList(nickname);
                    event.reply("Убрал `" + nickname + "` из lockлиста").setEphemeral(true).queue();

                    break;
                }
                case "lock_list": {
                    List<String> list = Utils.ConfigThings.LOCKED_LIST.getAsArrayString();
                    if (list.isEmpty()) {
                        event.reply("Нету никаких lockеров людей в датабазе").setEphemeral(true).queue();
                        break;
                    }
                    ArrayList<EmbedBuilder> embedBuilder = new ArrayList<>();

                    list.forEach(nick -> {
                        int i = -1;
                        do {
                            i++;
                            if (embedBuilder.size() <= i) embedBuilder.add(new EmbedBuilder());
                            if (embedBuilder.get(i).getFields().size() < 25) {
                                embedBuilder.get(i).addField(new MessageEmbed.Field(nick, "`" + nick + "`", true));
                                break;
                            }
                        } while (embedBuilder.get(i).getFields().size() >= 25);
                    });

                    event.replyEmbeds(embedBuilder.get(0).build()).setEphemeral(true).queue();
                    event.getHook().setEphemeral(true);
                    for (int j = 1; j < embedBuilder.size(); j++)
                        event.getHook().sendMessageEmbeds(embedBuilder.get(j).build()).queue();
                    break;
                }

            /*if (event.getName().equalsIgnoreCase("ban_user")) {

                Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();
                assert member != null : "ban_user -> member is NULL";
                double duration = Objects.requireNonNull(event.getOption("duration")).getAsDouble();
                String reason = "неуказанна";
                if (event.getOption("reason") != null) reason = Objects.requireNonNull(event.getOption("reason")).getAsString();

                if (Utils.addBan(member, duration, reason, event.getMember())) {
                    String nick = Utils.getNickFromMember(member).orElse("`null`");
                    event.reply(member.getAsMention() + " (" + nick + ") был забанен на `" + ServerEvents.remainingTime(nick) + "`.").setEphemeral(true).queue();
                } else event.reply("Ошибка, посмотрите в личку для подробностей").setEphemeral(true).queue();

            } else if (event.getName().equalsIgnoreCase("ban_nickname")) {

                String nickname = Objects.requireNonNull(event.getOption("nickname")).getAsString();
                double duration = Objects.requireNonNull(event.getOption("duration")).getAsDouble();
                String reason = "неуказанна";
                if (event.getOption("reason") != null) reason = Objects.requireNonNull(event.getOption("reason")).getAsString();

                if (Utils.addBan(nickname, duration, reason, event.getMember())) {
                    String asMention = Utils.getMemberFromNick(nickname).map(IMentionable::getAsMention).orElse("`null`");
                    event.reply(asMention + " (" + nickname + ") был забанен на `" + ServerEvents.remainingTime(nickname) + "`.").setEphemeral(true).queue();
                } else event.reply("Ошибка, посмотрите в личку для подробностей").setEphemeral(true).queue();


            } else if (event.getName().equalsIgnoreCase("pardon_user")) {

                Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();
                assert member != null : "pardon_user -> member is NULL";
                Optional<String> nickname = Utils.getNickFromMember(member);
                if (!nickname.isPresent()) {
                    event.reply(member.getAsMention() + " не найден").setEphemeral(true).queue();
                    return;
                }
                if (Utils.removeBan(member, event.getMember()))
                    event.reply(member.getAsMention() + " (" + nickname.get() + ") был разбанен.").setEphemeral(true).queue();

            } else if (event.getName().equalsIgnoreCase("pardon_nickname")) {

                String nickname = Objects.requireNonNull(event.getOption("nickname")).getAsString();
                String asMention = Utils.getMemberFromNick(nickname).map(IMentionable::getAsMention).orElse("`null`");

                if (Utils.isBanned(nickname)) {
                    Utils.removeBan(nickname, event.getMember());
                    event.reply(asMention + " (" + nickname + ") был разбанен.").setEphemeral(true).queue();
                } else event.reply(asMention + " ( " + nickname + ") не забанен (может истек срок бана)").queue();

            } else if (event.getName().equalsIgnoreCase("get_ban_user")) {

                Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();
                assert member != null : "get_ban_user -> member is NULL";
                Optional<String> optionalNickname = Utils.getNickFromMember(member);
                if (!optionalNickname.isPresent()) {
                    event.reply(member.getAsMention() + " не найден").setEphemeral(true).queue();
                    return;
                }
                String nickname = optionalNickname.get();
                if (Utils.isBanned(member))
                    event.reply(member.getAsMention() + " (" + nickname + ") забанен за `" + Utils.getReason(nickname) + "` на `" + ServerEvents.remainingTime(nickname) + "`").setEphemeral(true).queue();
                else event.reply(member.getAsMention() + " ( " + nickname + ") не забанен (может истек срок бана)").setEphemeral(true).queue();

            } else if (event.getName().equalsIgnoreCase("get_ban_nickname")) {

                String nickname = Objects.requireNonNull(event.getOption("nickname")).getAsString();
                String asMention = Utils.getMemberFromNick(nickname).map(IMentionable::getAsMention).orElse("`null`");

                if (Utils.isBanned(nickname))
                    event.reply(asMention + " (" + nickname + ") забанен за `" + Utils.getReason(nickname) + "` на `" + ServerEvents.remainingTime(nickname) + "`").setEphemeral(true).queue();
                else event.reply(asMention + "( " + nickname + ") не забанен").setEphemeral(true).queue();
            }


            else if (event.getName().equalsIgnoreCase("say")) {

                TextChannel textChannel = event.getChannel().asTextChannel();
                String text = Objects.requireNonNull(event.getOption("text")).getAsString();
                Member sayer = event.getMember();
                assert sayer != null;

                if (text.length() >= 2000) event.reply("Текст слишком большой").setEphemeral(true).queue();
                textChannel.sendMessage(text).queue();

                Objects.requireNonNull(jda.getChannelById(TextChannel.class, Utils.ConfigThings.WHITELIST_LOG_CHANNEL.value))
                        .sendMessageEmbeds(new EmbedBuilder().setColor(0x0088FF)
                                .setTitle("Saying")
                                .addField("Кто сказал:", sayer.getAsMention(), true)
                                .addField("Что сказал:", text, true)
                                .build()
                        ).queue();

            }*/
            }
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (event.getName().equals("Добавить в вайтлист")) {
            if (checkPermitAndSendMessage(event.getMember(), Utils::canAddWhitelist, event.reply(""))) {
                if (!Utils.whitelistedID(event.getTargetMember().getId()))
                    event.replyModal(Modal.create("wlist_" + event.getTarget().getId(), "Добавить в вайтлист")
                            .addActionRow(new TextInputImpl("wlist_input", TextInputStyle.SHORT, "Ник", 3, 32, true, "Ник", "Ник")).build()).queue();
                else
                    event.replyModal(Modal.create("wlistchange_" + event.getTarget().getId(), "Добавить в вайтлист")
                            .addActionRow(new TextInputImpl("wlistchange_input", TextInputStyle.SHORT, "Ник", 3, 32, true, "Заменить на", "Заменить на")).build()).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        Member odmin = event.getMember();
        assert odmin != null : "OnModalInteraction -> odmin is NULL";
        if (checkPermitAndSendMessage(odmin, Utils::canAddWhitelist, event.reply(""))) {
            if (event.getModalId().contains("wlist_")) {
                String id = event.getModalId().replace("wlist_", "");
                String nickname = Objects.requireNonNull(event.getValue("wlist_input")).getAsString().replace(" ", "").replace("\n", "").replace("\r", "");

                Member member = Objects.requireNonNull(event.getGuild()).getMemberById(id);
                if (member == null) {
                    Utils.sendDM(odmin, "Аккаунт не найден ¯\\_(ツ)_/¯");
                    return;
                }

                if (Utils.addConnection(nickname, member, odmin))
                    event.reply(member.getAsMention() + " (`" + nickname + "`) был добавлен в вайтлист сервера").setEphemeral(true).queue();
                else event.reply("Ошибка, посмотрите в личку для подробностей").setEphemeral(true).queue();

            } else if (event.getModalId().contains("wlistchange_")) {
                String id = event.getModalId().replace("wlistchange_", "");
                String nickname = Objects.requireNonNull(event.getValue("wlistchange_input")).getAsString().replace(" ", "").replace("\n", "").replace("\r", "");

                Member member = Objects.requireNonNull(event.getGuild()).getMemberById(id);
                if (member == null) {
                    Utils.sendDM(odmin, "Аккаунт не найден ¯\\_(ツ)_/¯");
                    return;
                }

                Utils.addOrReplaceConnection(nickname, member, odmin);
                event.reply(member.getAsMention() + "теперь имеет ник `" + nickname + "` в вайтлисте сервера").setEphemeral(true).queue();
            }
        }
    }

    private static boolean checkPermitAndSendMessage(Member member, Predicate<Member> predicate, ReplyCallbackAction reply) {
        if (member == null) return false;
        if (!predicate.test(member)) {
            reply.setContent("У вас нету прав (skill issue).").setEphemeral(true).queue();
            return false;
        } else return true;
    }
}
