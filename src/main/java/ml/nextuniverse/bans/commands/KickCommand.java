package ml.nextuniverse.bans.commands;

import ml.nextuniverse.bans.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.*;
import java.util.UUID;

/**
 * Created by TheDiamondPicks on 2/07/2017.
 */
public class KickCommand extends Command {
    public KickCommand() {
        super("kick", "bans.kick");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length > 1) {
            if (strings[0].length() > 16 && strings[0].length() != 36) {
                commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Usage: ").color(ChatColor.RED).append("/kick <username or UUID> <reason>").color(ChatColor.WHITE).create());
            }
            else if (strings[0].length() < 16) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1 ; i < strings.length ; i++) {
                    String s = strings[i];
                    sb.append(s + " ");
                }
                sb.trimToSize();
                String message = sb.toString();
                message.trim();
                kickUsername(strings[0], commandSender, message);
            }
            else {
                StringBuilder sb = new StringBuilder();
                for (int i = 1 ; i < strings.length ; i++) {
                    String s = strings[i];
                    sb.append(s + " ");
                }
                sb.trimToSize();
                String message = sb.toString();
                message.trim();

                kickUUID(strings[0], commandSender, message);
            }
        }
        else {
            commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Usage: ").color(ChatColor.RED).append("/kick <username or UUID> <reason>").color(ChatColor.WHITE).create());
        }
    }
    private static final String KickUser = "INSERT INTO Kicks VALUES(DEFAULT,?,?,?,?)";
    private static final String VerifyUsername = "SELECT UUID FROM UserInfo WHERE CachedUserName=?";

    public void kickUsername(final String name, final CommandSender commandSender, final String reason) {
        try {
            final ProxiedPlayer p = ProxyServer.getInstance().getPlayer(name);
            if (p.isConnected()) {
                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        try (Connection connection = Main.getHikari().getConnection();
                             PreparedStatement select = connection.prepareStatement(VerifyUsername)) {
                            select.setString(1, name);
                            ResultSet result = select.executeQuery();
                            String uuid;
                            if (result.next()) {
                                uuid = result.getString("UUID");
                            } else {
                                commandSender.sendMessage(new ComponentBuilder("That player is not online. Please ensure you spelt the name correctly").color(ChatColor.RED).create());
                                return;
                            }
                            final String innerUUID = uuid;
                            ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    try (Connection connection = Main.getHikari().getConnection();
                                         PreparedStatement insert = connection.prepareStatement(KickUser)) {
                                        insert.setString(1, innerUUID);
                                        insert.setString(2, reason);
                                        insert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                                        if (commandSender instanceof ProxiedPlayer) {
                                            insert.setString(4, ((ProxiedPlayer) commandSender).getUniqueId().toString());
                                        }
                                        else
                                            insert.setString(4, "-CONSOLE-");
                                        insert.execute();

                                        p.disconnect(Main.getMessage(reason, null, Main.ReasonType.KICK_KICK, null, null, null));
                                        for (ProxiedPlayer a : ProxyServer.getInstance().getPlayers()) {
                                            if (a.hasPermission("bans.broadcast"))
                                                a.sendMessage(Main.getMessage(reason, null, Main.ReasonType.KICK_BROADCAST, commandSender.getName(), name, null));
                                        }
                                    }
                                    catch (SQLException e) {
                                        commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (SQLException e) {
                            commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                        }
                    }
                });
            }
            else {
                commandSender.sendMessage(new ComponentBuilder("That player is not online. Please ensure you spelt the UUID correctly").color(ChatColor.RED).create());
            }
        } catch (NullPointerException e) {
            commandSender.sendMessage(new ComponentBuilder("That player is not online. Please ensure you spelt the name correctly").color(ChatColor.RED).create());
        }
    }
    public void kickUUID(final String uuid, final CommandSender commandSender, final String reason) {
        try {
            final ProxiedPlayer p = ProxyServer.getInstance().getPlayer(UUID.fromString(uuid));
            if (p.isConnected()) {
                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        try (Connection connection = Main.getHikari().getConnection();
                             PreparedStatement insert = connection.prepareStatement(KickUser)) {
                            insert.setString(1, uuid);
                            insert.setString(2, reason);
                            insert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                            if (commandSender instanceof ProxiedPlayer) {
                                insert.setString(4, ((ProxiedPlayer) commandSender).getUniqueId().toString());
                            }
                            else
                                insert.setString(4, "-CONSOLE-");
                            insert.execute();

                            p.disconnect(Main.getMessage(reason, null, Main.ReasonType.KICK_KICK, null, null, null));
                            for (ProxiedPlayer a : ProxyServer.getInstance().getPlayers()) {
                                if (a.hasPermission("bans.broadcast"))
                                    a.sendMessage(Main.getMessage(reason, null, Main.ReasonType.KICK_BROADCAST, commandSender.getName(), uuid, null));
                            }
                        }
                        catch (SQLException e) {
                            commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                            e.printStackTrace();
                        }
                    }
                });
            }
            else {
                commandSender.sendMessage(new ComponentBuilder("That player is not online. Please ensure you spelt the UUID correctly").color(ChatColor.RED).create());
            }
        } catch (NullPointerException e) {
            commandSender.sendMessage(new ComponentBuilder("That player is not online. Please ensure you spelt the UUID correctly").color(ChatColor.RED).create());
        }
    }
}
