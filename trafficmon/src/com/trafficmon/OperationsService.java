package com.trafficmon;

import java.math.BigDecimal;

public class OperationsService implements OperationsServiceInterface {
    private PenaltiesService OpTeam = OperationsTeam.getInstance();

    @Override
    public void issuePenaltyNotice(VehicleInterface vehicle, BigDecimal charge) {
        OpTeam.issuePenaltyNotice((Vehicle) vehicle,charge);
    }

    @Override
    public void triggerInvestigationInto(VehicleInterface vehicle){
        OpTeam.triggerInvestigationInto((Vehicle) vehicle);
    }


}
