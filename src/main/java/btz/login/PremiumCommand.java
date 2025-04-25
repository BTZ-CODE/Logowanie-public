package btz.login;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PremiumCommand extends AbstractCommand {
    private final Main main;

    public PremiumCommand(Main main) {
        super("premium", "/premium <hasło> <potwierdź hasło>", "Aktywuj status premium i pomijaj logowanie", "§cNie masz uprawnień do tej komendy.", null);
        this.main = main;
        this.register();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage("§cUżycie: /premium <hasło> <potwierdź hasło>");
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];
        String username = player.getName();
        Connection connection = Main.getInstance().getConnection();

        if (!password.equals(confirmPassword)) {
            player.sendMessage("§cHasła nie są zgodne!");
            return true;
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            player.sendMessage("§cHasło musi zawierać co najmniej 8 znaków, jedną dużą literę i jedną cyfrę!");
            return true;
        }

        if (!isPremiumAccount(username, player.getUniqueId())) {
            player.sendMessage("§cTwoje konto Minecraft nie jest premium lub API Mojang jest niedostępne!");
            return true;
        }

        try {
            PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                player.sendMessage("§cNajpierw musisz się zarejestrować komendą /register <hasło>!");
                return true;
            }

            String dbPassword = rs.getString("password");
            if (!BCrypt.checkpw(password, dbPassword)) {
                player.sendMessage("§cPodane hasło nie pasuje do tego przypisanego do twojego konta!");
                return true;
            }



            PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE users SET is_premium = TRUE, password = ? WHERE username = ?"
            );

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            updateStmt.setString(1, hashedPassword);
            updateStmt.setString(2, username);
            updateStmt.executeUpdate();

            player.sendMessage("§aPomyślnie aktywowano status premium! Od teraz nie musisz się logować!");
        } catch (SQLException e) {

            e.printStackTrace();
            player.sendMessage("§cWystąpił błąd podczas przetwarzania twojego żądania.");
        }

        return true;
    }

    private boolean isPremiumAccount(String username, UUID playerUUID) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }

                    JsonObject json = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
                    if (json.has("id")) {
                        String mojangUUID = json.get("id").getAsString();
                        String formattedUUID = mojangUUID.replaceFirst(
                                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                                "$1-$2-$3-$4-$5"
                        );
                        return UUID.fromString(formattedUUID).equals(playerUUID);
                    }
                }
            } else {
                System.out.println("Mojang API odpowiedziało kodem: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}