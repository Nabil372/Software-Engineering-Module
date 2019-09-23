package com.trafficmon;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

public class testEntryEvent
{
    private Vehicle testVehicle = Vehicle.withRegistration("testVehicle");
    private EntryEvent  testEntryEvent = new EntryEvent(testVehicle);

    @Test
    public void testTimestamp()
    {
        assertThat((double) testEntryEvent.timestamp() , is(closeTo((double)System.currentTimeMillis(), 100)));
    }

    @Test
    public void testSetVehicle()
    {
        assertTrue(testEntryEvent instanceof EntryEvent);
    }

    @Test
    public void testGetVehicle()
    {
        assertThat(testEntryEvent.getVehicle() , is(testVehicle));

    }
}
