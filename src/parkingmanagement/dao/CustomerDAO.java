package parkingmanagement.dao;

import parkingmanagement.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerDAO {

    // Add a customer and return the generated customer ID
    public int addCustomer(
            String customerName,
            String phoneNumber,
            String vehicleNumber,
            String vehicleType
    ) throws SQLException {

        String sql = """
                INSERT INTO customers
                (customer_name, phone_number, vehicle_number, vehicle_type)
                VALUES (?, ?, ?, ?)
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
            statement.setString(1, customerName);
            statement.setString(2, phoneNumber);
            statement.setString(3, vehicleNumber);
            statement.setString(4, vehicleType);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Customer could not be inserted."
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
                "Customer ID was not generated."
        );
    }

    // Find customer ID using vehicle number
    public Integer findCustomerIdByVehicleNumber(
            String vehicleNumber
    ) throws SQLException {

        String sql = """
                SELECT customer_id
                FROM customers
                WHERE vehicle_number = ?
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
                    return resultSet.getInt("customer_id");
                }
            }
        }

        return null;
    }

    // Display all customers
    public void displayAllCustomers() throws SQLException {

        String sql = """
                SELECT customer_id,
                       customer_name,
                       phone_number,
                       vehicle_number,
                       vehicle_type
                FROM customers
                ORDER BY customer_id
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
                    "\n===== CUSTOMER LIST ====="
            );

            boolean customerFound = false;

            while (resultSet.next()) {
                customerFound = true;

                System.out.println(
                        "Customer ID   : "
                                + resultSet.getInt("customer_id")
                );

                System.out.println(
                        "Name          : "
                                + resultSet.getString("customer_name")
                );

                System.out.println(
                        "Phone         : "
                                + resultSet.getString("phone_number")
                );

                System.out.println(
                        "Vehicle Number: "
                                + resultSet.getString("vehicle_number")
                );

                System.out.println(
                        "Vehicle Type  : "
                                + resultSet.getString("vehicle_type")
                );

                System.out.println("-------------------------");
            }

            if (!customerFound) {
                System.out.println("No customers found.");
            }
        }
    }
}
