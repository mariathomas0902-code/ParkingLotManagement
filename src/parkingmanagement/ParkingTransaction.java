package parkingmanagement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkingTransaction {

    private int ticketId;
    private Vehicle vehicle;
    private ParkingSpace parkingSpace;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double parkingFee;
    private boolean active;

    public ParkingTransaction(
            int ticketId,
            Vehicle vehicle,
            ParkingSpace parkingSpace) {

        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.parkingSpace = parkingSpace;
        this.entryTime = LocalDateTime.now();
        this.active = true;
    }

    public int getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpace getParkingSpace() {
        return parkingSpace;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public double getParkingFee() {
        return parkingFee;
    }

    public boolean isActive() {
        return active;
    }

    public void closeTransaction(
            LocalDateTime exitTime,
            double parkingFee) {

        this.exitTime = exitTime;
        this.parkingFee = parkingFee;
        this.active = false;
    }

    @Override
    public String toString() {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy HH:mm:ss"
                );

        String exitValue;

        if (exitTime == null) {
            exitValue = "Vehicle is still parked";
        } else {
            exitValue = exitTime.format(formatter);
        }

        return "\nTicket ID: " + ticketId
                + "\nVehicle Number: "
                + vehicle.getRegistrationNumber()
                + "\nVehicle Type: "
                + vehicle.getVehicleType()
                + "\nParking Slot: "
                + parkingSpace.getSlotId()
                + "\nEntry Time: "
                + entryTime.format(formatter)
                + "\nExit Time: " + exitValue
                + "\nParking Fee: Rs. "
                + String.format("%.2f", parkingFee)
                + "\nStatus: "
                + (active ? "ACTIVE" : "COMPLETED");
    }
}
