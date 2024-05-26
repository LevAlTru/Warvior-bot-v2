package org.levaltru.warviorbot_v2.MinecraftCommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.levaltru.warviorbot_v2.Utils;
import org.levaltru.warviorbot_v2.ServerEvents;
import org.levaltru.warviorbot_v2.Warviorbot_v2;

public class WgetbanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.isPermissionSet("warvior_bot.wban") || sender.isOp()) {
            if (args.length < 1) return Warviorbot_v2.sendMessageAndReturnFalse(sender, "Не достаточно аргументов");
            else {
                String nickname = args[0];
                long until = Utils.getUntil(nickname);
                if (until > System.currentTimeMillis() || until < 0)
                    sender.sendMessage(NamedTextColor.YELLOW + nickname + " забанен за " + NamedTextColor.WHITE + Utils.getReason(nickname) + NamedTextColor.YELLOW + " на " + NamedTextColor.WHITE + ServerEvents.remainingTime(nickname));
                else sender.sendMessage(NamedTextColor.RED + nickname + " не забанен (может истек срок бана)");
                return true;
            }
        }
        return false;
    }
}
