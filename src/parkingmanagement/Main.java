package parkingmanagement;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner =
            new Scanner(System.in);

    private static final ParkingLotService service =
            new ParkingLotService();

    public static void main(String[] args) {

        int choice;

        do {
            displayMenu();
            choice = readInteger("Enter your choice: ");

            switch (choice) {

                case 1:
                    addParkingSpace();
                    break;

                case 2:
                    service.displayParkingSpaces();
                    break;

                case 3:
                    registerCustomer();
                    break;

                case 4:
                    registerVehicle();
                    break;

                case 5:
                    vehicleEntry();
                    break;

                case 6:
                    vehicleExit();
                    break;

                case 7:
                    searchVehicle();
                    break;

                case 8:
                    service.displayAvailableSpaces();
                    break;

                case 9:
                    service.displayCustomers();
                    break;

                case 10:
                    service.displayVehicles();
                    break;

                case 11:
                    service.displayTransactions();
                    break;

                case 12:
                    service.displayTotalCollection();
                    break;

                case 0:
                    System.out.println(
                            "Thank you. Program closed."
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }

        } while (choice != 0);

        scanner.close();
    }

    private static void displayMenu() {

        System.out.println(
                "\n=================================="
        );
        System.out.println(
                "   PARKING LOT MANAGEMENT SYSTEM"
        );
        System.out.println(
                "=================================="
        );
        System.out.println("1. Add Parking Space");
        System.out.println("2. View All Parking Spaces");
        System.out.println("3. Register Customer");
        System.out.println("4. Register Vehicle");
        System.out.println("5. Vehicle Entry");
        System.out.println("6. Vehicle Exit");
        System.out.println("7. Search Vehicle");
        System.out.println("8. View Available Spaces");
        System.out.println("9. View Customers");
        System.out.println("10. View Vehicles");
        System.out.println("11. View Transactions");
        System.out.println("12. View Total Collection");
        System.out.println("0. Exit");
    }

    private static void addParkingSpace() {

        int slotId =
                readInteger("Enter parking slot ID: ");

        VehicleType vehicleType =
                readVehicleType();

        service.addParkingSpace(
                slotId,
                vehicleType
        );
    }

    private static void registerCustomer() {

        int customerId =
                readInteger("Enter customer ID: ");

        System.out.print("Enter customer name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter email address: ");
        String email = scanner.nextLine().trim();

        service.registerCustomer(
                customerId,
                name,
                phone,
                email
        );
    }

    private static void registerVehicle() {

        System.out.print(
                "Enter vehicle registration number: "
        );

        String registrationNumber =
                scanner.nextLine().trim();

        VehicleType vehicleType =
                readVehicleType();

        int customerId =
                readInteger("Enter owner customer ID: ");

        service.registerVehicle(
                registrationNumber,
                vehicleType,
                customerId
        );
    }

    private static void vehicleEntry() {

        System.out.print(
                "Enter vehicle registration number: "
        );

        String registrationNumber =
                scanner.nextLine().trim();

        service.vehicleEntry(registrationNumber);
    }

    private static void vehicleExit() {

        System.out.print(
                "Enter vehicle registration number: "
        );

        String registrationNumber =
                scanner.nextLine().trim();

        service.vehicleExit(registrationNumber);
    }

    private static void searchVehicle() {

        System.out.print(
                "Enter vehicle registration number: "
        );

        String registrationNumber =
                scanner.nextLine().trim();

        Vehicle vehicle =
                service.findVehicle(registrationNumber);

        if (vehicle == null) {
            System.out.println("Vehicle not found.");
        } else {
            System.out.println(
                    "Vehicle found:\n" + vehicle
            );
        }
    }

    private static VehicleType readVehicleType() {

        while (true) {

            System.out.println("Select vehicle type:");
            System.out.println("1. Bike");
            System.out.println("2. Car");
            System.out.println("3. Truck");

            int typeChoice =
                    readInteger("Enter vehicle type: ");

            switch (typeChoice) {

                case 1:
                    return VehicleType.BIKE;

                case 2:
                    return VehicleType.CAR;

                case 3:
                    return VehicleType.TRUCK;

                default:
                    System.out.println(
                            "Invalid vehicle type."
                    );
            }
        }
    }

    private static int readInteger(String message) {

        while (true) {

            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);

            } catch (NumberFormatException exception) {

                System.out.println(
                        "Please enter a valid number."
                );
            }
        }
    }
}
