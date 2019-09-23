package com.trafficmon;

public class Vehicle implements VehicleInterface {

    private final String registration;

    private Vehicle(String registration) {
        this.registration = registration;
    }

    static Vehicle withRegistration(String registration) {
        return new Vehicle(registration);
    }

    @Override
    public String toString() {
        return "Vehicle [" + registration + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vehicle vehicle = (Vehicle) o;

        return (registration != null) ? registration.equals(vehicle.registration) : vehicle.registration == null;

        /*if (registration != null){
                return registration.equals(vehicle.registration);
        }
        return (vehicle.registration == null);*/
    }

    @Override
    public int hashCode() {
        return registration != null ? registration.hashCode() : 0;
    }
}
