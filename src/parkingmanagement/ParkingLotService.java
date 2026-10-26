package parkingmanagement;

import parkingmanagement.dao.CustomerDAO;
import parkingmanagement.dao.InMemoryParkingRepository;
import parkingmanagement.dao.ParkingSpaceDAO;
import parkingmanagement.dao.ParkingTransactionDAO;
import parkingmanagement.dao.ParkingTransactionDAO.ActiveTransaction;
import parkingmanagement.model.ActiveParkingRecord;
import parkingmanagement.model.CustomerRecord;
import parkingmanagement.model.DashboardStats;
import parkingmanagement.model.ExitResult;
import parkingmanagement.model.ParkResult;
import parkingmanagement.model.TransactionRecord;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParkingLotService {

    private final CustomerDAO customerDAO;
    private final ParkingSpaceDAO parkingSpaceDAO;
    private final ParkingTransactionDAO transactionDAO;
    private final InMemoryParkingRepository inMemoryRepo;

    public ParkingLotService() {
        this.customerDAO = new CustomerDAO();
        this.parkingSpaceDAO = new ParkingSpaceDAO();
        this.transactionDAO = new ParkingTransactionDAO();
        this.inMemoryRepo = InMemoryParkingRepository.getInstance();
    }

    public boolean isDatabaseConnected() {
        return DatabaseConnection.isConnected();
    }

    public ParkResult parkVehicle(
            String customerName,
            String phoneNumber,
            String vehicleNumber,
            VehicleType vehicleType
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return ParkResult.failure("Customer name cannot be empty.");
        }
        if (phoneNumber == null || !phoneNumber.trim().matches("\\d{10}")) {
            return ParkResult.failure("Enter a valid 10-digit phone number.");
        }
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return ParkResult.failure("Vehicle number cannot be empty.");
        }
        if (vehicleType == null) {
            return ParkResult.failure("Please select a vehicle type.");
        }

        customerName = customerName.trim();
        phoneNumber = phoneNumber.trim();
        vehicleNumber = vehicleNumber.trim().toUpperCase(Locale.ROOT);

        if (!isDatabaseConnected()) {
            ParkResult result = inMemoryRepo.parkVehicle(customerName, phoneNumber, vehicleNumber, vehicleType);
            System.out.println(result.message());
            return result;
        }

        try {
            ActiveTransaction existingTransaction = transactionDAO.findActiveTransaction(vehicleNumber);
            if (existingTransaction != null) {
                ParkResult result = ParkResult.failure("Vehicle is already parked in Slot " + existingTransaction.spaceId() + ".");
                System.out.println(result.message());
                return result;
            }

            Integer spaceId = parkingSpaceDAO.findAvailableSpace(vehicleType.name());
            if (spaceId == null) {
                ParkResult result = ParkResult.failure("No available space for " + vehicleType + ".");
                System.out.println(result.message());
                return result;
            }

            Integer customerId = customerDAO.findCustomerIdByVehicleNumber(vehicleNumber);
            if (customerId == null) {
                customerDAO.addCustomer(customerName, phoneNumber, vehicleNumber, vehicleType.name());
            }

            int ticketId = transactionDAO.createTransaction(vehicleNumber, spaceId);
            parkingSpaceDAO.updateSpaceStatus(spaceId, "OCCUPIED");

            LocalDateTime entryTime = LocalDateTime.now();
            ParkResult result = ParkResult.success(ticketId, spaceId, vehicleNumber, vehicleType, customerName, entryTime);

            System.out.println("\nVehicle parked successfully.");
            System.out.println("Transaction / Ticket ID : " + ticketId);
            System.out.println("Vehicle Number          : " + vehicleNumber);
            System.out.println("Vehicle Type            : " + vehicleType);
            System.out.println("Parking Space ID        : " + spaceId);

            return result;

        } catch (SQLException exception) {
            System.out.println("Could not park the vehicle: " + exception.getMessage());
            return ParkResult.failure("Database error: " + exception.getMessage());
        }
    }

    public ExitResult previewExit(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return ExitResult.failure("Vehicle number cannot be empty.");
        }
        vehicleNumber = vehicleNumber.trim().toUpperCase(Locale.ROOT);

        if (!isDatabaseConnected()) {
            for (TransactionRecord t : inMemoryRepo.getAllTransactions()) {
                if ("ACTIVE".equalsIgnoreCase(t.status()) && t.registrationNumber().equalsIgnoreCase(vehicleNumber)) {
                    LocalDateTime exitTime = LocalDateTime.now();
                    double fee = FeeCalculator.calculateFee(t.vehicleType(), t.entryTime(), exitTime);
                    String duration = FeeCalculator.formatDuration(t.entryTime(), exitTime);
                    return ExitResult.success(t.ticketId(), t.slotId(), vehicleNumber, t.vehicleType(), t.customerName(), t.entryTime(), exitTime, fee, duration);
                }
            }
            return ExitResult.failure("No active parking record found for vehicle " + vehicleNumber + ".");
        }

        try {
            ActiveTransaction transaction = transactionDAO.findActiveTransaction(vehicleNumber);
            if (transaction == null) {
                return ExitResult.failure("No active parking record found for vehicle " + vehicleNumber + ".");
            }

            String customerName = customerDAO.findCustomerNameByVehicleNumber(vehicleNumber);
            VehicleType vehicleType = VehicleType.valueOf(transaction.vehicleType().toUpperCase(Locale.ROOT));
            LocalDateTime exitTime = LocalDateTime.now();
            double fee = FeeCalculator.calculateFee(vehicleType, transaction.entryTime(), exitTime);
            String duration = FeeCalculator.formatDuration(transaction.entryTime(), exitTime);

            return ExitResult.success(transaction.transactionId(), transaction.spaceId(), vehicleNumber, vehicleType, customerName, transaction.entryTime(), exitTime, fee, duration);

        } catch (SQLException exception) {
            return ExitResult.failure("Database error: " + exception.getMessage());
        }
    }

    public ExitResult exitVehicle(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return ExitResult.failure("Vehicle number cannot be empty.");
        }
        vehicleNumber = vehicleNumber.trim().toUpperCase(Locale.ROOT);

        if (!isDatabaseConnected()) {
            ExitResult result = inMemoryRepo.exitVehicle(vehicleNumber);
            System.out.println(result.message());
            return result;
        }

        try {
            ActiveTransaction transaction = transactionDAO.findActiveTransaction(vehicleNumber);
            if (transaction == null) {
                ExitResult result = ExitResult.failure("No active parking record found for vehicle " + vehicleNumber + ".");
                System.out.println(result.message());
                return result;
            }

            LocalDateTime exitTime = LocalDateTime.now();
            VehicleType vehicleType = VehicleType.valueOf(transaction.vehicleType().toUpperCase(Locale.ROOT));
            double fee = FeeCalculator.calculateFee(vehicleType, transaction.entryTime(), exitTime);
            String duration = FeeCalculator.formatDuration(transaction.entryTime(), exitTime);

            boolean completed = transactionDAO.completeTransaction(transaction.transactionId(), fee);
            if (!completed) {
                return ExitResult.failure("Vehicle exit could not be completed.");
            }

            parkingSpaceDAO.updateSpaceStatus(transaction.spaceId(), "AVAILABLE");
            String customerName = customerDAO.findCustomerNameByVehicleNumber(vehicleNumber);

            ExitResult result = ExitResult.success(
                    transaction.transactionId(),
                    transaction.spaceId(),
                    vehicleNumber,
                    vehicleType,
                    customerName,
                    transaction.entryTime(),
                    exitTime,
                    fee,
                    duration
            );

            System.out.println("\nVehicle exit completed successfully.");
            System.out.println("Ticket ID     : " + transaction.transactionId());
            System.out.println("Vehicle Number: " + vehicleNumber);
            System.out.println("Parking Space : " + transaction.spaceNumber());
            System.out.println("Entry Time    : " + transaction.entryTime());
            System.out.println("Exit Time     : " + exitTime);
            System.out.printf("Parking Fee   : Rs. %.2f%n", fee);

            return result;

        } catch (SQLException exception) {
            System.out.println("Could not complete vehicle exit: " + exception.getMessage());
            return ExitResult.failure("Database error: " + exception.getMessage());
        }
    }

    public List<ParkingSpace> getAllSpaces() {
        if (!isDatabaseConnected()) {
            return inMemoryRepo.getAllSpaces();
        }
        try {
            return parkingSpaceDAO.getAllSpaces();
        } catch (SQLException e) {
            return inMemoryRepo.getAllSpaces();
        }
    }

    public List<ActiveParkingRecord> getParkedVehicles() {
        if (!isDatabaseConnected()) {
            return inMemoryRepo.getActiveTransactions();
        }
        try {
            return transactionDAO.getActiveTransactions();
        } catch (SQLException e) {
            return inMemoryRepo.getActiveTransactions();
        }
    }

    public List<TransactionRecord> getTransactionHistory() {
        if (!isDatabaseConnected()) {
            return inMemoryRepo.getAllTransactions();
        }
        try {
            return transactionDAO.getAllTransactions();
        } catch (SQLException e) {
            return inMemoryRepo.getAllTransactions();
        }
    }

    public List<CustomerRecord> getAllCustomers() {
        if (!isDatabaseConnected()) {
            return inMemoryRepo.getAllCustomers();
        }
        try {
            return customerDAO.getAllCustomers();
        } catch (SQLException e) {
            return inMemoryRepo.getAllCustomers();
        }
    }

    public DashboardStats getDashboardStats() {
        if (!isDatabaseConnected()) {
            return inMemoryRepo.getDashboardStats();
        }
        try {
            int total = parkingSpaceDAO.countTotalSpaces();
            int available = parkingSpaceDAO.countAvailableSpaces();
            int occupied = parkingSpaceDAO.countOccupiedSpaces();
            int active = transactionDAO.countActiveTransactions();
            int completed = transactionDAO.countCompletedTransactions();
            double revenue = transactionDAO.getTotalRevenue();
            Map<VehicleType, Integer> availByType = parkingSpaceDAO.getAvailableSpacesCountByType();
            Map<VehicleType, Integer> totalByType = parkingSpaceDAO.getTotalSpacesCountByType();

            return new DashboardStats(
                    total, available, occupied, active, completed, revenue, availByType, totalByType
            );
        } catch (SQLException e) {
            return inMemoryRepo.getDashboardStats();
        }
    }

    public void showParkedVehicles() {
        if (!isDatabaseConnected()) {
            List<ActiveParkingRecord> list = inMemoryRepo.getActiveTransactions();
            System.out.println("\n===== CURRENTLY PARKED VEHICLES (Demo Mode) =====");
            for (ActiveParkingRecord r : list) {
                System.out.println("Ticket ID: " + r.ticketId() + " | " + r.registrationNumber() + " | Slot: " + r.slotId());
            }
            return;
        }
        try {
            transactionDAO.displayActiveTransactions();
        } catch (SQLException exception) {
            System.out.println("Could not display parked vehicles: " + exception.getMessage());
        }
    }

    public void showTransactionHistory() {
        if (!isDatabaseConnected()) {
            List<TransactionRecord> list = inMemoryRepo.getAllTransactions();
            System.out.println("\n===== TRANSACTION HISTORY (Demo Mode) =====");
            for (TransactionRecord r : list) {
                System.out.println("Ticket ID: " + r.ticketId() + " | " + r.registrationNumber() + " | Fee: Rs." + r.parkingFee() + " | " + r.status());
            }
            return;
        }
        try {
            transactionDAO.displayTransactionHistory();
        } catch (SQLException exception) {
            System.out.println("Could not display transaction history: " + exception.getMessage());
        }
    }

    public void showAvailableSpaces() {
        if (!isDatabaseConnected()) {
            List<ParkingSpace> list = inMemoryRepo.getAllSpaces();
            System.out.println("\n===== AVAILABLE PARKING SPACES (Demo Mode) =====");
            for (ParkingSpace s : list) {
                if (s.isAvailable()) {
                    System.out.println("Slot ID: " + s.getSlotId() + " | " + s.getSupportedType() + " | AVAILABLE");
                }
            }
            return;
        }
        try {
            parkingSpaceDAO.displayAvailableSpaces();
        } catch (SQLException exception) {
            System.out.println("Could not display available spaces: " + exception.getMessage());
        }
    }
}
