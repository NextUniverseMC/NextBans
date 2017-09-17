package ml.nextuniverse.bans;

import com.zaxxer.hikari.HikariDataSource;
import ml.nextuniverse.bans.commands.bans.BanCommand;
import ml.nextuniverse.bans.commands.bans.TempbanCommand;
import ml.nextuniverse.bans.commands.bans.UnbanCommand;
import ml.nextuniverse.bans.listener.JoinListener;
import ml.nextuniverse.bans.commands.KickCommand;
import ml.nextuniverse.bans.commands.mutes.MuteCommand;
import ml.nextuniverse.bans.commands.mutes.TempmuteCommand;
import ml.nextuniverse.bans.commands.mutes.UnmuteCommand;
import ml.nextuniverse.bans.util.DateFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by TheDiamondPicks on 28/09/2016
 */
public class Main extends Plugin {

    private static Main instance;
    private static HikariDataSource hikari;

    private static final String ExpireBans = "UPDATE Bans SET RemovedByUUID=?, RemovedAt=?, Active=? WHERE IntendedRemovalDate<=? AND Active=?  AND RemovedByUUID!=?";
    private static final String ExpireMutes = "UPDATE Mutes SET RemovedByUUID=?, RemovedAt=?, Active=? WHERE IntendedRemovalDate<=? AND Active=?  AND RemovedByUUID!=?";
    private static final String ExpireWarns = "UPDATE Warns SET RemovedByUUID=?, RemovedAt=?, Active=? WHERE IntendedRemovalDate<=? AND Active=?  AND RemovedByUUID!=?";

    @Override
    public void onEnable() {
        instance = this;

        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            hikari = new HikariDataSource();
            hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            hikari.addDataSourceProperty("serverName", configuration.getString("serverName"));
            hikari.addDataSourceProperty("port",  3306);
            hikari.addDataSourceProperty("databaseName",  configuration.getString("databaseName"));
            hikari.addDataSourceProperty("user",  configuration.getString("user"));
            hikari.addDataSourceProperty("password",  configuration.getString("password"));
        }
        catch (IOException e) {
            getLogger().severe("Could not load config");
            e.printStackTrace();
            ProxyServer.getInstance().stop();
        }
        createTable();

        getProxy().getPluginManager().registerListener(this, new JoinListener());

