package parkingmanagement;

public class FeeCalculator {

    private FeeCalculator() {
        // Prevent creation of FeeCalculator objects
    }

    public static double calculateFee(
            VehicleType vehicleType,
            long hours) {

        if (hours < 1) {
            hours = 1;
        }

        double firstHourRate;
        double additionalHourRate;

        switch (vehicleType) {

            case BIKE:
                firstHourRate = 20.0;
                additionalHourRate = 10.0;
                break;

            case CAR:
                firstHourRate = 40.0;
                additionalHourRate = 20.0;
                break;

            case TRUCK:
                firstHourRate = 60.0;
                additionalHourRate = 30.0;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid vehicle type"
                );
        }

        if (hours == 1) {
            return firstHourRate;
        }

        return firstHourRate
                + ((hours - 1) * additionalHourRate);
    }
}
