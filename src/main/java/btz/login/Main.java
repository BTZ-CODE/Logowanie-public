package btz.login;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private Connection connection;
    private final Set<Player> unregisteredPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;

        DatabaseManager databaseManager = new DatabaseManager(this);
        this.connection = databaseManager.initDatabase();

        Bukkit.getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        new LoginCommand(this);
        new RegisterCommand(this);
        new UnregisterCommand(this);
        new ChangePasswordCommand(this);
        new PremiumCommand(this);
    }

    @Override
    public void onDisable() {
        DatabaseManager.closeConnection(this.connection);
    }

    public static Main getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public Set<Player> getUnregisteredPlayers() {
        return unregisteredPlayers;
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (unregisteredPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();

        if (isPremiumAccount(username)) {

            try {
                PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                checkStmt.setString(1, username);

                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    if (rs.getBoolean("is_premium")) {


                        unregisteredPlayers.remove(player);
                        player.sendMessage("§aWitaj, " + username + "! Pominąłeś logowanie (premium).");
                    } else {

                        unregisteredPlayers.add(player);

                        player.sendTitle("§6Zaloguj sie lub zarejestruj", "§6Zaloguj sie lub zarejestruj");
                        player.sendMessage("§cMusisz się zarejestrować lub zalogować w przeciągu 30 sekund.");
                    }
                } else {

                    unregisteredPlayers.add(player);

                    player.sendTitle("§6Zaloguj sie lub zarejestruj", "§6Zaloguj sie lub zarejestruj");
                    player.sendMessage("§cMusisz się zarejestrować lub zalogować w przeciągu 30 sekund.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage("§cWystąpił błąd, zaloguj się ponownie");
            }
        } else {
            unregisteredPlayers.add(player);

            player.sendTitle("§6Zaloguj sie lub zarejestruj", "§6Zaloguj sie lub zarejestruj");
            player.sendMessage("§cMusisz zarejestrować lub zalogować się w przeciągu 30 sekund.");
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (unregisteredPlayers.contains(player)) {
                player.kickPlayer("§cNie udało ci się zalogować lub zarejestrować na czas.");
                unregisteredPlayers.remove(player);
            }
        }, 30 * 20L);
    }


    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (unregisteredPlayers.contains(player)) {
            String message = event.getMessage().toLowerCase();
            if (!message.startsWith("/login") && !message.startsWith("/register")) {
                event.setCancelled(true);
                player.sendMessage("§cMusisz się najpierw zalogować lub zarejestrować!");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (unregisteredPlayers.contains(player)) {
                event.setCancelled(true);
                player.sendMessage("§cMusisz się najpierw zalogować lub zarejestrować!");
            }
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (unregisteredPlayers.contains(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (unregisteredPlayers.contains(player)) {
            event.setCancelled(true);
            player.sendMessage("§cMusisz się najpierw zalogować lub zarejestrować!");
        }
    }



    private boolean isPremiumAccount(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }


        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT is_premium FROM users WHERE username = ?");

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_premium");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return false;
    }

}
