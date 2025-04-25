package btz.login;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginCommand extends AbstractCommand {
    private final Main main;

    public LoginCommand(Main main) {
        super("login", "/login <hasło>", "Zaloguj się na swoje konto", "§cNie masz uprawnień do tej komendy.", null);
        this.main = main;
        this.register();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("§cUżycie: /login <hasło>");
            return true;
        }

        String username = player.getName();
        String password = args[0];
        Connection connection = Main.getInstance().getConnection();


        if (!Main.getInstance().getUnregisteredPlayers().contains(player)) {
            player.sendMessage("§cJuż jesteś zalogowany!");
            return true;
        }


        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT password FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    player.sendMessage("§aPomyślnie zalogowano!");
                    Main.getInstance().getUnregisteredPlayers().remove(player);
                    return true;
                }
            }
            player.sendMessage("§cNieprawidłowe hasło.");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cWystąpił błąd podczas przetwarzania twojego żądania.");
        }

        return true;
    }
}
