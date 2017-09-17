package ml.nextuniverse.bans.commands.warns;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by TheDiamondPicks on 11/07/2017.
 */
public class WarnCommand extends Command {
    public WarnCommand() {
        super("warn", "bans.warn");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length > 1) {
            if (strings[1].equals("profanity") || strings[1].equals("ip") || strings[1].equals("harassment") || strings[1].equals("advertising") || strings[1].equals("reportabuse") || strings[1].equals("spamming")) {
                if (strings[0].length() > 16 && strings[0].length() != 36) {
                    commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Usage: ").color(ChatColor.RED).append("/warn <username or UUID> <category> <reason>").color(ChatColor.WHITE).create());
                } else if (strings[0].length() < 16) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 2; i < strings.length; i++) {
                        String s = strings[i];
                        sb.append(s + " ");
                    }
                    sb.trimToSize();
                    String message = sb.toString();
                    message.trim();

                    warnUsername(strings[0], commandSender, strings[1], message);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < strings.length; i++) {
                        String s = strings[i];
                        sb.append(s + " ");
                    }
                    sb.trimToSize();
                    String message = sb.toString();
                    message.trim();

                    warnUUID(strings[0], commandSender, strings[1], message);
                }
            }
        }
    }
    public void warnUsername(String name, CommandSender commandSender, String category, String message) {

    }
    public void warnUUID(String uuid, CommandSender commandSender, String category, String message) {

    }
}
