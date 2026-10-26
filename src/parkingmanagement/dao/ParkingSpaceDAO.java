package parkingmanagement.dao;

import parkingmanagement.DatabaseConnection;
import parkingmanagement.ParkingSpace;
import parkingmanagement.SlotStatus;
import parkingmanagement.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ParkingSpaceDAO {

    public Integer findAvailableSpace(String vehicleType) throws SQLException {
        String sql = """
                SELECT slot_id
                FROM parking_spaces
                WHERE supported_type = ?
                  AND status = 'AVAILABLE'
                ORDER BY slot_id
                LIMIT 1
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, vehicleType);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("slot_id");
                }
            }
        }

        return null;
    }

    public void updateSpaceStatus(int slotId, String status) throws SQLException {
        String sql = """
                UPDATE parking_spaces
                SET status = ?
                WHERE slot_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, status);
            statement.setInt(2, slotId);
            statement.executeUpdate();
        }
    }

    public List<ParkingSpace> getAllSpaces() throws SQLException {
        List<ParkingSpace> spaces = new ArrayList<>();
        String sql = """
                SELECT slot_id, supported_type, status
                FROM parking_spaces
                ORDER BY slot_id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                int slotId = resultSet.getInt("slot_id");
                VehicleType type = VehicleType.valueOf(resultSet.getString("supported_type"));
                SlotStatus status = SlotStatus.valueOf(resultSet.getString("status"));

                ParkingSpace space = new ParkingSpace(slotId, type);
                if (status == SlotStatus.OCCUPIED) {
                    space.occupy();
                }
                spaces.add(space);
            }
        }
        return spaces;
    }

    public List<ParkingSpace> getAvailableSpaces() throws SQLException {
        List<ParkingSpace> spaces = new ArrayList<>();
        String sql = """
                SELECT slot_id, supported_type, status
                FROM parking_spaces
                WHERE status = 'AVAILABLE'
                ORDER BY slot_id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                int slotId = resultSet.getInt("slot_id");
                VehicleType type = VehicleType.valueOf(resultSet.getString("supported_type"));
                spaces.add(new ParkingSpace(slotId, type));
            }
        }
        return spaces;
    }

    public Map<VehicleType, Integer> getAvailableSpacesCountByType() throws SQLException {
        Map<VehicleType, Integer> countMap = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            countMap.put(type, 0);
        }

        String sql = """
                SELECT supported_type, COUNT(*) AS available_count
                FROM parking_spaces
                WHERE status = 'AVAILABLE'
                GROUP BY supported_type
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                try {
                    VehicleType type = VehicleType.valueOf(resultSet.getString("supported_type"));
                    countMap.put(type, resultSet.getInt("available_count"));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return countMap;
    }

    public Map<VehicleType, Integer> getTotalSpacesCountByType() throws SQLException {
        Map<VehicleType, Integer> countMap = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            countMap.put(type, 0);
        }

        String sql = """
                SELECT supported_type, COUNT(*) AS total_count
                FROM parking_spaces
                GROUP BY supported_type
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                try {
                    VehicleType type = VehicleType.valueOf(resultSet.getString("supported_type"));
                    countMap.put(type, resultSet.getInt("total_count"));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return countMap;
    }

    public int countTotalSpaces() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_spaces";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    public int countAvailableSpaces() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_spaces WHERE status = 'AVAILABLE'";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    public int countOccupiedSpaces() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_spaces WHERE status = 'OCCUPIED'";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    public void displayAvailableSpaces() throws SQLException {
        String sql = """
                SELECT slot_id, supported_type, status
                FROM parking_spaces
                WHERE status = 'AVAILABLE'
                ORDER BY slot_id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            System.out.println("\n===== AVAILABLE PARKING SPACES =====");
            boolean found = false;
            while (resultSet.next()) {
                found = true;
                System.out.println(
                        "Slot ID: " + resultSet.getInt("slot_id")
                                + " | Vehicle Type: "
                                + resultSet.getString("supported_type")
                                + " | Status: "
                                + resultSet.getString("status")
                );
            }
            if (!found) {
                System.out.println("No parking spaces are available.");
            }
        }
    }
}
