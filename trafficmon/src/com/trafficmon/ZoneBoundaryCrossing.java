package com.trafficmon;

public abstract class ZoneBoundaryCrossing {

    private final VehicleInterface vehicle;
    private final long time;

    public ZoneBoundaryCrossing(VehicleInterface vehicle) {
        this.vehicle = vehicle;
        this.time = System.currentTimeMillis();
    }
    //Package private constructor for testing purposes
    ZoneBoundaryCrossing(VehicleInterface vehicle, long time) {
        this.vehicle = vehicle;
        this.time = time;
    }

    public VehicleInterface getVehicle() {
        return vehicle;
    }

    public long timestamp() {
        return time;
    }
}
