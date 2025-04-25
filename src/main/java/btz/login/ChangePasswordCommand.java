package btz.login;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangePasswordCommand extends AbstractCommand {
    private final Main main;

    public ChangePasswordCommand(Main main) {
        super("changepassword", "/changepassword <nowe hasło>", "Zmień swoje hasło na serwerze", "§cNie masz uprawnień do użycia tej komendy.", null);
        this.main = main;
        this.register();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("§cUżycie: /changepassword <nowe hasło>");
            return true;
        }

        String newPassword = args[0];

        if (!newPassword.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            player.sendMessage("§cHasło musi zawierać co najmniej 8 znaków, jedną dużą literę i jedną cyfrę!");
            return true;
        }

        String username = player.getName();
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        Connection connection = main.getConnection();

        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET password = ? WHERE username = ?");
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);
            stmt.executeUpdate();

            player.sendMessage("§aHasło zostało zmienione pomyślnie!");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cWystąpił błąd podczas zmiany hasła.");
        }

        return true;
    }
}
