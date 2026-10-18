package com.nextbyte.bank.service;

import com.nextbyte.bank.model.BankAccount;
import com.nextbyte.bank.repository.AccountRepository;
import com.nextbyte.bank.exception.InvalidAgeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractBankService implements BankService {
    protected static final AtomicInteger counter = new AtomicInteger(1000);
    protected static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Override
    public void processAccount(BankAccount account) throws InvalidAgeException {
        System.out.println("[DEBUG] AbstractBankService: Starting account processing for -> " + account.getFullName());

        if (account.getDob() != null) {
            int age = Period.between(account.getDob(), LocalDate.now()).getYears();
            System.out.println("[DEBUG] Calculated age for " + account.getFullName() + ": " + age + " years.");
            if (age < 18) {
                System.err.println("[ERROR] Checked exception triggered! User age is " + age + " (Minimum required is 18).");
                // Throws checked exception
                throw new InvalidAgeException("Account creation failed: User must be at least 18 years old. Current calculated age is " + age + " years.");
            }
        } else {
            System.err.println("[ERROR] Date of Birth is null.");
            throw new InvalidAgeException("Account creation failed: Date of Birth is required.");
        }

        String generatedAccNo = generateAccountNumber();
        String timestamp = ZonedDateTime.now(IST_ZONE).format(FORMATTER);
        String bankName = getBankSpecificName();

        account.setAccountNumber(generatedAccNo);
        account.setOpeningTimeIST(timestamp);
        account.setBankName(bankName);

        applyBankSpecificLogic(account);
        AccountRepository.addAccount(account);
        System.out.println("[DEBUG] Account added to repository. Total accounts: " + AccountRepository.getAllAccounts().size());
    }

    protected String generateAccountNumber() {
        return String.valueOf(counter.incrementAndGet());
    }

    public abstract String getBankSpecificName();
    public abstract void applyBankSpecificLogic(BankAccount account);
}