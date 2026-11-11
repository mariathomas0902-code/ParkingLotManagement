package parkingmanagement.model;

import parkingmanagement.FeeCalculator;
import parkingmanagement.VehicleType;

import java.time.LocalDateTime;

public record ActiveParkingRecord(
        int ticketId,
        String registrationNumber,
        VehicleType vehicleType,
        int slotId,
        String customerName,
        String phoneNumber,
        LocalDateTime entryTime
) {
    public String getFormattedDuration() {
        return FeeCalculator.formatDuration(entryTime, LocalDateTime.now());
    }

    public double getEstimatedFee() {
        return FeeCalculator.calculateFee(vehicleType, entryTime, LocalDateTime.now());
    }
}
