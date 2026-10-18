package com.nextbyte.bank.model;

public class HdfcBankAccount extends BankAccount {
    @Override
    public String getBankName() {
        return "HDFC Bank";
    }
}