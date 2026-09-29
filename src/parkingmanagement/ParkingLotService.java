package parkingmanagement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ParkingLotService {

    private final ArrayList<ParkingSpace> parkingSpaces;
    private final ArrayList<Customer> customers;
    private final ArrayList<Vehicle> vehicles;
    private final ArrayList<ParkingTransaction> transactions;

    private int nextTicketId;

    public ParkingLotService() {
        parkingSpaces = new ArrayList<>();
        customers = new ArrayList<>();
        vehicles = new ArrayList<>();
        transactions = new ArrayList<>();

        nextTicketId = 1001;
    }

    public void addParkingSpace(
            int slotId,
            VehicleType vehicleType) {

        if (findParkingSpace(slotId) != null) {
            System.out.println(
                    "A parking space with this ID already exists."
            );
            return;
        }

        ParkingSpace parkingSpace =
                new ParkingSpace(slotId, vehicleType);

        parkingSpaces.add(parkingSpace);

        System.out.println(
                "Parking space added successfully."
        );
    }

    public void registerCustomer(
            int customerId,
            String name,
            String phone,
            String email) {

        if (findCustomer(customerId) != null) {
            System.out.println(
                    "A customer with this ID already exists."
            );
            return;
        }

        Customer customer =
                new Customer(
                        customerId,
                        name,
                        phone,
                        email
                );

        customers.add(customer);

        System.out.println(
                "Customer registered successfully."
        );
    }

    public void registerVehicle(
            String registrationNumber,
            VehicleType vehicleType,
            int customerId) {

        registrationNumber =
                registrationNumber.toUpperCase();

        if (findVehicle(registrationNumber) != null) {
            System.out.println(
                    "This vehicle is already registered."
            );
            return;
        }

        Customer customer = findCustomer(customerId);

        if (customer == null) {
            System.out.println(
                    "Customer not found. Register the customer first."
            );
            return;
        }

        Vehicle vehicle =
                new Vehicle(
                        registrationNumber,
                        vehicleType,
                        customer
                );

        vehicles.add(vehicle);

        System.out.println(
                "Vehicle registered successfully."
        );
    }

    public void vehicleEntry(
            String registrationNumber) {

        Vehicle vehicle =
                findVehicle(registrationNumber);

        if (vehicle == null) {
            System.out.println(
                    "Vehicle not found. Register it first."
            );
            return;
        }

        if (findActiveTransaction(
                vehicle.getRegistrationNumber()) != null) {

            System.out.println(
                    "This vehicle is already inside the parking lot."
            );
            return;
        }

        ParkingSpace availableSpace =
                findAvailableParkingSpace(
                        vehicle.getVehicleType()
                );

        if (availableSpace == null) {
            System.out.println(
                    "No suitable parking space is available."
            );
            return;
        }

        availableSpace.occupy();

        ParkingTransaction transaction =
                new ParkingTransaction(
                        nextTicketId,
                        vehicle,
                        availableSpace
                );

        transactions.add(transaction);
        nextTicketId++;

        System.out.println("\nVehicle entry successful.");
        System.out.println("Parking ticket:");
        System.out.println(transaction);

    }

    public void vehicleExit(String registrationNumber) {

        ParkingTransaction transaction =
                findActiveTransaction(registrationNumber);

        if (transaction == null) {
            System.out.println(
                    "No active transaction found for this vehicle."
            );
            return;
        }

        LocalDateTime exitTime = LocalDateTime.now();

        long parkedMinutes =
                Duration.between(
                        transaction.getEntryTime(),
                        exitTime
                ).toMinutes();

        long chargedHours =
                (long) Math.ceil(parkedMinutes / 60.0);

        if (chargedHours < 1) {
            chargedHours = 1;
        }

        double fee =
                FeeCalculator.calculateFee(
                        transaction
                                .getVehicle()
                                .getVehicleType(),
                        chargedHours
                );

        transaction.closeTransaction(exitTime, fee);
        transaction.getParkingSpace().release();

        System.out.println("\nVehicle exit successful.");
        System.out.println("Payment receipt:");
        System.out.println(transaction);
        System.out.println("Charged Hours: " + chargedHours);
    }

    public ParkingSpace findParkingSpace(int slotId) {

        for (ParkingSpace parkingSpace : parkingSpaces) {

            if (parkingSpace.getSlotId() == slotId) {
                return parkingSpace;
            }
        }

        return null;
    }

    public Customer findCustomer(int customerId) {

        for (Customer customer : customers) {

            if (customer.getCustomerId() == customerId) {
                return customer;
            }
        }

        return null;
    }

    public Vehicle findVehicle(
            String registrationNumber) {

        for (Vehicle vehicle : vehicles) {

            if (vehicle.getRegistrationNumber()
                    .equalsIgnoreCase(registrationNumber)) {

                return vehicle;
            }
        }

        return null;
    }

    private ParkingSpace findAvailableParkingSpace(
            VehicleType vehicleType) {

        for (ParkingSpace parkingSpace : parkingSpaces) {

            if (parkingSpace.isAvailable()
                    && parkingSpace.getSupportedType()
                    == vehicleType) {

                return parkingSpace;
            }
        }

        return null;
    }

    private ParkingTransaction findActiveTransaction(
            String registrationNumber) {

        for (ParkingTransaction transaction : transactions) {

            boolean sameVehicle =
                    transaction.getVehicle()
                            .getRegistrationNumber()
                            .equalsIgnoreCase(
                                    registrationNumber
                            );

            if (sameVehicle && transaction.isActive()) {
                return transaction;
            }
        }

        return null;
    }

    public void displayParkingSpaces() {

        if (parkingSpaces.isEmpty()) {
            System.out.println(
                    "No parking spaces have been added."
            );
            return;
        }

        System.out.println("\nPARKING SPACES");

        for (ParkingSpace parkingSpace : parkingSpaces) {
            System.out.println(parkingSpace);
        }
    }

    public void displayCustomers() {

        if (customers.isEmpty()) {
            System.out.println(
                    "No customers have been registered."
            );
            return;
        }

        System.out.println("\nREGISTERED CUSTOMERS");

        for (Customer customer : customers) {
            System.out.println(customer);
        }
    }

    public void displayVehicles() {

        if (vehicles.isEmpty()) {
            System.out.println(
                    "No vehicles have been registered."
            );
            return;
        }

        System.out.println("\nREGISTERED VEHICLES");

        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    public void displayTransactions() {

        if (transactions.isEmpty()) {
            System.out.println(
                    "No parking transactions found."
            );
            return;
        }

        System.out.println("\nPARKING TRANSACTIONS");

        for (ParkingTransaction transaction : transactions) {
            System.out.println(transaction);
            System.out.println("-------------------------");
        }
    }

    public void displayAvailableSpaces() {

        boolean found = false;

        System.out.println("\nAVAILABLE PARKING SPACES");

        for (ParkingSpace parkingSpace : parkingSpaces) {

            if (parkingSpace.isAvailable()) {
                System.out.println(parkingSpace);
                found = true;
            }
        }

        if (!found) {
            System.out.println(
                    "No parking spaces are available."
            );
        }
    }

    public void displayTotalCollection() {

        double total = 0;

        for (ParkingTransaction transaction : transactions) {

            if (!transaction.isActive()) {
                total += transaction.getParkingFee();
            }
        }

        System.out.printf(
                "Total Parking Fees Collected: Rs. %.2f%n",
                total
        );
    }
}