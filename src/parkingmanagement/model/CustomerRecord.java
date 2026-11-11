package parkingmanagement.model;

import parkingmanagement.VehicleType;

public record CustomerRecord(
        int customerId,
        String name,
        String phoneNumber,
        String email,
        String registrationNumber,
        VehicleType vehicleType
) {
}
