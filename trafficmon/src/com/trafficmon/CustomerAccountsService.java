package com.trafficmon;

import java.math.BigDecimal;

/**
 * Created by Shoaib on 11/19/18.
 */

public class CustomerAccountsService implements CustomerAccountsServiceInterface {
    private AccountsService registeredCustomerAccountsService = RegisteredCustomerAccountsService.getInstance();

    @Override
    public void deductChargeFrom(VehicleInterface vehicle, BigDecimal charge) throws InsufficientCreditException,AccountNotRegisteredException {
        registeredCustomerAccountsService.accountFor((Vehicle) vehicle).deduct(charge);
    }
}
