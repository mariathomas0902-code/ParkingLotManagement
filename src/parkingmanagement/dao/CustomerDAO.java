package parkingmanagement.dao;

import parkingmanagement.DatabaseConnection;
import parkingmanagement.VehicleType;
import parkingmanagement.model.CustomerRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDAO {

    public int addCustomer(
            String customerName,
            String phoneNumber,
            String vehicleNumber,
            String vehicleType
    ) throws SQLException {

        vehicleNumber = vehicleNumber.trim().toUpperCase(Locale.ROOT);
        vehicleType = vehicleType.trim().toUpperCase(Locale.ROOT);

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // Determine next customer_id in case table doesn't have auto_increment
                int nextId = 1;
                try (Statement idStmt = connection.createStatement();
                     ResultSet idRs = idStmt.executeQuery("SELECT COALESCE(MAX(customer_id), 0) + 1 FROM customers")) {
                    if (idRs.next()) {
                        nextId = idRs.getInt(1);
                    }
                }

                int customerId = nextId;

                // Try inserting with explicit customer_id first (compatible with Dump20260929 schema)
                String customerSql = """
                        INSERT INTO customers (customer_id, name, phone_number)
                        VALUES (?, ?, ?)
                        """;

                try (PreparedStatement statement = connection.prepareStatement(customerSql)) {
                    statement.setInt(1, nextId);
                    statement.setString(2, customerName.trim());
                    statement.setString(3, phoneNumber.trim());
                    statement.executeUpdate();
                } catch (SQLException ex) {
                    // Fallback to inserting without explicit ID if auto_increment is strictly configured
                    String autoSql = """
                            INSERT INTO customers (name, phone_number)
                            VALUES (?, ?)
                            """;
                    try (PreparedStatement autoStmt = connection.prepareStatement(autoSql, Statement.RETURN_GENERATED_KEYS)) {
                        autoStmt.setString(1, customerName.trim());
                        autoStmt.setString(2, phoneNumber.trim());
                        autoStmt.executeUpdate();
                        try (ResultSet keys = autoStmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                customerId = keys.getInt(1);
                            }
                        }
                    }
                }

                // Link vehicle
                String vehicleSql = """
                        INSERT INTO vehicles (registration_number, vehicle_type, customer_id)
                        VALUES (?, ?, ?)
                        """;

                try (PreparedStatement statement = connection.prepareStatement(vehicleSql)) {
                    statement.setString(1, vehicleNumber);
                    statement.setString(2, vehicleType);
                    statement.setInt(3, customerId);
                    statement.executeUpdate();
                }

                connection.commit();
                return customerId;

            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        }
    }

    public Integer findCustomerIdByVehicleNumber(String vehicleNumber) throws SQLException {
        String sql = """
                SELECT customer_id
                FROM vehicles
                WHERE registration_number = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, vehicleNumber.trim().toUpperCase(Locale.ROOT));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("customer_id");
                }
            }
        }
        return null;
    }

    public String findCustomerNameByVehicleNumber(String vehicleNumber) throws SQLException {
        String sql = """
                SELECT c.name
                FROM vehicles v
                JOIN customers c ON v.customer_id = c.customer_id
                WHERE v.registration_number = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, vehicleNumber.trim().toUpperCase(Locale.ROOT));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        }
        return "Unknown Customer";
    }

    public List<CustomerRecord> getAllCustomers() throws SQLException {
        List<CustomerRecord> list = new ArrayList<>();
        String sql = """
                SELECT
                    c.customer_id,
                    c.name,
                    c.phone_number,
                    c.email,
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
            while (resultSet.next()) {
                int id = resultSet.getInt("customer_id");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone_number");
                String email = resultSet.getString("email");
                String reg = resultSet.getString("registration_number");
                String vTypeStr = resultSet.getString("vehicle_type");
                VehicleType vType = null;
                if (vTypeStr != null) {
                    try {
                        vType = VehicleType.valueOf(vTypeStr.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                list.add(new CustomerRecord(id, name, phone, email, reg, vType));
            }
        }
        return list;
    }

    public void displayAllCustomers() throws SQLException {
        List<CustomerRecord> list = getAllCustomers();
        System.out.println("\n===== CUSTOMER LIST =====");
        if (list.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        for (CustomerRecord c : list) {
            System.out.println("Customer ID   : " + c.customerId());
            System.out.println("Name          : " + c.name());
            System.out.println("Phone         : " + c.phoneNumber());
            System.out.println("Vehicle Number: " + (c.registrationNumber() == null ? "-" : c.registrationNumber()));
            System.out.println("Vehicle Type  : " + (c.vehicleType() == null ? "-" : c.vehicleType()));
            System.out.println("-------------------------");
        }
    }
}
