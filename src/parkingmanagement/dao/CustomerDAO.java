package parkingmanagement.dao;

import parkingmanagement.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class CustomerDAO {

    // Save the customer and vehicle together.
    // Return the generated customer ID.
    public int addCustomer(
            String customerName,
            String phoneNumber,
            String vehicleNumber,
            String vehicleType
    ) throws SQLException {

        String customerSql = """
                INSERT INTO customers (name, phone_number)
                VALUES (?, ?)
                """;

        String vehicleSql = """
                INSERT INTO vehicles
                    (registration_number, vehicle_type, customer_id)
                VALUES (?, ?, ?)
                """;

        vehicleNumber = vehicleNumber.trim().toUpperCase(Locale.ROOT);
        vehicleType = vehicleType.trim().toUpperCase(Locale.ROOT);

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Both inserts must succeed before saving.
            connection.setAutoCommit(false);

            try {
                int customerId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     customerSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setString(1, customerName.trim());
                    statement.setString(2, phoneNumber.trim());

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Customer could not be inserted."
                        );
                    }

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Customer ID was not generated. "
                                            + "Check customer_id AUTO_INCREMENT."
                            );
                        }

                        customerId = keys.getInt(1);
                    }
                }

                // Link the vehicle to the newly created customer.
                try (PreparedStatement statement =
                             connection.prepareStatement(vehicleSql)) {

                    statement.setString(1, vehicleNumber);
                    statement.setString(2, vehicleType);
                    statement.setInt(3, customerId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Vehicle could not be inserted."
                        );
                    }
                }

                connection.commit();
                return customerId;

            } catch (SQLException exception) {
                // Undo the customer insert if vehicle insertion fails.
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }

    // Find the customer linked to a vehicle.
    public Integer findCustomerIdByVehicleNumber(
            String vehicleNumber
    ) throws SQLException {

        String sql = """
                SELECT customer_id
                FROM vehicles
                WHERE registration_number = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    vehicleNumber.trim().toUpperCase(Locale.ROOT)
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("customer_id");
                }
            }
        }

        return null;
    }

    // Display customers and their linked vehicles.
    public void displayAllCustomers() throws SQLException {

        String sql = """
                SELECT
                    c.customer_id,
                    c.name,
                    c.phone_number,
                    v.registration_number,
                    v.vehicle_type
                FROM customers c
                LEFT JOIN vehicles v
                    ON c.customer_id = v.customer_id
                ORDER BY c.customer_id, v.registration_number
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            System.out.println("\n===== CUSTOMER LIST =====");

            boolean customerFound = false;

            while (resultSet.next()) {
                customerFound = true;

                String vehicleNumber =
                        resultSet.getString("registration_number");

                String vehicleType =
                        resultSet.getString("vehicle_type");

                System.out.println(
                        "Customer ID   : "
                                + resultSet.getInt("customer_id")
                );

                System.out.println(
                        "Name          : "
                                + resultSet.getString("name")
                );

                System.out.println(
                        "Phone         : "
                                + resultSet.getString("phone_number")
                );

                System.out.println(
                        "Vehicle Number: "
                                + (vehicleNumber == null ? "-" : vehicleNumber)
                );

                System.out.println(
                        "Vehicle Type  : "
                                + (vehicleType == null ? "-" : vehicleType)
                );

                System.out.println("-------------------------");
            }

            if (!customerFound) {
                System.out.println("No customers found.");
            }
        }
    }
}