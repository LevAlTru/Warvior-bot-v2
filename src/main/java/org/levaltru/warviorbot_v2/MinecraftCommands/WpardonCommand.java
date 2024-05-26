package org.levaltru.warviorbot_v2.MinecraftCommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.levaltru.warviorbot_v2.Utils;
import org.levaltru.warviorbot_v2.Warviorbot_v2;

public class WpardonCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.isPermissionSet("warvior_bot.wban") || sender.isOp()) {
            if (args.length < 1) Warviorbot_v2.sendMessageAndReturnFalse(sender, "Не достаточно аргументов");
            else {
                String nickname = args[0];
                long until = Utils.getUntil(nickname);
                if (until > System.currentTimeMillis() || until < 0) {
                    Utils.removeBan(nickname, Utils.getMemberFromNick(sender.getName()).orElse(null));
                    sender.sendMessage(NamedTextColor.GREEN + "Разбанил " + nickname);
                } else sender.sendMessage(NamedTextColor.RED + nickname + " не забанен (может истек срок бана)");
            }
            return true;
        }
        return false;
    }
}
