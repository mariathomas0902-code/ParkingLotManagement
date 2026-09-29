package parkingmanagement;

public class Vehicle {
    private String registrationNumber;
    private VehicleType vehicleType;
    private Customer owner;

    public Vehicle(String registrationNumber,
                   VehicleType vehicleType,
                   Customer owner) {

        this.registrationNumber =
                registrationNumber.toUpperCase();

        this.vehicleType = vehicleType;
        this.owner = owner;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Customer getOwner() {
        return owner;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Registration Number: " + registrationNumber
                + ", Type: " + vehicleType
                + ", Owner: " + owner.getName();
    }
}
