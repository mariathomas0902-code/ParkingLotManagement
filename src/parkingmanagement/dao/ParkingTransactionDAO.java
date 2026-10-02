package parkingmanagement.dao;

import parkingmanagement.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class ParkingTransactionDAO {

    /*
     * Stores the details required when processing
     * the exit of a parked vehicle.
     */
    public record ActiveTransaction(
            int transactionId,
            int spaceId,
            String spaceNumber,
            String vehicleType,
            LocalDateTime entryTime
    ) {
    }

    // The vehicle must already exist in the vehicles table.
    // Pass its registration number, not the customer ID.
    public int createTransaction(
            String vehicleNumber,
            int slot_id
    ) throws SQLException {

        String sql = """
                INSERT INTO parking_transactions
                (registration_number, slot_id, entry_time,
                 transaction_status)
                VALUES (?, ?, NOW(), 'ACTIVE')
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(1, vehicleNumber);
            statement.setInt(2, slot_id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Parking transaction was not created."
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Transaction ID was not generated."
        );
    }

    // Find the active transaction using vehicle number
    public ActiveTransaction findActiveTransaction(
            String vehicleNumber
    ) throws SQLException {

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
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, vehicleNumber);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return new ActiveTransaction(
                            resultSet.getInt("ticket_id"),
                            resultSet.getInt("slot_id"),
                            resultSet.getString("space_number"),
                            resultSet.getString("vehicle_type"),
                            resultSet.getTimestamp("entry_time")
                                    .toLocalDateTime()
                    );
                }
            }
        }

        return null;
    }

    // Complete a transaction during vehicle exit
    public boolean completeTransaction(
            int transactionId,
            double parkingFee
    ) throws SQLException {

        String sql = """
                UPDATE parking_transactions
                SET exit_time = NOW(),
                    parking_fee = ?,
                    transaction_status = 'COMPLETED'
                WHERE ticket_id = ?
                  AND transaction_status = 'ACTIVE'
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setDouble(1, parkingFee);
            statement.setInt(2, transactionId);

            return statement.executeUpdate() > 0;
        }
    }

    // Display vehicles that are currently parked
    public void displayActiveTransactions()
            throws SQLException {

        String sql = """
                SELECT
                    pt.ticket_id,
                    c.name AS customer_name,
                    pt.registration_number AS vehicle_number,
                    v.vehicle_type,
                    ps.slot_id AS space_number,
                    pt.entry_time
                FROM parking_transactions pt
                JOIN vehicles v
                    ON pt.registration_number = v.registration_number
                LEFT JOIN customers c
                    ON v.customer_id = c.customer_id
                JOIN parking_spaces ps
                    ON pt.slot_id = ps.slot_id
                WHERE pt.transaction_status = 'ACTIVE'
                ORDER BY pt.entry_time
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
                    "\n===== CURRENTLY PARKED VEHICLES ====="
            );

            boolean found = false;

            while (resultSet.next()) {
                found = true;

                System.out.println(
                        "Transaction ID : "
                                + resultSet.getInt("ticket_id")
                );

                System.out.println(
                        "Customer       : "
                                + resultSet.getString("customer_name")
                );

                System.out.println(
                        "Vehicle Number : "
                                + resultSet.getString("vehicle_number")
                );

                System.out.println(
                        "Vehicle Type   : "
                                + resultSet.getString("vehicle_type")
                );

                System.out.println(
                        "Parking Space  : "
                                + resultSet.getString("space_number")
                );

                System.out.println(
                        "Entry Time     : "
                                + resultSet.getTimestamp("entry_time")
                );

                System.out.println("------------------------------");
            }

            if (!found) {
                System.out.println(
                        "No vehicles are currently parked."
                );
            }
        }
    }

    // Display completed and active transaction history
    public void displayTransactionHistory()
            throws SQLException {

        String sql = """
                SELECT
                    pt.ticket_id,
                    pt.registration_number AS vehicle_number,
                    ps.slot_id AS space_number,
                    pt.entry_time,
                    pt.exit_time,
                    pt.parking_fee,
                    pt.transaction_status
                FROM parking_transactions pt
                JOIN parking_spaces ps
                    ON pt.slot_id = ps.slot_id
                ORDER BY pt.ticket_id DESC
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
                    "\n===== TRANSACTION HISTORY ====="
            );

            boolean found = false;

            while (resultSet.next()) {
                found = true;

                System.out.println(
                        "Transaction ID : "
                                + resultSet.getInt("ticket_id")
                );

                System.out.println(
                        "Vehicle Number : "
                                + resultSet.getString("vehicle_number")
                );

                System.out.println(
                        "Parking Space  : "
                                + resultSet.getString("space_number")
                );

                System.out.println(
                        "Entry Time     : "
                                + resultSet.getTimestamp("entry_time")
                );

                System.out.println(
                        "Exit Time      : "
                                + resultSet.getTimestamp("exit_time")
                );

                System.out.println(
                        "Parking Fee    : Rs."
                                + resultSet.getDouble("parking_fee")
                );

                System.out.println(
                        "Status         : "
                                + resultSet.getString(
                                "transaction_status"
                        )
                );

                System.out.println("------------------------------");
            }

            if (!found) {
                System.out.println(
                        "No parking transactions found."
                );
            }
        }
    }
}