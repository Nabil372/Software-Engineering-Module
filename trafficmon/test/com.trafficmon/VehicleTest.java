package com.trafficmon;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class VehicleTest {
    Vehicle testVehicle = Vehicle.withRegistration("testVehicle");

    @Test
    public void testInstanceVariable()
    {
        assertThat(testVehicle.toString() , containsString("testVehicle"));
    }

    @Test
    public void testEqualsMethod()
    {
        assertTrue(testVehicle.equals(testVehicle));
        assertFalse(testVehicle.equals(null));
        Vehicle testVehicle2 = Vehicle.withRegistration("testVehicle2");
        assertFalse(testVehicle.equals(testVehicle2));
    }

    @Test
    public void testToStringMethod()
    {
        assertThat(testVehicle.toString() , is("Vehicle [testVehicle]"));
    }

    @Test
    public void testHashCode()
    {
        assertThat(testVehicle.hashCode() , is("testVehicle".hashCode()));
        assertThat(Vehicle.withRegistration(null).hashCode() , is(0));
    }
}
