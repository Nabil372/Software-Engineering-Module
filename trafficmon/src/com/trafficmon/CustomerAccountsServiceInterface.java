package com.trafficmon;

import java.math.BigDecimal;

/**
 * Created by Shoaib on 11/19/18.
 */

public interface CustomerAccountsServiceInterface {

    void deductChargeFrom(VehicleInterface vehicle, BigDecimal charge) throws InsufficientCreditException,AccountNotRegisteredException ;
}
