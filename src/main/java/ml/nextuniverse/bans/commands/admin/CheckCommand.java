package ml.nextuniverse.bans.commands.admin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by TheDiamondPicks on 30/07/2017.
 */
public class CheckCommand extends Command {
    public CheckCommand() {
        super("check", "bans.check");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length == 1) {
            if (strings[0].length() <= 16 && strings[0].length() >= 3) {

            }
            else if (strings[0].length() == 36) {

            }
            else
                commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Use ") .color(ChatColor.RED).append("/check <username / uuid>").color(ChatColor.WHITE).create());
        }
        else {
            commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Use ") .color(ChatColor.RED).append("/check <username / uuid>").color(ChatColor.WHITE).create());
        }
    }
}
