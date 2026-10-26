package parkingmanagement;

import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {

    private FeeCalculator() {
    }

    public static double getHourlyRate(VehicleType vehicleType) {
        if (vehicleType == null) return 40.0;
        return switch (vehicleType) {
            case BIKE -> 20.0;
            case CAR -> 40.0;
            case TRUCK -> 60.0;
        };
    }

    public static double calculateFee(
            VehicleType vehicleType,
            LocalDateTime entryTime,
            LocalDateTime exitTime
    ) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Unsupported vehicle type");
        }

        if (entryTime == null || exitTime == null || exitTime.isBefore(entryTime)) {
            return getHourlyRate(vehicleType);
        }

        long minutes = Duration.between(entryTime, exitTime).toMinutes();

        long hours = Math.max(
                1,
                (long) Math.ceil(minutes / 60.0)
        );

        return hours * getHourlyRate(vehicleType);
    }

    public static String formatDuration(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            return "0 min";
        }
        long minutes = Math.max(0, Duration.between(entryTime, exitTime).toMinutes());
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + " min(s)";
        }
        return hours + " hr(s) " + remainingMinutes + " min(s)";
    }
}
