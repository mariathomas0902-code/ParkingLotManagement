package parkingmanagement.dao;

import parkingmanagement.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingSpaceDAO {

    public Integer findAvailableSpace(String vehicleType)
            throws SQLException {

        String sql = """
        SELECT slot_id
        FROM parking_spaces
        WHERE supported_type = ?
        AND status = 'AVAILABLE'
        ORDER BY slot_id
        LIMIT 1
        """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, vehicleType);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("space_id");
                }
            }
        }

        return null;
    }

    public void updateSpaceStatus(
            int spaceId,
            String status
    ) throws SQLException {

        String sql = """
                UPDATE parking_spaces
                SET status = ?
                WHERE space_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, status);
            statement.setInt(2, spaceId);
            statement.executeUpdate();
        }
    }
    public void displayAvailableSpaces() throws SQLException {

        String sql = """
            SELECT space_id,
                   space_number,
                   vehicle_type,
                   status
            FROM parking_spaces
            WHERE status = 'AVAILABLE'
            ORDER BY space_number
            """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            System.out.println(
                    "\n===== AVAILABLE PARKING SPACES ====="
            );

            boolean found = false;

            while (resultSet.next()) {
                found = true;

                System.out.println(
                        resultSet.getString("space_number")
                                + " - "
                                + resultSet.getString("vehicle_type")
                );
            }

            if (!found) {
                System.out.println(
                        "No parking spaces are available."
                );
            }
        }
    }
}