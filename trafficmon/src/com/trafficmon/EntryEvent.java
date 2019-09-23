package com.trafficmon;

public class EntryEvent extends ZoneBoundaryCrossing{
    public EntryEvent(VehicleInterface vehicleRegistration) {
        super(vehicleRegistration);
    }
    // Package-Private Method For Testing
    EntryEvent(VehicleInterface vehicleRegistration,long time) {super(vehicleRegistration, time);}
}
