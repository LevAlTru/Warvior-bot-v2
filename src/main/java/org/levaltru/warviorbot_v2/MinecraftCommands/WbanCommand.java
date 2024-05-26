package org.levaltru.warviorbot_v2.MinecraftCommands;

import net.dv8tion.jda.api.entities.Member;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.levaltru.warviorbot_v2.Utils;
import org.levaltru.warviorbot_v2.Warviorbot_v2;

public class WbanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.isPermissionSet("warvior_bot.wban") || sender.isOp()) {
            int length = args.length;
            if (length < 1) return Warviorbot_v2.sendMessageAndReturnFalse(sender, "Не достаточно аргументов");
            Member odmin = Utils.getMemberFromNick(sender.getName()).orElse(null);
            String nickname = args[0];
            if (length == 1) Utils.addBan(nickname, -1, "", odmin);
            else {
                double i;

                try { i = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) { return Warviorbot_v2.sendMessageAndReturnFalse(sender, "Нужно ввести на сколько часов нужно забанить а не " + args[1]); }

                if (length > 2) Utils.addBan(nickname, i, args[2], odmin);
                else Utils.addBan(nickname, i, "", odmin);

                sender.sendMessage(NamedTextColor.YELLOW + "Забанил " + nickname + " на " + i + "ч");
                return true;
            }
            sender.sendMessage(NamedTextColor.YELLOW + "Забанил " + nickname);
            return true;
        }
        return false;
    }
}
