package parkingmanagement.model;

import parkingmanagement.FeeCalculator;
import parkingmanagement.VehicleType;

import java.time.LocalDateTime;

public record TransactionRecord(
        int ticketId,
        String registrationNumber,
        VehicleType vehicleType,
        int slotId,
        String customerName,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        double parkingFee,
        String status
) {
    public String getFormattedDuration() {
        if (exitTime == null) {
            return FeeCalculator.formatDuration(entryTime, LocalDateTime.now());
        }
        return FeeCalculator.formatDuration(entryTime, exitTime);
    }
}
