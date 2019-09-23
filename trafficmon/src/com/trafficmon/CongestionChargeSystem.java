package com.trafficmon;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class CongestionChargeSystem {
    //Variables
    public static final long HOURS_TO_MILLI_SECONDS = 60*60*1000L;
    private Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicle;
    private CustomerAccountsServiceInterface customerAccountService;
    private  OperationsServiceInterface operationsService;
    //Variables*

    //Package Private Constructor for testing
    CongestionChargeSystem(Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicle,CustomerAccountsServiceInterface customerAccountService,OperationsServiceInterface opsi){
        this.crossingsByVehicle = crossingsByVehicle;
        this.customerAccountService = customerAccountService;
        this.operationsService = opsi;
    }

    public  CongestionChargeSystem(){
        this.operationsService = new OperationsService();
        this.customerAccountService = new CustomerAccountsService();
        this.crossingsByVehicle = new HashMap<VehicleInterface, List<ZoneBoundaryCrossing>>();
    }

    public void vehicleEnteringZone(VehicleInterface vehicle) {
        if (!crossingsByVehicle.containsKey(vehicle)) {
            crossingsByVehicle.put(vehicle, new ArrayList<ZoneBoundaryCrossing>());
        }
        crossingsByVehicle.get(vehicle).add(new EntryEvent(vehicle));
    }

    public void vehicleLeavingZone(VehicleInterface vehicle) {
        //Make sure vehicle is registered
        if ((!crossingsByVehicle.containsKey(vehicle))||(crossingsByVehicle.get(vehicle).size()<=0)) {
            return;
        }
        crossingsByVehicle.get(vehicle).add(new ExitEvent(vehicle));
    }

    public void calculateCharges() {
        //crossingsByVehicle.clear();//reset hashmap
        //For each vehicle make sure the crossing are not illegal and then calculate the charges and deduct from account
        for (Map.Entry<VehicleInterface, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            VehicleInterface vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();
            if (!checkOrderingOf(crossings)) {
                operationsService.triggerInvestigationInto(vehicle);
            } else {

                BigDecimal charge = calculateChargeForTimeInZone(crossings);
                try {
                    customerAccountService.deductChargeFrom(vehicle,charge);
                } catch (InsufficientCreditException | AccountNotRegisteredException ex) {
                    operationsService.issuePenaltyNotice(vehicle, charge);
                }
            }
        }

    }

    private double timeFromEpochToHourOfDay(long millis){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date resultantDate = new Date(millis);
        String[] timeOfDay = sdf.format(resultantDate).split(":");
        return (Double.parseDouble(timeOfDay[0])+(Double.parseDouble(timeOfDay[1])/60.0));

    }

     private BigDecimal calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings){
        BigDecimal charge;
        ZoneBoundaryCrossing previousEvent = crossings.get(0);//stores the previous crossing
        ZoneBoundaryCrossing previousEntryEventCharged = crossings.get(0);//stores the last entry event in which you were charged
        long totalTimeInZone = 0;

        if (timeFromEpochToHourOfDay(previousEntryEventCharged.timestamp()) < 14.0){
            charge= new BigDecimal(6.0);//before 2 pm
        }else{
            charge= new BigDecimal(4.0);//2pm onwards will be 4 gbp
        }

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing instanceof ExitEvent) {
                totalTimeInZone += crossing.timestamp()-previousEvent.timestamp();
                //For the case where you weren't charged when you entered but stayed past the 4 hour mark from the previous charge
                if ( (crossing.timestamp()-previousEntryEventCharged.timestamp()) > (4*HOURS_TO_MILLI_SECONDS) ){
                    long tmp_crossing_time = previousEntryEventCharged.timestamp()+(4*HOURS_TO_MILLI_SECONDS);
                    if (timeFromEpochToHourOfDay(tmp_crossing_time)  < 14.0){
                        charge = charge.add(new BigDecimal(6.0));
                    }else{
                        charge = charge.add(new BigDecimal(4.0));
                    }
                    previousEntryEventCharged = new EntryEvent(crossing.getVehicle(),tmp_crossing_time);//create a phantom crossing(entry event) to make the system work
                }

            }else{
                //Add Charges if previous charge was more than 4 hours ago
                if ( (crossing.timestamp()-previousEntryEventCharged.timestamp()) > (4*HOURS_TO_MILLI_SECONDS) ){
                    if (timeFromEpochToHourOfDay(crossing.timestamp()) < 14.0) {
                        charge = charge.add(new BigDecimal(6.0));
                    } else{
                        charge = charge.add(new BigDecimal(4.0));
                    }
                    previousEntryEventCharged = crossing;
                }
            }
            previousEvent = crossing;
        }

         //if total time spent in the zone greater than 4 hours return 12 gbp
        if (totalTimeInZone > (4* HOURS_TO_MILLI_SECONDS)){
            return new BigDecimal(12);
        }

        return charge;
    }

    private boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {

        ZoneBoundaryCrossing previousEvent = crossings.get(0);
        //Even though the vehicleLeavingZone function does not allow for exit events to be registered as the first event, it is always a good idea to perform checks in multiple places
        if (previousEvent instanceof ExitEvent){
            return false;
        }
        //Make sure a vehicle always leaves after entering (the list size is even and every Entry is followed by and Exit)
        if ((crossings.size() % 2) != 0){
            return false;
        }
        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing.timestamp() < previousEvent.timestamp()) {
                return false;
            }
            if (crossing instanceof EntryEvent && previousEvent instanceof EntryEvent) {
                return false;
            }
            if (crossing instanceof ExitEvent && previousEvent instanceof ExitEvent) {
                return false;
            }
            previousEvent = crossing;
        }

        return true;
    }

}
