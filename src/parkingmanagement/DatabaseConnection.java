package parkingmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/parking_management"
                    + "?useSSL=false"
                    + "&serverTimezone=Asia/Kolkata";

    private static final String USERNAME =
            "parking_app";

    private static final String PASSWORD =
            "  ";

    private DatabaseConnection() {
    }

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD
        );
    }
}