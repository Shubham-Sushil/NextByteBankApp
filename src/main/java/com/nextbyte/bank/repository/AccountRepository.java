package com.nextbyte.bank.repository;

import com.nextbyte.bank.model.BankAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountRepository {
    private static final List<BankAccount> accountDatabase = Collections.synchronizedList(new ArrayList<>());

    public static void addAccount(BankAccount account) {
        accountDatabase.add(account);
    }

    public static List<BankAccount> getAllAccounts() {
        synchronized (accountDatabase) {
            return new ArrayList<>(accountDatabase);
        }
    }
}