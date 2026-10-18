package com.nextbyte.bank.manager;

import com.nextbyte.bank.model.BankAccount;
import com.nextbyte.bank.service.BankService;
import com.nextbyte.bank.exception.InvalidAgeException;

public class AccountManager {
    private final BankService bankService;

    public AccountManager(BankService bankService) {
        this.bankService = bankService;
    }

    public void openAccount(BankAccount account) throws InvalidAgeException {
        bankService.processAccount(account);
    }
}