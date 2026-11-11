package parkingmanagement.model;

import parkingmanagement.VehicleType;

import java.time.LocalDateTime;

public record ParkResult(
        boolean success,
        String message,
        int ticketId,
        int slotId,
        String vehicleNumber,
        VehicleType vehicleType,
        String customerName,
        LocalDateTime entryTime
) {
    public static ParkResult failure(String message) {
        return new ParkResult(false, message, 0, 0, null, null, null, null);
    }

    public static ParkResult success(
            int ticketId,
            int slotId,
            String vehicleNumber,
            VehicleType vehicleType,
            String customerName,
            LocalDateTime entryTime
    ) {
        return new ParkResult(
                true,
                "Vehicle parked successfully.",
                ticketId,
                slotId,
                vehicleNumber,
                vehicleType,
                customerName,
                entryTime
        );
    }
}
