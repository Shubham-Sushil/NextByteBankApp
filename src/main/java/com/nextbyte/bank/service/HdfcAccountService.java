package com.nextbyte.bank.service;

import com.nextbyte.bank.model.BankAccount;

public class HdfcAccountService extends AbstractBankService {
    @Override
    public String getBankSpecificName() {
        return "HDFC Bank";
    }

    @Override
    public void applyBankSpecificLogic(BankAccount account) {
        System.out.println("[LOG] Executing HDFC-specific business logic and hooks for: " + account.getFullName());
    }
}