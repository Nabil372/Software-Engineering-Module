package com.trafficmon;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

public class testExitEvent
{
    Vehicle testVehicle = Vehicle.withRegistration("testVehicle");
    ExitEvent  testExitEvent = new ExitEvent(testVehicle);

    @Test
    public void testTimestamp()
    {
        assertThat((double) testExitEvent.timestamp() , is(closeTo((double)System.currentTimeMillis(), 100)));
    }

    @Test
    public void testSetVehicle()
    {
        assertTrue(testExitEvent instanceof ExitEvent);
    }

    @Test
    public void testGetVehicle()
    {
        assertThat(testExitEvent.getVehicle() , is(testVehicle));

    }
}
