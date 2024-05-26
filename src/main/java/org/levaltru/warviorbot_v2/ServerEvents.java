package org.levaltru.warviorbot_v2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ServerEvents implements Listener {

    @EventHandler
    private void onJoin(PlayerLoginEvent event) {

        String nickname = event.getPlayer().getName().toLowerCase();

        if (Utils.isBanned(nickname))
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Component.text("Вы были забанены\nзаканчивается через: " + remainingTime(nickname) + "\n\nПричина бана:\n" + Utils.getReason(nickname), TextColor.color(NamedTextColor.RED)));

        if (Utils.ConfigThings.IS_LOCKED.getAsBoolean())
            if (!Utils.ConfigThings.LOCKED_LIST.getAsArrayString().contains(nickname))
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Component.text("Сервер закрыт для публичного посещения\nUwU", TextColor.color(NamedTextColor.RED)));

        if (!Utils.whitelisted(nickname))
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Component.text("Вы не в вайтлисте", TextColor.color(NamedTextColor.RED)));

        if (!Utils.getMemberFromNick(nickname).isPresent())
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Component.text("Вы в вайтлисте но не на Дискорд сервере\n\n> > > https://discord.gg/kkmHGXkFNV < < <", TextColor.color(NamedTextColor.RED)));

    }

    public static String remainingTime(String nickname) {
        long endTime = Utils.getUntil(nickname);
        if (endTime <= 0) return "неуказанно";
        long startTime = System.currentTimeMillis();
        long remainingTime = endTime - startTime;

        long minutes = remainingTime / 60000 % 60;
        long hours = remainingTime / (60000 * 60) % 24;
        long days = remainingTime / (60000 * 60 * 24);
        String sminutes = " " + minutes + "м";
        String shours = " " + hours + "ч";
        String sdays = days + "д";

        if (days == 0) {
            sdays = "";
            shours = hours + "ч";
            if (hours == 0) {
                shours = "";
                sminutes = minutes + "м";
                if (minutes == 0) {
                    return "<1м";
                }
            }
        }

        return sdays + shours + sminutes;

    }
}
