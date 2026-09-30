package parkingmanagement;

import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {

    private FeeCalculator() {
    }

    public static double calculateFee(
            VehicleType vehicleType,
            LocalDateTime entryTime,
            LocalDateTime exitTime
    ) {
        long minutes =
                Duration.between(entryTime, exitTime).toMinutes();

        long hours = Math.max(
                1,
                (long) Math.ceil(minutes / 60.0)
        );

        double hourlyRate;

        switch (vehicleType) {
            case BIKE:
                hourlyRate = 20.0;
                break;

            case CAR:
                hourlyRate = 40.0;
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported vehicle type"
                );
        }

        return hours * hourlyRate;
    }
}