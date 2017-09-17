package ml.nextuniverse.bans.commands.bans;


import ml.nextuniverse.bans.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by TheDiamondPicks on 28/09/2016
 */
public class TempbanCommand extends Command {
    public TempbanCommand() {
        super("tempban", "bans.tempban");
    }

    String rawDate;

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length >= 3) {
            HashMap<String, Integer> timeStorage = new HashMap<>();
            Timestamp t = new Timestamp(System.currentTimeMillis());
            String timeTemp = "";
            for (int i = 0 ; i < strings[1].length() ; i++) {
                Character c = strings[1].charAt(i);
                if (c.equals('d')) {
                    if (timeTemp.isEmpty()) {
                        commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                        return;
                    }
                    else {
                        try {
                            timeStorage.put("days", Integer.parseInt(timeTemp));
                            timeTemp = "";
                        }
                        catch (NumberFormatException e ) {
                            commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                            return;
                        }
                    }
                }
                else if (c.equals('h')) {
                    if (timeTemp.isEmpty()) {
                        commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                        return;
                    }
                    else {
                        try {
                            timeStorage.put("hours", Integer.parseInt(timeTemp));
                            timeTemp = "";
                        }
                        catch (NumberFormatException e ) {
                            commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                            return;
                        }
                    }
                }
                else if (c.equals('m')) {
                    if (timeTemp.isEmpty()) {
                        commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                        return;
                    }
                    else {
                        try {
                            timeStorage.put("minutes", Integer.parseInt(timeTemp));
                            timeTemp = "";
                        }
                        catch (NumberFormatException e ) {
                            commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                            
                            return;
                        }
                    }
                }
                else if (c.equals('s')) {
                    if (timeTemp.isEmpty()) {
                        commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                        return;
                    }
                    else {
                        try {
                            timeStorage.put("seconds", Integer.parseInt(timeTemp));
                            timeTemp = "";
                        }
                        catch (NumberFormatException e ) {
                            commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                            return;
                        }
                    }
                }
                else {
                    timeTemp = timeTemp + c;
                }
            }
            if (timeStorage.isEmpty()) {
                commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.INVALID_TIMESTAMP, null, null, null));
                return;
            }
            else {
                rawDate = strings[1];
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(t.getTime());
                if (timeStorage.containsKey("days"))
                    c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + timeStorage.get("days"));
                if (timeStorage.containsKey("hours"))
                    c.set(Calendar.HOUR, c.get(Calendar.HOUR) + timeStorage.get("hours"));
                if (timeStorage.containsKey("minutes"))
                    c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + timeStorage.get("minutes"));
                if (timeStorage.containsKey("seconds"))
                    c.set(Calendar.SECOND, c.get(Calendar.SECOND) + timeStorage.get("seconds"));
                t.setTime(c.getTimeInMillis());
            }
            if (strings[0].length() > 16 && strings[0].length() != 36) {
                commandSender.sendMessage(new ComponentBuilder("Invalid usage! ").color(ChatColor.RED).append("/tempban <username or UUID> <time> <reason>").color(ChatColor.WHITE).create());
            }
            else if (strings[0].length() < 16) {
                StringBuilder sb = new StringBuilder();
                for (int i = 2 ; i < strings.length ; i++) {
                    String s = strings[i];
                    sb.append(s + " ");
                }
                sb.trimToSize();
                String message = sb.toString();
                message.trim();
                banUsername(strings[0], commandSender, message, t);
            }
            else {
                StringBuilder sb = new StringBuilder();
                for (int i = 2 ; i < strings.length ; i++) {
                    String s = strings[i];
                    sb.append(s + " ");
                }
                sb.trimToSize();
                String message = sb.toString();
                message.trim();
                System.out.println(strings[0] + message);
                banUUID(strings[0], commandSender, message, t);
            }
        }
        else {
            commandSender.sendMessage(new ComponentBuilder("Invalid usage! ").color(ChatColor.RED).append("/tempban <username or UUID> <time> <reason>").color(ChatColor.WHITE).create());
        }
    }
    private static final String VerifyUsername = "SELECT UUID FROM UserInfo WHERE CachedUserName=?";
    private static final String CheckIfBanned = "SELECT * FROM Bans WHERE UserUUID=? AND Active=?";
    private static final String BanUser = "INSERT INTO Bans VALUES(DEFAULT,?,?,?,?,?,?,?,?)";


    public void banUsername(final String name, final CommandSender commandSender, final String reason, final Timestamp time) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                try (Connection connection = Main.getHikari().getConnection();
                     PreparedStatement select = connection.prepareStatement(VerifyUsername)) {
                    select.setString(1, name);
                    ResultSet result = select.executeQuery();
                    int i = 0;
                    boolean itsOk = true;
                    String uuid = "";
                    if (result.next()) {
                        uuid = result.getString("UUID");
                    }
                    final String innerUUID = uuid;


                        try (Connection connection2 = Main.getHikari().getConnection();
                             PreparedStatement select2 = connection2.prepareStatement(CheckIfBanned)) {
                            select2.setString(1, uuid);
                            select2.setBoolean(2, true);
                            ResultSet result2 = select2.executeQuery();
                            if (result2.first()) {
                                commandSender.sendMessage(new ComponentBuilder("That player is already banned! Use ").color(ChatColor.RED).append("/unban <username>").color(ChatColor.WHITE).append(" to unban them first.").color(ChatColor.RED).create());
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
                                            insert.setTimestamp(5, time);
                                            insert.setString(6, "-NONE-");
                                            insert.setDate(7, new Date(0L));
                                            insert.setBoolean(8, true);
                                            insert.execute();
                                            try {
                                                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(name);
                                                if (p.isConnected()) {
                                                    p.disconnect(Main.getMessage(reason, time, Main.ReasonType.TEMPBAN_KICK, null, null, null));
                                                } else {
                                                    commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                                }
                                            }
                                            catch (NullPointerException e) {
                                                commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                            }
                                            for (ProxiedPlayer a : ProxyServer.getInstance().getPlayers()) {
                                                if (a.hasPermission("bans.broadcast")) {
                                                    a.sendMessage(Main.getMessage(reason, time, Main.ReasonType.TEMPBAN_BROADCAST, commandSender.getName(), name, rawDate));
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

    public void banUUID(final String uuid, final CommandSender commandSender, final String reason, final Timestamp time) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
            @Override
            public void run() {

                try (Connection connection = Main.getHikari().getConnection();
                     PreparedStatement select = connection.prepareStatement(CheckIfBanned)) {
                    select.setString(1, uuid);
                    select.setBoolean(2, true);
                    ResultSet result = select.executeQuery();
                    if (result.first()) {
                        commandSender.sendMessage(new ComponentBuilder("That player is already banned! Use ").color(ChatColor.RED).append("/unban <username>").color(ChatColor.WHITE).append(" to unban them first.").color(ChatColor.RED).create());
                    } else {
                        System.out.println("here lol");
                        final Timestamp d = new Timestamp(System.currentTimeMillis());
                        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                try (Connection connection = Main.getHikari().getConnection();
                                     PreparedStatement insert = connection.prepareStatement(BanUser)) {
                                    insert.setString(1, uuid);
                                    insert.setString(2, reason);
                                    insert.setTimestamp(3, d);
                                    if (commandSender instanceof ProxiedPlayer)
                                        insert.setString(4, ((ProxiedPlayer) commandSender).getUniqueId().toString());
                                    else
                                        insert.setString(4, "-CONSOLE-");
                                    insert.setTimestamp(5, time);
                                    insert.setString(6, "-NONE-");
                                    insert.setDate(7, new Date(0L));
                                    insert.setBoolean(8, true);
                                    insert.execute();

                                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(UUID.fromString(uuid));
                                    try {
                                        if (p.isConnected()) {
                                            p.disconnect(Main.getMessage(reason, time, Main.ReasonType.TEMPBAN_KICK, null, null, null));
                                        } else {
                                            commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                        }
                                    }
                                    catch (NullPointerException e) {
                                        commandSender.sendMessage(Main.getMessage(null, null, Main.ReasonType.OFFLINE_MESSAGE, null, null, null));
                                    }
                                    for (ProxiedPlayer a : ProxyServer.getInstance().getPlayers()) {
                                        if (a.hasPermission("bans.broadcast")) {
                                            a.sendMessage(Main.getMessage(reason, time, Main.ReasonType.TEMPBAN_BROADCAST, commandSender.getName(), uuid, rawDate));
                                        }
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());

                                }
                            }
                        });
                    }
                    result.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());

                }
            }
        });
    }
}

