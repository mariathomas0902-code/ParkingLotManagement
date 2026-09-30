package parkingmanagement;

import java.util.Locale;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ParkingLotService service = new ParkingLotService();

    public static void main(String[] args) {
        System.out.println("Parking Lot Management System Started");
        try {
            boolean running = true;
            while (running) {
                System.out.println("\n===== PARKING LOT MANAGEMENT =====");
                System.out.println("1. Park Vehicle");
                System.out.println("2. Exit Vehicle");
                System.out.println("3. View Available Spaces");
                System.out.println("4. View Parked Vehicles");
                System.out.println("5. View Transaction History");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");
                if (!scanner.hasNextLine()) break;
                switch (scanner.nextLine().trim()) {
                    case "1" -> parkVehicle();
                    case "2" -> service.exitVehicle(readRequiredInput("Vehicle number: "));
                    case "3" -> service.showAvailableSpaces();
                    case "4" -> service.showParkedVehicles();
                    case "5" -> service.showTransactionHistory();
                    case "0" -> running = false;
                    default -> System.out.println("Invalid choice. Enter 0 to 5.");
                }
            }
        } catch (java.util.NoSuchElementException exception) {
            System.out.println("Input ended.");
        } finally {
            scanner.close();
        }
        System.out.println("Application closed.");
    }

    private static void parkVehicle() {
        String name = readRequiredInput("Customer name: ");
        String phone;
        while (true) {
            phone = readRequiredInput("Phone number (10 digits): ");
            if (phone.matches("[0-9]{10}")) break;
            System.out.println("Enter a valid 10-digit phone number.");
        }
        String number = readRequiredInput("Vehicle number: ").toUpperCase(Locale.ROOT);
        VehicleType type;
        while (true) {
            String input = readRequiredInput("Vehicle type (BIKE/CAR): ")
                    .toUpperCase(Locale.ROOT);
            if (input.equals("BIKE") || input.equals("CAR")) {
                type = VehicleType.valueOf(input);
                break;
            }
            System.out.println("Invalid type. Enter BIKE or CAR.");
        }
        service.parkVehicle(name, phone, number, type);
    }

    private static String readRequiredInput(String message) {
        while (true) {
            System.out.print(message);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }
}
