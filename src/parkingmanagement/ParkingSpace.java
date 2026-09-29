package parkingmanagement;

public class ParkingSpace {
    private int slotId;
    private VehicleType supportedType;
    private SlotStatus status;

    public ParkingSpace(int slotId,
                        VehicleType supportedType) {

        this.slotId = slotId;
        this.supportedType = supportedType;
        this.status = SlotStatus.AVAILABLE;
    }

    public int getSlotId() {
        return slotId;
    }

    public VehicleType getSupportedType() {
        return supportedType;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    public void occupy() {
        status = SlotStatus.OCCUPIED;
    }

    public void release() {
        status = SlotStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Slot ID: " + slotId
                + ", Supported Type: " + supportedType
                + ", Status: " + status;
    }

}
