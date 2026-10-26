package parkingmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/parking_management"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=Asia/Kolkata";

    private static final String PRIMARY_USER = "parking_app";
    private static final String PRIMARY_PASS = "root";

    private static final String FALLBACK_USER = "root";
    private static final String FALLBACK_PASS = "root";

    private static final String XAMPP_USER = "root";
    private static final String XAMPP_PASS = "";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, PRIMARY_USER, PRIMARY_PASS);
        } catch (SQLException ex1) {
            try {
                return DriverManager.getConnection(URL, FALLBACK_USER, FALLBACK_PASS);
            } catch (SQLException ex2) {
                try {
                    return DriverManager.getConnection(URL, XAMPP_USER, XAMPP_PASS);
                } catch (SQLException ex3) {
                    throw ex1;
                }
            }
        }
    }

    public static boolean isConnected() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (Exception exception) {
            return false;
        }
    }
}
