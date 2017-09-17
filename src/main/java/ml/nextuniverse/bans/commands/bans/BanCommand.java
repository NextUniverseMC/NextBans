package ml.nextuniverse.bans.commands.bans;


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
 * Created by TheDiamondPicks on 28/09/2016
 */
public class BanCommand extends Command {
    public BanCommand() {
        super("ban", "bans.ban");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length > 1) {
            if (strings[0].length() > 16 && strings[0].length() != 36) {
                commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Usage: ").color(ChatColor.RED).append("/ban <username or UUID> <reason>").color(ChatColor.WHITE).create());
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
                banUsername(strings[0], commandSender, message);
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

                banUUID(strings[0], commandSender, message);
            }
        }
        else {
            commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Usage: ").color(ChatColor.RED).append("/ban <username or UUID> <reason>").color(ChatColor.WHITE).create());
        }
    }
    private static final String VerifyUsername = "SELECT UUID FROM UserInfo WHERE CachedUserName=?";
    private static final String VerifyUUID = "SELECT * FROM UserInfo WHERE UUID=?";
    private static final String CheckIfBanned = "SELECT * FROM Bans WHERE UserUUID=? AND Active=?";
    private static final String BanUser = "INSERT INTO Bans VALUES(DEFAULT,?,?,?,?,?,?,?,?)";


    public void banUsername(final String name, final CommandSender commandSender, final String reason) {
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
                    }
                    else {
                        commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.NO_USERNAME)).color(ChatColor.RED).create());
                        return;
                    }
                    final String innerUUID = uuid;


                        try (Connection connection2 = Main.getHikari().getConnection();
                             PreparedStatement select2 = connection2.prepareStatement(CheckIfBanned)) {
                            select2.setString(1, uuid);
                            select2.setBoolean(2, true);
                            ResultSet result2 = select2.executeQuery();
                            if (result2.first()) {
                                commandSender.sendMessage(new ComponentBuilder("That user is already banned! Use ").color(ChatColor.RED).append("/unban <username>").color(ChatColor.WHITE).append(" to unban them first.").color(ChatColor.RED).create());
                            }
                            else {
                                final Timestamp d = new Timestamp(System.currentTimeMillis());
                                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        try (Connection connection = Main.getHikari().getConnection();
                                             PreparedStatement insert = connection.prepareStatement(BanUser)) {
                                            insert.setString(1, innerUUID);
                                            insert.setString(2, reason);
                                            insert.setTimestamp(3, d);
                                            if (commandSender instanceof ProxiedPlayer) {
                                                insert.setString(4, ((ProxiedPlayer) commandSender).getUniqueId().toString());
                                            }
                                            else
                                                insert.setString(4, "-CONSOLE-");
                                            insert.setDate(5, new Date(0L));
                                            insert.setString(6, "-PERMBAN-");
                                            insert.setDate(7, new Date(0L));
                                            insert.setBoolean(8, true);
                                            insert.execute();
                                            try {
                                                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(name);
                                                if (p.isConnected()) {
                                                    p.disconnect(Main.getMessage(reason, null, Main.ReasonType.PERMBAN_KICK, null, null, null));
                                                } else {
                                                    commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                                }
                                            }
                                            catch (NullPointerException e) {
                                                commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                            }
                                            for (ProxiedPlayer a : ProxyServer.getInstance().getPlayers()) {
                                                if (a.hasPermission("bans.broadcast")) {
                                                    a.sendMessage(Main.getMessage(reason, null, Main.ReasonType.PERMBAN_BROADCAST, commandSender.getName(), name, null));
                                                }
                                            }
                                        }
                                        catch (SQLException e) {
                                            e.printStackTrace();
                                            commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                                        }
                                    }
                                });
                            }
                            result2.close();
                        }

                    result.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void banUUID(final String uuid, final CommandSender commandSender, final String reason) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                try (Connection connection = Main.getHikari().getConnection();
                     PreparedStatement select = connection.prepareStatement(VerifyUUID)) {
                    select.setString(1, uuid);
                    ResultSet result = select.executeQuery();
                    if (result.next()) {
                        try (Connection connection2 = Main.getHikari().getConnection();
                             PreparedStatement select2 = connection2.prepareStatement(CheckIfBanned)) {
                            select2.setString(1, uuid);
                            select2.setBoolean(2, true);
                            ResultSet result2 = select2.executeQuery();
                            if (result2.first()) {
                                commandSender.sendMessage(new ComponentBuilder("That user is already banned! Use ").color(ChatColor.RED).append("/unban <username>").color(ChatColor.WHITE).append(" to unban them first.").color(ChatColor.RED).create());
                            }
                            else {
                                final Timestamp d = new Timestamp(System.currentTimeMillis());
                                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        try (Connection connection = Main.getHikari().getConnection();
                                             PreparedStatement insert = connection.prepareStatement(BanUser)) {
                                            insert.setString(1, uuid);
                                            insert.setString(2, reason);
                                            insert.setTimestamp(3, d);
                                            if (commandSender instanceof ProxiedPlayer) {
                                                insert.setString(4, ((ProxiedPlayer) commandSender).getUniqueId().toString());
                                            }
                                            else
                                                insert.setString(4, "-CONSOLE-");
                                            insert.setDate(5, new Date(0L));
                                            insert.setString(6, "-PERMBAN-");
                                            insert.setDate(7, new Date(0L));
                                            insert.setBoolean(8, true);
                                            insert.execute();
                                            try {
                                                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(UUID.fromString(uuid));
                                                if (p.isConnected()) {
                                                    p.disconnect(Main.getMessage(reason, null, Main.ReasonType.PERMBAN_KICK, null, null, null));
                                                } else {
                                                    commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                                }
                                            }
                                            catch (NullPointerException e) {
                                                commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                            }
                                            for (ProxiedPlayer a : ProxyServer.getInstance().getPlayers()) {
                                                if (a.hasPermission("bans.broadcast")) {
                                                    a.sendMessage(Main.getMessage(reason, null, Main.ReasonType.PERMBAN_BROADCAST, commandSender.getName(), uuid, null));
                                                }
                                            }
                                        }
                                        catch (SQLException e) {
                                            e.printStackTrace();
                                            commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());

                                        }
                                    }
                                });
                            }
                            result2.close();
                        }
                    }
                    else {
                        commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.NO_UUID)).color(ChatColor.RED).create());
                    }




                    result.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                }
            }
        });

    }
}

