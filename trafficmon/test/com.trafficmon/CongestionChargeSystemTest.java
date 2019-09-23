package com.trafficmon;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CongestionChargeSystemTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private OperationsServiceInterface mockOperationsService = context.mock(OperationsServiceInterface.class);
    private CustomerAccountsServiceInterface mockAccountsService = context.mock(CustomerAccountsServiceInterface.class);

    private Vehicle testVehicle = Vehicle.withRegistration("A123 XYZ");
    private Vehicle testVehicle2 = Vehicle.withRegistration("B113 ZYZ");
    private Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<VehicleInterface, List<ZoneBoundaryCrossing>>();
    private CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem(crossingsByVehicle, mockAccountsService,mockOperationsService);
    private final static long START_OF_DAY = 1543536000001L;//start of a day in ms from epoch (for testing purposes)

    private void addCrossingEvent(double EntryHour, double ExitHour, VehicleInterface testVehiclee ){
        /*eventLog.add(new EntryEvent(testVehicle, (START_OF_DAY+(long)(EntryHour*CongestionChargeSystem.HOURS_TO_MILLI_SECONDS))));
        eventLog.add(new ExitEvent(testVehicle, (START_OF_DAY+(long)(ExitHour*CongestionChargeSystem.HOURS_TO_MILLI_SECONDS)) ));*/
        if (!crossingsByVehicle.containsKey(testVehiclee)) {
            crossingsByVehicle.put(testVehiclee, new ArrayList<ZoneBoundaryCrossing>());
        }
        crossingsByVehicle.get(testVehiclee).add(new EntryEvent(testVehiclee, (START_OF_DAY+(long)(EntryHour*CongestionChargeSystem.HOURS_TO_MILLI_SECONDS))));
        crossingsByVehicle.get(testVehiclee).add(new ExitEvent(testVehiclee, (START_OF_DAY+(long)(ExitHour*CongestionChargeSystem.HOURS_TO_MILLI_SECONDS)) ));
    }

    @Test
    public void testVariables(){
        assertThat(crossingsByVehicle.size(), is(0));
        assertThat(CongestionChargeSystem.HOURS_TO_MILLI_SECONDS, is(60*60*1000L));
    }

    @Test
    public void testVehicleEnteringZone(){
        assertThat(crossingsByVehicle.size(), is(0));
        congestionChargeSystem.vehicleEnteringZone(testVehicle);
        assertThat(crossingsByVehicle.size(), is(1));
        assertTrue(crossingsByVehicle.get(testVehicle).get(0) instanceof  EntryEvent);
        assertThat(crossingsByVehicle.get(testVehicle).size(),is(1));

        congestionChargeSystem.vehicleEnteringZone(testVehicle);
        assertThat(crossingsByVehicle.get(testVehicle).size(), is(2));
        assertTrue(crossingsByVehicle.get(testVehicle).get(1) instanceof  EntryEvent);
    }

    @Test
    public void testVehicleLeavingZone(){
        assertThat(crossingsByVehicle.size(), is(0));
        congestionChargeSystem.vehicleEnteringZone(testVehicle);
        assertThat(crossingsByVehicle.size(), is(1));
        assertThat(crossingsByVehicle.get(testVehicle).size(), is(1));
        assertTrue(crossingsByVehicle.get(testVehicle).get(0) instanceof  EntryEvent);
        assertThat(crossingsByVehicle.get(testVehicle).get(0).getVehicle() , is(testVehicle));
        assertTrue( (crossingsByVehicle.get(testVehicle).get(0).timestamp() - System.currentTimeMillis()) < 10 );//the timestamp will close to current system time

        congestionChargeSystem.vehicleLeavingZone(testVehicle);
        assertThat(crossingsByVehicle.size(), is(1));
        assertThat(crossingsByVehicle.get(testVehicle).size(), is(2));
        assertTrue(crossingsByVehicle.get(testVehicle).get(1) instanceof  ExitEvent);
        assertThat(crossingsByVehicle.get(testVehicle).get(1).getVehicle() , is(testVehicle));
    }

    @Test
    public void testPreviouslyRegistered(){
        //Can not start with an exit event (testing previously registered)
        assertThat(crossingsByVehicle.size(), is(0));
        congestionChargeSystem.vehicleLeavingZone(testVehicle);
        assertThat(crossingsByVehicle.size(), is(0));

        congestionChargeSystem.vehicleEnteringZone(testVehicle);
        assertThat(crossingsByVehicle.size(), is(1));
        assertThat(crossingsByVehicle.get(testVehicle).size(), is(1));
        congestionChargeSystem.vehicleLeavingZone(testVehicle2);
        assertThat(crossingsByVehicle.size(), is(1));
        assertThat(crossingsByVehicle.get(testVehicle).size(), is(1));
        //Can not start with an exit event (testing previously registered)*
    }



    @Test
    public void testCalculateChargesNoEntry(){
        context.checking(new Expectations() {{
            never(mockAccountsService);
        }});
        assertThat(crossingsByVehicle.size(), is(0));
        congestionChargeSystem.calculateCharges();
        assertThat(crossingsByVehicle.size(), is(0));

    }


    //Testing checkOrderingOf
    @Test
    public void testOnlyEntry() throws Exception{
        context.checking(new Expectations() {{
            never(mockAccountsService);
            exactly(1).of(mockOperationsService).triggerInvestigationInto(with(equal(testVehicle)));

        }});
        congestionChargeSystem.vehicleEnteringZone(testVehicle);
        congestionChargeSystem.calculateCharges();

    }

    @Test
    public void testOnlyExit() throws Exception{
        context.checking(new Expectations() {{
            never(mockAccountsService);
            exactly(1).of(mockOperationsService).triggerInvestigationInto(with(equal(testVehicle)));

        }});
        crossingsByVehicle.put(testVehicle, new ArrayList<ZoneBoundaryCrossing>());
        crossingsByVehicle.get(testVehicle).add(new ExitEvent(testVehicle, START_OF_DAY));
        congestionChargeSystem.calculateCharges();
    }


    @Test
    public void testSecondCrossingBeforeFirstOne() throws Exception{
        context.checking(new Expectations() {{
            never(mockAccountsService);
            exactly(1).of(mockOperationsService).triggerInvestigationInto(with(equal(testVehicle)));

        }});
        addCrossingEvent(0.5,0.5,testVehicle);
        addCrossingEvent(0,0.6,testVehicle);
        congestionChargeSystem.calculateCharges();
    }
    //Testing checkOrderingOf*

    //Make sure charges are correct for all cases
    @Test
    public void testCalculateChargesOneCrossingBeforeTwoPM() throws Exception{
        context.checking(new Expectations() {{
                exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(6.00)) ));

        }});
        addCrossingEvent(0,0.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }


    @Test
    public void testCalculateChargesOneCrossingAtTwoPM() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(4.00)) ));

        }});
        addCrossingEvent(14,14.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesOneCrossingAfterTwoPM() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(4.00)) ));

        }});
        addCrossingEvent(14.1,14.5,testVehicle);
        congestionChargeSystem.calculateCharges();

    }

    //if staying for exactly 4 hours should not charge 12 gbp
    @Test
    public void testCalculateChargesOneCrossingBeforeTwoPMForFourHours() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(6.00)) ));

        }});
        addCrossingEvent(0,4,testVehicle);
        congestionChargeSystem.calculateCharges();
    }


    @Test
    public void testCalculateChargesOneCrossingAfterTwoPMForFourHours() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(4.00)) ));

        }});
        addCrossingEvent(15,19,testVehicle);
        congestionChargeSystem.calculateCharges();
    }
    //if staying for exactly 4 hours should not charge 12 gbp*

    //More than 4 hours regardless of entry time should be charge 12 gbp
    @Test
    public void testCalculateChargesOneCrossingBeforeTwoPMOverFourHours() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));

        }});
        addCrossingEvent(0,8,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesOneCrossingAfterTwoPMOverFourHours() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));
        }});
        addCrossingEvent(14.1,19.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }
    //More than 4 hours regardless of entry time should be charge 12 gbp*

    //Make sure can enter and exit multiple times within four hours of entry where you were charged without extra charges
    @Test
    public void testCalculateChargesMultipleCrossingBeforeTwoWithinFourHours() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(6.00)) ));

        }});
        addCrossingEvent(0,0.5,testVehicle);
        addCrossingEvent(1,1.1,testVehicle);
        addCrossingEvent(1.5,1.9,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesMultipleCrossingAfterTwoWithinFourHours() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(4.00)) ));

        }});
        addCrossingEvent(14,14.5,testVehicle);
        addCrossingEvent(15.5,15.6,testVehicle);
        addCrossingEvent(16.5,17.9,testVehicle);
        congestionChargeSystem.calculateCharges();
    }


    @Test
    public void testCalculateChargesMultipleCrossingBeforeTwoAndAfterTwoWithinFourHours() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(6.00)) ));

        }});
        addCrossingEvent(13.5,14,testVehicle);
        addCrossingEvent(15,16.1,testVehicle);
        addCrossingEvent(17.4,17.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }
    //Make sure can enter and exit multiple times within four hours of entry where you were charged without extra charges*

    //Make sure you are charged appropriately if you enter after four hours from entry you were previously charged at
    @Test
    public void testCalculateChargesTwoCrossingBeforeTwoOverFourHourIntervals() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));

        }});
        addCrossingEvent(0,1,testVehicle);
        addCrossingEvent(4.1,4.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesMultipleCrossingBeforeTwoOverFourHourIntervals() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(18.00)) ));

        }});
        addCrossingEvent(0,1,testVehicle);
        addCrossingEvent(4.1,4.5,testVehicle);
        addCrossingEvent(8.1,8.5,testVehicle);
        congestionChargeSystem.calculateCharges();
        //Discount if total time exceeds four hours
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));

        }});
        addCrossingEvent(12.1,16.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesTwoCrossingAfterTwoOverFourHourIntervals() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(8.00)) ));
        }});
        addCrossingEvent(14,14.5,testVehicle);
        addCrossingEvent(18.1,18.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }
    //Make sure you are charged appropriately if you enter after four hours from entry you were previously charged at (if total time under 4 hours)*

    //Make sure you get charged if you enter within four hours of the previous entry for which you were charged but stayed past the 4 hour mark
    @Test
    public void testCalculateChargesInterFourHourIntervalCrossingsBefore2PM() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));
        }});
        addCrossingEvent(0,0.5,testVehicle);
        addCrossingEvent(3.8,4.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesInterFourHourIntervalCrossingsAfter2PM() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(8.00)) ));
        }});
        addCrossingEvent(14,14.5,testVehicle);
        addCrossingEvent(17.8,18.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesInterFourHourIntervalCrossingsBefore2TillAfter2() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(10.00)) ));
        }});
        addCrossingEvent(10,10.5,testVehicle);
        addCrossingEvent(13.999,14.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }

    @Test
    public void testCalculateChargesMultipleCrossingsUnderFourHoursOverFourHourIntervals() throws Exception{
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));
        }});
        addCrossingEvent(14,14.5,testVehicle);
        addCrossingEvent(18.1,18.5,testVehicle);
        addCrossingEvent(22.1,23.5,testVehicle);
        congestionChargeSystem.calculateCharges();
    }
    //Make sure you get charged if you enter within four hours of the previous entry for which you were charged but stayed past the 4 hour mark*
    @Test
    public void testCalculateChargesComplexCase() throws Exception{
        Vehicle testVehicle3 = Vehicle.withRegistration("C153 ZIZ");
        Vehicle testVehicle4 = Vehicle.withRegistration("B113 ZYZ");
        Vehicle testVehicle5 = Vehicle.withRegistration("B11R ZYZ");
        context.checking(new Expectations() {{
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle3)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle)), with(Matchers.comparesEqualTo(new BigDecimal(12.00)) ));
            exactly(1).of(mockAccountsService).deductChargeFrom(with(equal(testVehicle4)), with(Matchers.comparesEqualTo(new BigDecimal(20.00)) ));
            exactly(1).of(mockOperationsService).triggerInvestigationInto(with(equal(testVehicle5)));
        }});
        addCrossingEvent(14,14.5,testVehicle);
        addCrossingEvent(18.1,18.5,testVehicle);
        addCrossingEvent(22.1,23.5,testVehicle);

        addCrossingEvent(0,0.1,testVehicle4);
        addCrossingEvent(3.8,4.5,testVehicle2);
        addCrossingEvent(18.1,18.5,testVehicle2);
        addCrossingEvent(22.1,23.5,testVehicle2);


        addCrossingEvent(0,5,testVehicle3);
        crossingsByVehicle.put(testVehicle5, new ArrayList<ZoneBoundaryCrossing>());
        crossingsByVehicle.get(testVehicle5).add(new EntryEvent(testVehicle5, START_OF_DAY));
        congestionChargeSystem.calculateCharges();
    }

}