package parkingmanagement.model;

import parkingmanagement.VehicleType;

import java.time.LocalDateTime;

public record ExitResult(
        boolean success,
        String message,
        int ticketId,
        int slotId,
        String vehicleNumber,
        VehicleType vehicleType,
        String customerName,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        double fee,
        String durationText
) {
    public static ExitResult failure(String message) {
        return new ExitResult(false, message, 0, 0, null, null, null, null, null, 0.0, null);
    }

    public static ExitResult success(
            int ticketId,
            int slotId,
            String vehicleNumber,
            VehicleType vehicleType,
            String customerName,
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            double fee,
            String durationText
    ) {
        return new ExitResult(
                true,
                "Vehicle exit completed successfully.",
                ticketId,
                slotId,
                vehicleNumber,
                vehicleType,
                customerName,
                entryTime,
                exitTime,
                fee,
                durationText
        );
    }
}
