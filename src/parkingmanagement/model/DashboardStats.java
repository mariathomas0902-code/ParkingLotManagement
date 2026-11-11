package parkingmanagement.model;

import parkingmanagement.VehicleType;

import java.util.Map;

public record DashboardStats(
        int totalSpaces,
        int availableSpaces,
        int occupiedSpaces,
        int activeVehicles,
        int completedTransactions,
        double totalRevenue,
        Map<VehicleType, Integer> availableByType,
        Map<VehicleType, Integer> totalByType
) {
    public double getOccupancyRate() {
        if (totalSpaces == 0) return 0.0;
        return ((double) occupiedSpaces / totalSpaces) * 100.0;
    }
}
