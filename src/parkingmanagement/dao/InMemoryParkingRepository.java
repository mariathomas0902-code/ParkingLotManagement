package parkingmanagement.dao;

import parkingmanagement.FeeCalculator;
import parkingmanagement.ParkingSpace;
import parkingmanagement.VehicleType;
import parkingmanagement.model.ActiveParkingRecord;
import parkingmanagement.model.CustomerRecord;
import parkingmanagement.model.DashboardStats;
import parkingmanagement.model.ExitResult;
import parkingmanagement.model.ParkResult;
import parkingmanagement.model.TransactionRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stores parking information in memory.
 *
 * Data is lost when the application closes.
 * Database fallback selection must be handled by the service layer.
 */
public class InMemoryParkingRepository {

    // Change to true only when sample data is required.
    private static final boolean LOAD_DEMO_DATA = false;

    private static final InMemoryParkingRepository INSTANCE =
            new InMemoryParkingRepository();

    private final Map<Integer, ParkingSpace> spaces =
            new LinkedHashMap<>();

    private final Map<String, CustomerRecord> vehiclesToCustomer =
            new LinkedHashMap<>();

    // Keeps customer IDs stable when contact details are updated.
    private final Map<String, Integer> customerIdsByVehicle =
            new LinkedHashMap<>();

    private final List<TransactionRecord> transactions =
            new ArrayList<>();

    private final AtomicInteger ticketSequence =
            new AtomicInteger(1001);

    private final AtomicInteger customerSequence =
            new AtomicInteger(1);

    private InMemoryParkingRepository() {
        initializeSpaces();

        if (LOAD_DEMO_DATA) {
            loadDemoData();
        }
    }

    public static InMemoryParkingRepository getInstance() {
        return INSTANCE;
    }

    private void initializeSpaces() {
        spaces.put(101, new ParkingSpace(101, VehicleType.BIKE));
        spaces.put(102, new ParkingSpace(102, VehicleType.BIKE));

        spaces.put(201, new ParkingSpace(201, VehicleType.CAR));
        spaces.put(202, new ParkingSpace(202, VehicleType.CAR));

        spaces.put(301, new ParkingSpace(301, VehicleType.TRUCK));
    }

    private void loadDemoData() {
        LocalDateTime now = LocalDateTime.now();

        saveCustomer(
                "Test Customer",
                "9876543210",
                "KL05AB1234",
                VehicleType.CAR
        );

        saveCustomer(
                "Rahul Sharma",
                "9876543211",
                "KL07CD5678",
                VehicleType.BIKE
        );

        ParkingSpace carSlot = spaces.get(201);
        carSlot.occupy();

        transactions.add(new TransactionRecord(
                1000,
                "KL05AB1234",
                VehicleType.CAR,
                201,
                "Test Customer",
                now.minusHours(2).minusMinutes(15),
                null,
                0.0,
                "ACTIVE"
        ));

        LocalDateTime historicalEntry = now.minusHours(5);
        LocalDateTime historicalExit = now.minusHours(3);

        double historicalFee = FeeCalculator.calculateFee(
                VehicleType.BIKE,
                historicalEntry,
                historicalExit
        );

        transactions.add(new TransactionRecord(
                999,
                "KL07CD5678",
                VehicleType.BIKE,
                101,
                "Rahul Sharma",
                historicalEntry,
                historicalExit,
                historicalFee,
                "COMPLETED"
        ));
    }

    /**
     * Creates or updates the customer associated with a vehicle.
     * Called only during initialization or a synchronized operation.
     *
     * This repository does not collect email addresses.
     */
    private void saveCustomer(
            String customerName,
            String phoneNumber,
            String vehicleNumber,
            VehicleType vehicleType
    ) {
        Integer customerId = customerIdsByVehicle.get(vehicleNumber);

        if (customerId == null) {
            customerId = customerSequence.getAndIncrement();
            customerIdsByVehicle.put(vehicleNumber, customerId);
        }

        vehiclesToCustomer.put(
                vehicleNumber,
                new CustomerRecord(
                        customerId,
                        customerName,
                        phoneNumber,
                        null,
                        vehicleNumber,
                        vehicleType
                )
        );
    }

