package btz.login;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UnregisterCommand extends AbstractCommand {
    private final Main main;

    public UnregisterCommand(Main main) {
        super("unregister", "/unregister <gracz>", "Usuń gracza z bazy danych (tylko dla operatorów)", "§cNie masz uprawnień do tej komendy.", null);
        this.main = main;
        this.register();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("login.unregister")) {
            sender.sendMessage("§cTylko administrator może użyć tej komendy!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUżycie: /unregister <gracz>");
            return true;
        }

        String username = args[0];
        Connection connection = Main.getInstance().getConnection();

        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM users WHERE username = ?");
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sender.sendMessage("§aGracz §e" + username + " §azostał usunięty z bazy danych!");
            } else {
                sender.sendMessage("§cGracz §e" + username + " §cnie istnieje w bazie danych.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("§cWystąpił błąd podczas usuwania gracza z bazy danych.");
        }

        return true;
    }
}
