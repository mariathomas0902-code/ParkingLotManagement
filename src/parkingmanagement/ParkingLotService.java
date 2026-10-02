package parkingmanagement;

import parkingmanagement.dao.CustomerDAO;
import parkingmanagement.dao.ParkingSpaceDAO;
import parkingmanagement.dao.ParkingTransactionDAO;
import parkingmanagement.dao.ParkingTransactionDAO.ActiveTransaction;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ParkingLotService {

    private final CustomerDAO customerDAO;
    private final ParkingSpaceDAO parkingSpaceDAO;
    private final ParkingTransactionDAO transactionDAO;

    public ParkingLotService() {
        customerDAO = new CustomerDAO();
        parkingSpaceDAO = new ParkingSpaceDAO();
        transactionDAO = new ParkingTransactionDAO();
    }

    /*
     * Parks a vehicle.
     */
    public void parkVehicle(
            String customerName,
            String phoneNumber,
            String vehicleNumber,
            VehicleType vehicleType
    ) {
        try {
            vehicleNumber =
                    vehicleNumber.trim().toUpperCase();

            // Check whether this vehicle is already parked
            ActiveTransaction existingTransaction =
                    transactionDAO.findActiveTransaction(
                            vehicleNumber
                    );

            if (existingTransaction != null) {
                System.out.println(
                        "This vehicle is already parked."
                );
                return;
            }

            // Find an available parking space
            Integer spaceId =
                    parkingSpaceDAO.findAvailableSpace(
                            vehicleType.name()
                    );

            if (spaceId == null) {
                System.out.println(
                        "No available space for "
                                + vehicleType
                                + "."
                );
                return;
            }

            // Check whether customer already exists
            Integer customerId =
                    customerDAO.findCustomerIdByVehicleNumber(
                            vehicleNumber
                    );

            // Add the customer if not already present
            if (customerId == null) {
                customerId = customerDAO.addCustomer(
                        customerName,
                        phoneNumber,
                        vehicleNumber,
                        vehicleType.name()
                );
            }

            // Create parking transaction
            // Create parking transaction using the vehicle number
            int transactionId =
                    transactionDAO.createTransaction(
                            vehicleNumber,
                            spaceId
                    );

            // Mark the space as occupied
            parkingSpaceDAO.updateSpaceStatus(
                    spaceId,
                    "OCCUPIED"
            );

            System.out.println(
                    "\nVehicle parked successfully."
            );

            System.out.println(
                    "Transaction ID : " + transactionId
            );

            System.out.println(
                    "Vehicle Number : " + vehicleNumber
            );

            System.out.println(
                    "Vehicle Type   : " + vehicleType
            );

            System.out.println(
                    "Parking Space ID: " + spaceId
            );

        } catch (SQLException exception) {
            System.out.println(
                    "Could not park the vehicle."
            );

            System.out.println(
                    "Reason: " + exception.getMessage()
            );
        }
    }

    /*
     * Processes vehicle exit and calculates the fee.
     */
    public void exitVehicle(String vehicleNumber) {
        try {
            vehicleNumber =
                    vehicleNumber.trim().toUpperCase();

            ActiveTransaction transaction =
                    transactionDAO.findActiveTransaction(
                            vehicleNumber
                    );

            if (transaction == null) {
                System.out.println(
                        "No active parking record found "
                                + "for vehicle "
                                + vehicleNumber
                );
                return;
            }

            LocalDateTime exitTime =
                    LocalDateTime.now();

            VehicleType vehicleType =
                    VehicleType.valueOf(
                            transaction.vehicleType()
                                    .toUpperCase()
                    );

            double fee =
                    FeeCalculator.calculateFee(
                            vehicleType,
                            transaction.entryTime(),
                            exitTime
                    );

            boolean completed =
                    transactionDAO.completeTransaction(
                            transaction.transactionId(),
                            fee
                    );

            if (!completed) {
                System.out.println(
                        "Vehicle exit could not be completed."
                );
                return;
            }

            parkingSpaceDAO.updateSpaceStatus(
                    transaction.spaceId(),
                    "AVAILABLE"
            );

            System.out.println(
                    "\nVehicle exit completed successfully."
            );

            System.out.println(
                    "Vehicle Number : " + vehicleNumber
            );

            System.out.println(
                    "Parking Space  : "
                            + transaction.spaceNumber()
            );

            System.out.println(
                    "Entry Time     : "
                            + transaction.entryTime()
            );

            System.out.println(
                    "Exit Time      : " + exitTime
            );

            System.out.printf(
                    "Parking Fee    : Rs. %.2f%n",
                    fee
            );

        } catch (IllegalArgumentException exception) {
            System.out.println(
                    "Invalid vehicle type in database."
            );

        } catch (SQLException exception) {
            System.out.println(
                    "Could not complete vehicle exit."
            );

            System.out.println(
                    "Reason: " + exception.getMessage()
            );
        }
    }

    /*
     * Displays currently parked vehicles.
     */
    public void showParkedVehicles() {
        try {
            transactionDAO.displayActiveTransactions();

        } catch (SQLException exception) {
            System.out.println(
                    "Could not display parked vehicles."
            );

            System.out.println(
                    "Reason: " + exception.getMessage()
            );
        }
    }

    /*
     * Displays all parking transaction history.
     */
    public void showTransactionHistory() {
        try {
            transactionDAO.displayTransactionHistory();

        } catch (SQLException exception) {
            System.out.println(
                    "Could not display transaction history."
            );

            System.out.println(
                    "Reason: " + exception.getMessage()
            );
        }
    }

    public void showAvailableSpaces() {
        try {
            parkingSpaceDAO.displayAvailableSpaces();

        } catch (SQLException exception) {
            System.out.println(
                    "Could not display available spaces."
            );

            System.out.println(
                    "Reason: " + exception.getMessage()
            );
        }
    }
}