    private static String normalizeVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null) {
            return "";
        }

        return vehicleNumber
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "");
    }

    public synchronized ParkResult parkVehicle(
            String customerName,
            String phoneNumber,
            String vehicleNumber,
            VehicleType vehicleType
    ) {
        if (customerName == null || customerName.isBlank()) {
            return ParkResult.failure("Please enter the customer name.");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ParkResult.failure("Please enter the phone number.");
        }

        if (vehicleType == null) {
            return ParkResult.failure("Please select a vehicle type.");
        }

        customerName = customerName.trim();
        phoneNumber = phoneNumber.trim();
        vehicleNumber = normalizeVehicleNumber(vehicleNumber);

        if (vehicleNumber.isEmpty()) {
            return ParkResult.failure("Please enter the vehicle number.");
        }

        if (!vehicleNumber.matches("[A-Z0-9]+")) {
            return ParkResult.failure(
                    "Vehicle number must contain only letters and numbers."
            );
        }

        // Assumes an Indian 10-digit phone number without country code.
        if (!phoneNumber.matches("[0-9]{10}")) {
            return ParkResult.failure(
                    "Please enter a valid 10-digit phone number."
            );
        }

        for (TransactionRecord transaction : transactions) {
            if ("ACTIVE".equalsIgnoreCase(transaction.status())
                    && transaction.registrationNumber()
                    .equalsIgnoreCase(vehicleNumber)) {

                return ParkResult.failure(
                        "This vehicle is already parked in Slot "
                                + transaction.slotId() + "."
                );
            }
        }

        ParkingSpace targetSpace = null;

        for (ParkingSpace space : spaces.values()) {
            if (space.getSupportedType() == vehicleType
                    && space.isAvailable()) {
                targetSpace = space;
                break;
            }
        }

        if (targetSpace == null) {
            return ParkResult.failure(
                    "No available space for " + vehicleType + "."
            );
        }

        int ticketId = ticketSequence.getAndIncrement();
        LocalDateTime entryTime = LocalDateTime.now();

        TransactionRecord transaction = new TransactionRecord(
                ticketId,
                vehicleNumber,
                vehicleType,
                targetSpace.getSlotId(),
                customerName,
                entryTime,
                null,
                0.0,
                "ACTIVE"
        );

        saveCustomer(
                customerName,
                phoneNumber,
                vehicleNumber,
                vehicleType
        );

        targetSpace.occupy();
        transactions.add(transaction);

        return ParkResult.success(
                ticketId,
                targetSpace.getSlotId(),
                vehicleNumber,
                vehicleType,
                customerName,
                entryTime
        );
    }

    public synchronized ExitResult exitVehicle(String vehicleNumber) {
        vehicleNumber = normalizeVehicleNumber(vehicleNumber);

        if (vehicleNumber.isEmpty()) {
            return ExitResult.failure("Please enter the vehicle number.");
        }

        TransactionRecord activeRecord = null;
        int activeIndex = -1;

        for (int i = 0; i < transactions.size(); i++) {
            TransactionRecord transaction = transactions.get(i);

            if ("ACTIVE".equalsIgnoreCase(transaction.status())
                    && transaction.registrationNumber()
                    .equalsIgnoreCase(vehicleNumber)) {
                activeRecord = transaction;
                activeIndex = i;
                break;
            }
        }

        if (activeRecord == null) {
            return ExitResult.failure(
                    "No active parking record found for vehicle "
                            + vehicleNumber + "."
            );
        }

        ParkingSpace space = spaces.get(activeRecord.slotId());

        if (space == null || space.isAvailable()) {
            return ExitResult.failure(
                    "Parking slot information is inconsistent. "
                            + "Please check Slot "
                            + activeRecord.slotId() + "."
            );
        }

        LocalDateTime exitTime = LocalDateTime.now();

        if (exitTime.isBefore(activeRecord.entryTime())) {
            return ExitResult.failure(
                    "Exit time is earlier than entry time. "
                            + "Please check the computer clock."
            );
        }

        double fee = FeeCalculator.calculateFee(
                activeRecord.vehicleType(),
                activeRecord.entryTime(),
                exitTime
        );

        if (!Double.isFinite(fee) || fee < 0) {
            return ExitResult.failure(
                    "The calculated parking fee is invalid."
            );
        }

        String durationText = FeeCalculator.formatDuration(
                activeRecord.entryTime(),
                exitTime
        );

        TransactionRecord completedRecord = new TransactionRecord(
                activeRecord.ticketId(),
                activeRecord.registrationNumber(),
                activeRecord.vehicleType(),
                activeRecord.slotId(),
                activeRecord.customerName(),
                activeRecord.entryTime(),
                exitTime,
                fee,
                "COMPLETED"
        );

        space.release();
        transactions.set(activeIndex, completedRecord);

        return ExitResult.success(
                activeRecord.ticketId(),
                activeRecord.slotId(),
                activeRecord.registrationNumber(),
                activeRecord.vehicleType(),
                activeRecord.customerName(),
                activeRecord.entryTime(),
                exitTime,
                fee,
                durationText
        );
    }

    public synchronized List<ParkingSpace> getAllSpaces() {
        List<ParkingSpace> result = new ArrayList<>();

        for (ParkingSpace space : spaces.values()) {
            ParkingSpace copy = new ParkingSpace(
                    space.getSlotId(),
                    space.getSupportedType()
            );

            if (!space.isAvailable()) {
                copy.occupy();
            }

            result.add(copy);
        }

        return result;
    }

    public synchronized List<ActiveParkingRecord> getActiveTransactions() {
        List<ActiveParkingRecord> result = new ArrayList<>();

        for (TransactionRecord transaction : transactions) {
            if (!"ACTIVE".equalsIgnoreCase(transaction.status())) {
                continue;
            }

            CustomerRecord customer = vehiclesToCustomer.get(
                    transaction.registrationNumber()
            );

            String phoneNumber = "-";

            if (customer != null
                    && customer.phoneNumber() != null
                    && !customer.phoneNumber().isBlank()) {
                phoneNumber = customer.phoneNumber();
            }

            result.add(new ActiveParkingRecord(
                    transaction.ticketId(),
                    transaction.registrationNumber(),
                    transaction.vehicleType(),
                    transaction.slotId(),
                    transaction.customerName(),
                    phoneNumber,
                    transaction.entryTime()
            ));
        }

        result.sort(
                Comparator.comparing(ActiveParkingRecord::entryTime)
                        .reversed()
        );

        return result;
    }

    public synchronized List<TransactionRecord> getAllTransactions() {
        List<TransactionRecord> result =
                new ArrayList<>(transactions);

        result.sort(
                Comparator.comparing(TransactionRecord::entryTime)
                        .reversed()
        );

        return result;
    }

    public synchronized List<CustomerRecord> getAllCustomers() {
        return new ArrayList<>(vehiclesToCustomer.values());
    }

    public synchronized DashboardStats getDashboardStats() {
        int total = spaces.size();
        int available = 0;
        int occupied = 0;
        int active = 0;
        int completed = 0;
        double revenue = 0.0;

        Map<VehicleType, Integer> availableByType =
                new EnumMap<>(VehicleType.class);

        Map<VehicleType, Integer> totalByType =
                new EnumMap<>(VehicleType.class);

        for (VehicleType type : VehicleType.values()) {
            availableByType.put(type, 0);
            totalByType.put(type, 0);
        }

        for (ParkingSpace space : spaces.values()) {
            VehicleType type = space.getSupportedType();

            totalByType.put(type, totalByType.get(type) + 1);

            if (space.isAvailable()) {
                available++;
                availableByType.put(
                        type,
                        availableByType.get(type) + 1
                );
            } else {
                occupied++;
            }
        }

        for (TransactionRecord transaction : transactions) {
            if ("ACTIVE".equalsIgnoreCase(transaction.status())) {
                active++;
            } else if ("COMPLETED".equalsIgnoreCase(transaction.status())) {
                completed++;
                revenue += transaction.parkingFee();
            }
        }

        return new DashboardStats(
                total,
                available,
                occupied,
                active,
                completed,
                revenue,
                availableByType,
                totalByType
        );
    }
}