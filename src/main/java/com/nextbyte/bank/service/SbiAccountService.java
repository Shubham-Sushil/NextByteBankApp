package com.nextbyte.bank.service;

import com.nextbyte.bank.model.BankAccount;

public class SbiAccountService extends AbstractBankService {
    @Override
    public String getBankSpecificName() {
        return "State Bank of India (SBI)";
    }

    @Override
    public void applyBankSpecificLogic(BankAccount account) {
        System.out.println("[LOG] Executing SBI-specific business logic and hooks for: " + account.getFullName());
    }
}