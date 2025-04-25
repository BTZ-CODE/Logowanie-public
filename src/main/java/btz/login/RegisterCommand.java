package btz.login;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterCommand extends AbstractCommand {
    private final Main main;

    public RegisterCommand(Main main) {
        super("register", "/register <hasło>", "Zarejestruj się", "§cNie masz uprawnień do tej komendy.", null);
        this.main = main;
        this.register();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("§cUżycie: /register <hasło>");
            return true;
        }

        String password = args[0];
        String username = player.getName();
        Connection connection = Main.getInstance().getConnection();




        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            player.sendMessage("§cHasło musi zawierać co najmniej 8 znaków, jedną dużą literę i jedną cyfrę!");
            return true;
        }




        try {
            PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                player.sendMessage("§cJuż jesteś zarejestrowany!");
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cWystąpił błąd podczas sprawdzania bazy danych.");
            return true;
        }



        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            stmt.executeUpdate();

            player.sendMessage("§aPomyślnie zarejestrowano!");
            Main.getInstance().getUnregisteredPlayers().remove(player);


        } catch (SQLException e) {
            e.printStackTrace();

            player.sendMessage("§cWystąpił błąd podczas przetwarzania twojego żądania.");
        }

        return true;
    }
}
