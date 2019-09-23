package com.trafficmon;

public class ExitEvent extends ZoneBoundaryCrossing {
    public ExitEvent(VehicleInterface vehicle) {
        super(vehicle);
    }
    // Package-Private Method For Testing
    ExitEvent(VehicleInterface vehicle, long time) {
        super(vehicle,time);
    }
}
