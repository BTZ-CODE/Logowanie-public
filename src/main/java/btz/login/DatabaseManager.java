package btz.login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final Main main;
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    public DatabaseManager(Main main) {
        this.main = main;
        this.URL = main.getConfig().getString("url", "twoj_url_bazy_danych");
        this.USER = main.getConfig().getString("username", "twoja_baza_danych");
        this.PASSWORD = main.getConfig().getString("password", "twoje_haslo");
    }

    public Connection initDatabase() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[BTZ-Login] Połączono z bazą danych!");

            createTables(connection);

            return connection;
        } catch (SQLException e) {
            System.err.println("[BTZ-Login] Nie udało się połączyć z bazą danych:");
            e.printStackTrace();
            return null;
        }
    }

    private void createTables(Connection connection) {
        String createUsersTable = """
        CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) NOT NULL,
            password VARCHAR(255) NOT NULL,
            is_premium TINYINT(1) NOT NULL DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """;


        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createUsersTable);
            System.out.println("[BTZ-Login] Tabele zostały poprawnie utworzone!");
        } catch (SQLException e) {
            System.err.println("[BTZ-Login] Błąd podczas tworzenia tabel:");
            e.printStackTrace();
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[BTZ-Login] Zamknięto połączenie z bazą danych.");
            } catch (SQLException e) {
                System.err.println("[BTZ-Login] Błąd podczas zamykania połączenia z bazą danych:");
                e.printStackTrace();
            }
        }
    }
}