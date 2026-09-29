package parkingmanagement;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            System.out.println(
                    "Connected to parking_management successfully."
            );

            System.out.println(
                    "Database name: "
                            + connection.getCatalog()
            );

        } catch (SQLException exception) {

            System.out.println(
                    "Database connection failed."
            );

            System.out.println(
                    "Reason: " + exception.getMessage()
            );
        }
    }
}