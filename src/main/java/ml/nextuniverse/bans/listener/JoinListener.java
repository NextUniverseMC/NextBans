package ml.nextuniverse.bans.listener;

import ml.nextuniverse.bans.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.*;
import java.util.UUID;

/**
 * Created by TheDiamondPicks on 28/09/2016.
 */
public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(final LoginEvent e) {
        e.registerIntent(Main.getInstance());
        final UUID p = e.getConnection().getUniqueId();
        userInfo(e);

        final Timestamp d = new Timestamp(System.currentTimeMillis());
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                try (Connection connection = Main.getHikari().getConnection();
                     PreparedStatement select = connection.prepareStatement(IsBanned)) {
                    select.setString(1, p.toString());
                    ResultSet result = select.executeQuery();
                    if (result.next()) {
                        if (result.getTimestamp("IntendedRemovalDate").getTime() < d.getTime()) {
                            e.setCancelReason(Main.getMessage(result.getString("Reason"), null, Main.ReasonType.PERMBAN_KICK, null, null, null));

                            e.setCancelled(true);
                            e.completeIntent(Main.getInstance());
                        }
                        else {
                            e.setCancelReason(Main.getMessage(result.getString("Reason"), result.getTimestamp("IntendedRemovalDate"), Main.ReasonType.TEMPBAN_KICK, null, null, null));
                            e.setCancelled(true);
                            e.completeIntent(Main.getInstance());
                        }
                    }
                    else {
                        e.completeIntent(Main.getInstance());
                    }
                    result.close();
                }
                catch (SQLException ex) {
                    e.setCancelReason(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                    e.setCancelled(true);
                    ex.printStackTrace();

                }
            }
        });

    }

    private static final String UsercacheInsert = "INSERT INTO UserInfo VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE CachedUserName=?, LastIP=?, LastLoggedIn=?";

    private static final String IsBanned = "SELECT * FROM Bans WHERE UserUUID=? AND Active=true";

    public void userInfo(final LoginEvent e) {
        final Timestamp d = new Timestamp(System.currentTimeMillis());
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                try (Connection connection = Main.getHikari().getConnection();
                     PreparedStatement insert = connection.prepareStatement(UsercacheInsert)) {
                    insert.setString(1, e.getConnection().getUniqueId().toString());
                    insert.setString(2, e.getConnection().getName());
                    insert.setString(3, e.getConnection().getAddress().toString().replace("/", "").split(":")[0]);
                    insert.setTimestamp(4, d);
                    insert.setTimestamp(5, d);
                    insert.setString(6, e.getConnection().getName());
                    insert.setString(7, e.getConnection().getAddress().toString().replace("/", "").split(":")[0]);
                    insert.setTimestamp(8, d);
                    insert.execute();
                }
                catch (SQLException ex) {
                    e.setCancelReason(new ComponentBuilder(Main.getErrorMessage(Main.MessageType.SQL_ERROR)).color(ChatColor.RED).create());
                    e.setCancelled(true);
                    ex.printStackTrace();
                }
            }
        });

    }

    public void isBanned(final ProxiedPlayer p) {

    }
}