        getProxy().getPluginManager().registerCommand(this, new BanCommand());
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand());
        getProxy().getPluginManager().registerCommand(this, new TempbanCommand());

        getProxy().getPluginManager().registerCommand(this, new MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand());
        getProxy().getPluginManager().registerCommand(this, new TempmuteCommand());

        getProxy().getPluginManager().registerCommand(this, new KickCommand());

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        try (Connection connection = Main.getHikari().getConnection();
                             PreparedStatement select = connection.prepareStatement(ExpireBans)) {
                            select.setString(1, "-EXPIRED-");
                            select.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                            select.setBoolean(3, false);
                            select.setString(6, "-PERMBAN-");
                            Timestamp d = new Timestamp(System.currentTimeMillis());
                            Calendar c = Calendar.getInstance();
                            c.setTime(d);
                            c.add(Calendar.SECOND, 1);
                            d = new Timestamp(c.getTimeInMillis());
                            select.setTimestamp(4, d);
                            select.setBoolean(5, true);
                            select.execute();
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 1, 1, TimeUnit.SECONDS);

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        try (Connection connection = Main.getHikari().getConnection();
                             PreparedStatement select = connection.prepareStatement(ExpireMutes)) {
                            select.setString(1, "-EXPIRED-");
                            select.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                            select.setBoolean(3, false);
                            select.setString(6, "-PERMBAN-");
                            Timestamp d = new Timestamp(System.currentTimeMillis());
                            Calendar c = Calendar.getInstance();
                            c.setTime(d);
                            c.add(Calendar.SECOND, 1);
                            d = new Timestamp(c.getTimeInMillis());
                            select.setTimestamp(4, d);
                            select.setBoolean(5, true);
                            select.execute();
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 1, 1, TimeUnit.SECONDS);

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        try (Connection connection = Main.getHikari().getConnection();
                             PreparedStatement select = connection.prepareStatement(ExpireWarns)) {
                            select.setString(1, "-EXPIRED-");
                            select.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                            select.setBoolean(3, false);
                            select.setString(6, "-PERMBAN-");
                            Timestamp d = new Timestamp(System.currentTimeMillis());
                            Calendar c = Calendar.getInstance();
                            c.setTime(d);
                            c.add(Calendar.SECOND, 1);
                            d = new Timestamp(c.getTimeInMillis());
                            select.setTimestamp(4, d);
                            select.setBoolean(5, true);
                            select.execute();
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 1, 1, TimeUnit.SECONDS);


    }

    public void createTable(){
        try(Connection connection = hikari.getConnection();
            Statement statement = connection.createStatement();){
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Bans(ID int AUTO_INCREMENT, UserUUID VARCHAR(36), Reason varchar(64), AppliedAt DATETIME, ApplierUUID varchar(36), IntendedRemovalDate DATETIME, RemovedByUUID varchar(36), RemovedAt DATETIME, Active boolean, PRIMARY KEY (ID))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Mutes(ID int AUTO_INCREMENT, UserUUID VARCHAR(36), Reason varchar(64), AppliedAt DATETIME, ApplierUUID varchar(36), IntendedRemovalDate DATETIME, RemovedByUUID varchar(36), RemovedAt DATETIME, Active boolean, PRIMARY KEY (ID))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Warns(ID int AUTO_INCREMENT, UserUUID VARCHAR(36), Reason varchar(64), Category varchar64, AppliedAt DATETIME, ApplierUUID varchar(36), IntendedRemovalDate DATETIME, RemovedByUUID varchar(36), RemovedAt DATETIME, Active boolean, PRIMARY KEY (ID))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Kicks(ID int AUTO_INCREMENT, UserUUID VARCHAR(36), Reason varchar(64), AppliedAt DATETIME, ApplierUUID varchar(36), PRIMARY KEY (ID))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS UserInfo(UUID varchar(36), CachedUserName VARCHAR(16), LastIP varchar(32), LastLoggedIn DATETIME, FirstLogin DATETIME, PRIMARY KEY (UUID))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Plugin getInstance() {
        return instance;
    }
    public static HikariDataSource getHikari() {
        return hikari;
    }

    public static BaseComponent[] getMessage(String reason, Timestamp time, ReasonType r, String executor, String name, String rawDate)  {
        String relTime = "";
        if (r == ReasonType.TEMPBAN_KICK || r == ReasonType.TEMPMUTE_MESSAGE) {
            relTime = DateFormatter.format(time);
        }
        if (r == ReasonType.TEMPBAN_KICK)
            return new ComponentBuilder("Kicked from the server:").color(ChatColor.DARK_RED).append("\nYou have been temporarily banned!").color(ChatColor.RED).bold(true).append("\n\nReason: ").color(ChatColor.DARK_RED).bold(false).append(reason).color(ChatColor.WHITE).append("\nExpires In: ").color(ChatColor.DARK_RED).bold(false).append(relTime).color(ChatColor.WHITE).append("\n\nIf you feel you have been unfairly banned you can appeal at ").color(ChatColor.AQUA).append("forums.nextuniverse.ml").color(ChatColor.DARK_AQUA).create();
        else if (r == ReasonType.TEMPBAN_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" temporarily banned user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(" for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append(" (").color(ChatColor.DARK_GREEN).append(rawDate).color(ChatColor.GREEN).append(").").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.PERMBAN_KICK)
            return new ComponentBuilder("Kicked from the server:").color(ChatColor.DARK_RED).append("\nYou have been permanently banned!").color(ChatColor.RED).bold(true).append("\n\nReason: ").color(ChatColor.DARK_RED).bold(false).append(reason).color(ChatColor.WHITE).append("\nExpires In: ").color(ChatColor.DARK_RED).bold(false).append("\n\nIf you feel you have been unfairly banned you can appeal at ").color(ChatColor.AQUA).append("forums.nextuniverse.ml").color(ChatColor.DARK_AQUA).create();
        else if (r == ReasonType.PERMBAN_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" permanently banned user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(" for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
               else if (r == ReasonType.PERMMUTE_MESSAGE)
            return new ComponentBuilder("\n[NOTICE]\n").color(ChatColor.DARK_GREEN).bold(true).append("You have been permanently muted for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append( "and therefore cannot chat! \nIf you feel that you have been unfairly cautioned you can appeal at").color(ChatColor.DARK_GREEN).append(" forums.nextuniverse.ml\n").color(ChatColor.GREEN).create();
        else if (r == ReasonType.PERMMUTE_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" permanently muted user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(" for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.TEMPMUTE_MESSAGE)
            return new ComponentBuilder("\n[NOTICE]\n").color(ChatColor.DARK_GREEN).bold(true).append("You have been temporarily muted for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append( "and therefore cannot chat! The mute expires in").color(ChatColor.DARK_GREEN).append(relTime).color(ChatColor.GREEN).append("\nIf you feel that you have been unfairly cautioned you can appeal at").color(ChatColor.DARK_GREEN).append(" forums.nextuniverse.ml\n").color(ChatColor.GREEN).create();
        else if (r == ReasonType.TEMPMUTE_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" temporarily muted user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(" for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append(" (").color(ChatColor.DARK_GREEN).append(rawDate).color(ChatColor.GREEN).append(").").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.WARN_KICK)
            return new ComponentBuilder("Kicked from the server:").color(ChatColor.DARK_RED).append("\nYou have received a warning!").color(ChatColor.RED).bold(true).append("\n\nReason: ").color(ChatColor.DARK_RED).bold(false).append(reason).color(ChatColor.WHITE).append("\n\nIf you feel you have been unfairly warned you can appeal at ").color(ChatColor.AQUA).append("forums.nextuniverse.ml").color(ChatColor.DARK_AQUA).create();
        else if (r == ReasonType.WARN_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" warned user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(" for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.INFRACTION_MESSAGE)
            return new ComponentBuilder("\n[NOTICE]\n").color(ChatColor.DARK_GREEN).bold(true).append(" You have received a caution! ").color(ChatColor.DARK_GREEN).bold(false).append(reason).color(ChatColor.GREEN).append(". Further rule-breaking may result in a warning, mute or ban!\nIf you feel that you have been unfairly cautioned you can appeal at").color(ChatColor.DARK_GREEN).append(" forums.nextuniverse.ml\n").color(ChatColor.GREEN).create();
        else if (r == ReasonType.INFRACTION_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" cautioned user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(" for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.KICK_KICK)
            return new ComponentBuilder("Kicked from the server:").color(ChatColor.DARK_RED).append("\n\nReason: ").color(ChatColor.DARK_RED).bold(false).append(reason).color(ChatColor.WHITE).append("\n\nIf you feel you have been unfairly kicked you can appeal at ").color(ChatColor.AQUA).append("forums.nextuniverse.ml").color(ChatColor.DARK_AQUA).create();
        else if (r == ReasonType.KICK_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" kicked user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(" for ").color(ChatColor.DARK_GREEN).append(reason).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.UNBAN_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" unbanned user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.UNMUTE_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" unmuted user ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.UNWARN_BROADCAST)
            return new ComponentBuilder("[!] ").color(ChatColor.DARK_GREEN).bold(true).append(executor).bold(false).color(ChatColor.GREEN).append(" removed warning ").color(ChatColor.DARK_GREEN).append(name).color(ChatColor.GREEN).append(".").color(ChatColor.DARK_GREEN).create();
        else if (r == ReasonType.OFFLINE_MESSAGE)
            return new ComponentBuilder("WARNING!").color(ChatColor.DARK_RED).append(" That player is offline!").color(ChatColor.WHITE).create();
        else if (r == ReasonType.INVALID_TIMESTAMP)
            return new ComponentBuilder("Invalid timestamp format. Valid timestamp formats are: 1d (1 day) 2h (2 hours) 3m (3 minutes) and 4s (4 seconds)").color(ChatColor.RED).create();
        else
            return null;
    }
    public static String getErrorMessage(MessageType m) {
        if (m == MessageType.SQL_ERROR)
            return "An unexpected database error occurred while attempting to perform this command. Please contact TheDiamondPicks along with a time this happened.";
        else if (m == MessageType.NO_USERNAME)
            return "That player is not in the database. Please ensure you spelt the name correctly. If you feel this is a mistake use the UUID or contact TheDiamondPicks.";
        else if (m == MessageType.NO_UUID)
            return "That UUID is not in the database. Please ensure you spelt the UUID correctly. If you feel this is a mistake contact TheDiamondPicks.";
            return null;

    }
    public enum ReasonType {
        TEMPBAN_KICK, TEMPBAN_BROADCAST, PERMBAN_KICK, PERMBAN_BROADCAST, UNBAN_BROADCAST, TEMPMUTE_MESSAGE, TEMPMUTE_BROADCAST,
        PERMMUTE_MESSAGE, PERMMUTE_BROADCAST, WARN_KICK, WARN_BROADCAST, INFRACTION_MESSAGE, INFRACTION_BROADCAST, KICK_KICK, KICK_BROADCAST,
        OFFLINE_MESSAGE, INVALID_TIMESTAMP, UNMUTE_BROADCAST, UNWARN_BROADCAST
    }
    public enum MessageType {
        SQL_ERROR, NO_USERNAME, NO_UUID
    }
}






