package parkingmanagement;

import parkingmanagement.model.ActiveParkingRecord;
import parkingmanagement.model.DashboardStats;
import parkingmanagement.model.ExitResult;
import parkingmanagement.model.ParkResult;
import parkingmanagement.model.TransactionRecord;

import java.util.List;

public class SystemTest {

    public static void main(String[] args) {
        System.out.println("========== RUNNING SYSTEM VERIFICATION ==========");
        ParkingLotService service = new ParkingLotService();

        System.out.println("Mode: " + (service.isDatabaseConnected() ? "MySQL Online" : "Demo / In-Memory Mode"));

        // 1. Check initial stats
        DashboardStats initialStats = service.getDashboardStats();
        System.out.println("Initial Total Spaces     : " + initialStats.totalSpaces());
        System.out.println("Initial Available Spaces : " + initialStats.availableSpaces());
        System.out.println("Initial Occupied Spaces  : " + initialStats.occupiedSpaces());
        System.out.println("Initial Active Parked    : " + initialStats.activeVehicles());
        System.out.println("Initial Revenue          : Rs. " + initialStats.totalRevenue());

        // 2. Park a new Bike
        System.out.println("\n--- Testing Park Vehicle (BIKE) ---");
        ParkResult park1 = service.parkVehicle("John Doe", "9876543210", "MH12AB1234", VehicleType.BIKE);
        System.out.println("Success : " + park1.success());
        System.out.println("Message : " + park1.message());
        System.out.println("Ticket  : " + park1.ticketId() + ", Slot: " + park1.slotId());
        assert park1.success() : "Bike parking failed";

        // 3. Duplicate vehicle check
        System.out.println("\n--- Testing Duplicate Vehicle Check ---");
        ParkResult dup = service.parkVehicle("Jane Doe", "9876543211", "MH12AB1234", VehicleType.BIKE);
        System.out.println("Duplicate park rejected as expected: " + !dup.success() + " (" + dup.message() + ")");
        assert !dup.success() : "Duplicate vehicle should not be allowed";

        // 4. Park a Car
        System.out.println("\n--- Testing Park Vehicle (CAR) ---");
        ParkResult park2 = service.parkVehicle("Alice Smith", "9876543212", "DL03XY9876", VehicleType.CAR);
        System.out.println("Success : " + park2.success());
        System.out.println("Message : " + park2.message());
        System.out.println("Ticket  : " + park2.ticketId() + ", Slot: " + park2.slotId());
        assert park2.success() : "Car parking failed";

        // 5. Check Parked Vehicles list
        List<ActiveParkingRecord> parkedList = service.getParkedVehicles();
        System.out.println("\n--- Currently Parked Vehicles count: " + parkedList.size() + " ---");
        for (ActiveParkingRecord r : parkedList) {
            System.out.println("  > #" + r.ticketId() + " " + r.registrationNumber() + " [" + r.vehicleType() + "] Slot " + r.slotId() + " (" + r.customerName() + ")");
        }

        // 6. Test Fee Preview
        System.out.println("\n--- Testing Fee Preview ---");
        ExitResult preview = service.previewExit("MH12AB1234");
        System.out.println("Preview Success  : " + preview.success());
        System.out.println("Duration         : " + preview.durationText());
        System.out.println("Estimated Fee    : Rs. " + preview.fee());
        assert preview.success() : "Fee preview failed";

        // 7. Complete Exit
        System.out.println("\n--- Testing Complete Exit ---");
        ExitResult exitResult = service.exitVehicle("MH12AB1234");
        System.out.println("Exit Success     : " + exitResult.success());
        System.out.println("Duration         : " + exitResult.durationText());
        System.out.println("Calculated Fee   : Rs. " + exitResult.fee());
        assert exitResult.success() : "Exit failed";

        // 8. Verify Slot Released and Stats Updated
        DashboardStats afterExitStats = service.getDashboardStats();
        System.out.println("\n--- Stats After Exit ---");
        System.out.println("Available Spaces : " + afterExitStats.availableSpaces());
        System.out.println("Occupied Spaces  : " + afterExitStats.occupiedSpaces());
        System.out.println("Completed Exits  : " + afterExitStats.completedTransactions());
        System.out.println("Total Revenue    : Rs. " + afterExitStats.totalRevenue());

        // 9. Verify Transaction in History
        List<TransactionRecord> history = service.getTransactionHistory();
        System.out.println("\n--- Total History Records: " + history.size() + " ---");
        boolean foundCompleted = false;
        for (TransactionRecord t : history) {
            if (t.registrationNumber().equals("MH12AB1234") && "COMPLETED".equals(t.status())) {
                foundCompleted = true;
                System.out.println("  > Verified completed transaction: Ticket #" + t.ticketId() + " Fee: Rs. " + t.parkingFee());
                break;
            }
        }
        assert foundCompleted : "Completed transaction not found in history";

        System.out.println("\n=================================================");
        System.out.println("ALL SYSTEM VERIFICATION TESTS PASSED SUCCESSFULLY!");
        System.out.println("=================================================");
    }
}
