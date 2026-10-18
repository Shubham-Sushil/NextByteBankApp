package com.nextbyte.bank.model;

import java.time.LocalDate;

public abstract class BankAccount {
    private String fullName;
    private String email;
    private String contactNumber;
    private String accountType;
    private double balanceAmount;
    private String accountNumber;
    private String openingTimeIST;
    private LocalDate dob;
    private String bankName;

    public abstract String getBankName();

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(double balanceAmount) { this.balanceAmount = balanceAmount; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getOpeningTimeIST() { return openingTimeIST; }
    public void setOpeningTimeIST(String openingTimeIST) { this.openingTimeIST = openingTimeIST; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public void setBankName(String bankName) { this.bankName = bankName; }
}