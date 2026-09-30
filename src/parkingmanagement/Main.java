package parkingmanagement;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner =
            new Scanner(System.in);

    private static final ParkingLotService service =
            new ParkingLotService();

    public static void main(String[] args) {

        boolean running = true;

        System.out.println(
                "Parking Lot Management System Started"
        );

        while (running) {
            displayMenu();

            String choice =
                    scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    parkVehicle();
                    break;

                case "2":
                    exitVehicle();
                    break;

                case "3":
                    service.showAvailableSpaces();
                    break;

                case "4":
                    service.showParkedVehicles();
                    break;

                case "5":
                    service.showTransactionHistory();
                    break;

                case "0":
                    running = false;

                    System.out.println(
                            "Application closed."
                    );
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Enter 0 to 5."
                    );
            }
        }

        scanner.close();
    }

    private static void displayMenu() {

        System.out.println(
                "\n===== PARKING LOT MANAGEMENT ====="
        );

        System.out.println("1. Park Vehicle");
        System.out.println("2. Exit Vehicle");
        System.out.println("3. View Available Spaces");
        System.out.println("4. View Parked Vehicles");
        System.out.println("5. View Transaction History");
        System.out.println("0. Exit");

        System.out.print("Enter your choice: ");
    }

    private static void parkVehicle() {

        String customerName =
                readRequiredInput("Customer name: ");

        String phoneNumber =
                readPhoneNumber();

        String vehicleNumber =
                readRequiredInput("Vehicle number: ")
                        .toUpperCase();

        VehicleType vehicleType =
                readVehicleType();

        service.parkVehicle(
                customerName,
                phoneNumber,
                vehicleNumber,
                vehicleType
        );
    }

    private static void exitVehicle() {

        String vehicleNumber =
                readRequiredInput("Vehicle number: ")
                        .toUpperCase();

        service.exitVehicle(vehicleNumber);
    }

    private static String readRequiredInput(
            String message
    ) {
        while (true) {
            System.out.print(message);

            String value =
                    scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println(
                    "This field cannot be empty."
            );
        }
    }

    private static String readPhoneNumber() {

        while (true) {
            System.out.print(
                    "Phone number (10 digits): "
            );

            String phone =
                    scanner.nextLine().trim();

            if (phone.matches("\\d{10}")) {
                return phone;
            }

            System.out.println(
                    "Enter a valid 10-digit phone number."
            );
        }
    }

    private static VehicleType readVehicleType() {

        while (true) {
            System.out.print(
                    "Vehicle type (BIKE/CAR): "
            );

            String input =
                    scanner.nextLine()
                            .trim()
                            .toUpperCase();

            try {
                return VehicleType.valueOf(input);

            } catch (IllegalArgumentException exception) {
                System.out.println(
                        "Invalid type. Enter BIKE or CAR."
                );
            }
        }
    }
}