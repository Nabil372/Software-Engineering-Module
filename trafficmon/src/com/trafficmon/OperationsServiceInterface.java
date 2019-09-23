package com.trafficmon;

import java.math.BigDecimal;

public interface OperationsServiceInterface {
    void issuePenaltyNotice(VehicleInterface vehicle, BigDecimal charge);
    void triggerInvestigationInto(VehicleInterface vehicle);
}
