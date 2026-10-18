package com.nextbyte.bank.service;

import com.nextbyte.bank.model.BankAccount;
import com.nextbyte.bank.exception.InvalidAgeException;

public interface BankService {
    void processAccount(BankAccount account) throws InvalidAgeException;
}