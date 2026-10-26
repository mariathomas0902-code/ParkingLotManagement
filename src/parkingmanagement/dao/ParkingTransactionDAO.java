package parkingmanagement.dao;

import parkingmanagement.DatabaseConnection;
import parkingmanagement.VehicleType;
import parkingmanagement.model.ActiveParkingRecord;
import parkingmanagement.model.TransactionRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParkingTransactionDAO {

    public record ActiveTransaction(
            int transactionId,
            int spaceId,
            String spaceNumber,
            String vehicleType,
            LocalDateTime entryTime
    ) {
    }

    public int createTransaction(String vehicleNumber, int slotId) throws SQLException {
        String sql = """
                INSERT INTO parking_transactions
                (registration_number, slot_id, entry_time, transaction_status)
                VALUES (?, ?, NOW(), 'ACTIVE')
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, vehicleNumber.trim().toUpperCase(Locale.ROOT));
            statement.setInt(2, slotId);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Parking transaction was not created.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        throw new SQLException("Transaction ID was not generated.");
    }

    public ActiveTransaction findActiveTransaction(String vehicleNumber) throws SQLException {
        String sql = """
                SELECT
                    pt.ticket_id,
                    pt.slot_id,
                    ps.slot_id AS space_number,
                    v.vehicle_type,
                    pt.entry_time
                FROM parking_transactions pt
                JOIN vehicles v
                    ON pt.registration_number = v.registration_number
                JOIN parking_spaces ps
                    ON pt.slot_id = ps.slot_id
                WHERE pt.registration_number = ?
                  AND pt.transaction_status = 'ACTIVE'
                ORDER BY pt.ticket_id DESC
                LIMIT 1
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, vehicleNumber.trim().toUpperCase(Locale.ROOT));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new ActiveTransaction(
                            resultSet.getInt("ticket_id"),
                            resultSet.getInt("slot_id"),
                            String.valueOf(resultSet.getInt("space_number")),
                            resultSet.getString("vehicle_type"),
                            resultSet.getTimestamp("entry_time").toLocalDateTime()
                    );
                }
            }
        }

        return null;
    }

    public boolean completeTransaction(int ticketId, double parkingFee) throws SQLException {
        String sql = """
                UPDATE parking_transactions
                SET exit_time = NOW(),
                    parking_fee = ?,
                    transaction_status = 'COMPLETED'
                WHERE ticket_id = ?
                  AND transaction_status = 'ACTIVE'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setDouble(1, parkingFee);
            statement.setInt(2, ticketId);

            return statement.executeUpdate() > 0;
        }
    }

    public List<ActiveParkingRecord> getActiveTransactions() throws SQLException {
        List<ActiveParkingRecord> list = new ArrayList<>();
        String sql = """
                SELECT
                    pt.ticket_id,
                    pt.registration_number,
                    v.vehicle_type,
                    pt.slot_id,
                    COALESCE(c.name, 'Customer') AS customer_name,
                    COALESCE(c.phone_number, '-') AS phone_number,
                    pt.entry_time
                FROM parking_transactions pt
                JOIN vehicles v
                    ON pt.registration_number = v.registration_number
                LEFT JOIN customers c
                    ON v.customer_id = c.customer_id
                WHERE pt.transaction_status = 'ACTIVE'
                ORDER BY pt.entry_time DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                int ticketId = rs.getInt("ticket_id");
                String reg = rs.getString("registration_number");
                VehicleType vType = VehicleType.valueOf(rs.getString("vehicle_type"));
                int slotId = rs.getInt("slot_id");
                String customerName = rs.getString("customer_name");
                String phone = rs.getString("phone_number");
                LocalDateTime entryTime = rs.getTimestamp("entry_time").toLocalDateTime();

                list.add(new ActiveParkingRecord(
                        ticketId, reg, vType, slotId, customerName, phone, entryTime
                ));
            }
        }
        return list;
    }

    public List<TransactionRecord> getAllTransactions() throws SQLException {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = """
                SELECT
                    pt.ticket_id,
                    pt.registration_number,
                    v.vehicle_type,
                    pt.slot_id,
                    COALESCE(c.name, 'Customer') AS customer_name,
                    pt.entry_time,
                    pt.exit_time,
                    pt.parking_fee,
                    pt.transaction_status
                FROM parking_transactions pt
                LEFT JOIN vehicles v
                    ON pt.registration_number = v.registration_number
                LEFT JOIN customers c
                    ON v.customer_id = c.customer_id
                ORDER BY pt.ticket_id DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                int ticketId = rs.getInt("ticket_id");
                String reg = rs.getString("registration_number");
                String vTypeStr = rs.getString("vehicle_type");
                VehicleType vType = vTypeStr != null ? VehicleType.valueOf(vTypeStr) : VehicleType.CAR;
                int slotId = rs.getInt("slot_id");
                String customerName = rs.getString("customer_name");
                LocalDateTime entryTime = rs.getTimestamp("entry_time").toLocalDateTime();
                LocalDateTime exitTime = rs.getTimestamp("exit_time") != null
                        ? rs.getTimestamp("exit_time").toLocalDateTime()
                        : null;
                double fee = rs.getDouble("parking_fee");
                String status = rs.getString("transaction_status");

                list.add(new TransactionRecord(
                        ticketId, reg, vType, slotId, customerName, entryTime, exitTime, fee, status
                ));
            }
        }
        return list;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(parking_fee), 0.0) AS total_revenue
                FROM parking_transactions
                WHERE transaction_status = 'COMPLETED'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getDouble("total_revenue");
            }
        }
        return 0.0;
    }

    public int countActiveTransactions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_transactions WHERE transaction_status = 'ACTIVE'";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countCompletedTransactions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_transactions WHERE transaction_status = 'COMPLETED'";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void displayActiveTransactions() throws SQLException {
        List<ActiveParkingRecord> records = getActiveTransactions();
        System.out.println("\n===== CURRENTLY PARKED VEHICLES =====");
        if (records.isEmpty()) {
            System.out.println("No vehicles are currently parked.");
            return;
        }

        for (ActiveParkingRecord r : records) {
            System.out.println("Transaction ID : " + r.ticketId());
            System.out.println("Customer       : " + r.customerName());
            System.out.println("Vehicle Number : " + r.registrationNumber());
            System.out.println("Vehicle Type   : " + r.vehicleType());
            System.out.println("Parking Space  : " + r.slotId());
            System.out.println("Entry Time     : " + r.entryTime());
            System.out.println("------------------------------");
        }
    }

    public void displayTransactionHistory() throws SQLException {
        List<TransactionRecord> records = getAllTransactions();
        System.out.println("\n===== TRANSACTION HISTORY =====");
        if (records.isEmpty()) {
            System.out.println("No parking transactions found.");
            return;
        }

        for (TransactionRecord r : records) {
            System.out.println("Transaction ID : " + r.ticketId());
            System.out.println("Vehicle Number : " + r.registrationNumber());
            System.out.println("Parking Space  : " + r.slotId());
            System.out.println("Entry Time     : " + r.entryTime());
            System.out.println("Exit Time      : " + (r.exitTime() == null ? "Still Parked" : r.exitTime()));
            System.out.println("Parking Fee    : Rs." + r.parkingFee());
            System.out.println("Status         : " + r.status());
            System.out.println("------------------------------");
        }
    }
}
