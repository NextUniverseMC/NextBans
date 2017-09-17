package ml.nextuniverse.bans.commands.bans;


import ml.nextuniverse.bans.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.*;

/**
 * Created by TheDiamondPicks on 28/09/2016
 */
public class UnbanCommand extends Command {
    public UnbanCommand() {
        super("unban", "bans.unban");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length != 1) {
            commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Usage: ").color(ChatColor.RED).append("/unban <username or UUID>").color(ChatColor.WHITE).create());
        }
        else {
            if (strings[0].length() > 16 && strings[0].length() != 36) {
                commandSender.sendMessage(new ComponentBuilder("Invalid arguments! Usage: ").color(ChatColor.RED).append("/unban <username or UUID>").color(ChatColor.WHITE).create());
            }
            else if (strings[0].length() < 16) {
                unbanUsername(strings[0], commandSender);
            }
            else {
                unbanUUID(strings[0], commandSender);
            }
        }
    }
    private static final String VerifyUsername = "SELECT UUID FROM UserInfo WHERE CachedUserName=?";
    private static final String CheckIfBanned = "SELECT ID FROM Bans WHERE UserUUID=? AND Active=?";
    private static final String BanUser = "UPDATE Bans SET RemovedByUUID=?, RemovedAt=?, Active=? WHERE ID=?";

    public void unbanUsername(final String name, final CommandSender commandSender) {
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
                    else {
                        commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.NO_USERNAME)).color(ChatColor.RED).create());
                    }
                    if (!itsOk) {
                        commandSender.sendMessage(new ComponentBuilder("That username has multiple definitions within the database! Please use an UUID so that the correct player is banned.").color(ChatColor.RED).create());
                    }
                    else {
                        try (Connection connection2 = Main.getHikari().getConnection();
                             PreparedStatement select2 = connection2.prepareStatement(CheckIfBanned)) {
                            select2.setString(1, uuid);
                            select2.setBoolean(2, true);
                            ResultSet result2 = select2.executeQuery();
                            if (result2.next()) {
                                final int id = result2.getInt("ID");
                                final Timestamp d = new Timestamp(System.currentTimeMillis());
                                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        try (Connection connection = Main.getHikari().getConnection();
                                             PreparedStatement insert = connection.prepareStatement(BanUser)) {
                                            if (commandSender instanceof ProxiedPlayer)
                                                insert.setString(1, ((ProxiedPlayer) commandSender).getUniqueId().toString());
                                            else
                                                insert.setString(1, "-CONSOLE-");
                                            insert.setTimestamp(2, d);
                                            insert.setBoolean(3, false);
                                            insert.setInt(4, id);

                                            insert.execute();

                                            for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
                                                if (p.hasPermission("bans.broadcast")) {
                                                    p.sendMessage(Main.getMessage(null, null, Main.ReasonType.UNBAN_BROADCAST, commandSender.getName(), name, null));
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
                            else {
                                commandSender.sendMessage(new ComponentBuilder("That user is not banned! If you feel this is a mistake please use the UUID instead.").color(ChatColor.RED).create());
                            }
                            result2.close();
                        }
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
    public void unbanUUID(final String uuid, final CommandSender commandSender) {

                        try (Connection connection2 = Main.getHikari().getConnection();
                             PreparedStatement select2 = connection2.prepareStatement(CheckIfBanned)) {
                            select2.setString(1, uuid);
                            select2.setBoolean(2, true);
                            ResultSet result2 = select2.executeQuery();
                            if (result2.next()) {
                                final int id = result2.getInt("ID");
                                final Timestamp d = new Timestamp(System.currentTimeMillis());
                                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        try (Connection connection = Main.getHikari().getConnection();
                                             PreparedStatement insert = connection.prepareStatement(BanUser)) {
                                            if (commandSender instanceof ProxiedPlayer)
                                                insert.setString(1, ((ProxiedPlayer) commandSender).getUniqueId().toString());
                                            else
                                                insert.setString(1, "-CONSOLE-");
                                            insert.setTimestamp(2, d);
                                            insert.setBoolean(3, false);
                                            insert.setInt(4, id);
                                            insert.execute();

                                            for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
                                                if (p.hasPermission("bans.broadcast")) {
                                                    p.sendMessage(Main.getMessage(null, null, Main.ReasonType.UNBAN_BROADCAST, commandSender.getName(), uuid, null));
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
                            else {
                                commandSender.sendMessage(new ComponentBuilder("That user is not banned! If you feel this is a mistake please contact TheDiamondPicks.").color(ChatColor.RED).create());
                            }
                            result2.close();
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                            commandSender.sendMessage(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                        }
    }
}

