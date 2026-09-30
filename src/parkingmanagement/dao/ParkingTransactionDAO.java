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

    // Create a new parking transaction
    public int createTransaction(
            int customerId,
            int spaceId
    ) throws SQLException {

        String sql = """
                INSERT INTO parking_transactions
                (customer_id, space_id, entry_time,
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
            statement.setInt(1, customerId);
            statement.setInt(2, spaceId);

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
                    pt.transaction_id,
                    pt.space_id,
                    ps.space_number,
                    c.vehicle_type,
                    pt.entry_time
                FROM parking_transactions pt
                JOIN customers c
                    ON pt.customer_id = c.customer_id
                JOIN parking_spaces ps
                    ON pt.space_id = ps.space_id
                WHERE c.vehicle_number = ?
                  AND pt.transaction_status = 'ACTIVE'
                ORDER BY pt.transaction_id DESC
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
                            resultSet.getInt("transaction_id"),
                            resultSet.getInt("space_id"),
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
                WHERE transaction_id = ?
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
                    pt.transaction_id,
                    c.customer_name,
                    c.vehicle_number,
                    c.vehicle_type,
                    ps.space_number,
                    pt.entry_time
                FROM parking_transactions pt
                JOIN customers c
                    ON pt.customer_id = c.customer_id
                JOIN parking_spaces ps
                    ON pt.space_id = ps.space_id
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
                                + resultSet.getInt("transaction_id")
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
                    pt.transaction_id,
                    c.vehicle_number,
                    ps.space_number,
                    pt.entry_time,
                    pt.exit_time,
                    pt.parking_fee,
                    pt.transaction_status
                FROM parking_transactions pt
                JOIN customers c
                    ON pt.customer_id = c.customer_id
                JOIN parking_spaces ps
                    ON pt.space_id = ps.space_id
                ORDER BY pt.transaction_id DESC
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
                                + resultSet.getInt("transaction_id")
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