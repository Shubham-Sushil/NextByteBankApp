package com.nextbyte.bank.controller;

import com.nextbyte.bank.manager.AccountManager;
import com.nextbyte.bank.model.BankAccount;
import com.nextbyte.bank.model.HdfcBankAccount;
import com.nextbyte.bank.model.SbiBankAccount;
import com.nextbyte.bank.service.HdfcAccountService;
import com.nextbyte.bank.service.SbiAccountService;
import com.nextbyte.bank.exception.InvalidAgeException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/openAccount")
public class AccountController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("[DEBUG] AccountServlet: Received POST request for account creation.");

        String bankType = request.getParameter("bankType");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String contactNumber = request.getParameter("contactNumber");
        String accountType = request.getParameter("accountType");
        double balanceAmount = Double.parseDouble(request.getParameter("balanceAmount"));
        LocalDate dob = LocalDate.parse(request.getParameter("dob"));

        BankAccount account;
        AccountManager manager;

        try {
            if ("hdfc".equalsIgnoreCase(bankType)) {
                account = new HdfcBankAccount();
                manager = new AccountManager(new HdfcAccountService());
            } else {
                account = new SbiBankAccount();
                manager = new AccountManager(new SbiAccountService());
            }

            account.setFullName(fullName);
            account.setEmail(email);
            account.setContactNumber(contactNumber);
            account.setAccountType(accountType);
            account.setBalanceAmount(balanceAmount);
            account.setDob(dob);

            manager.openAccount(account);

            request.setAttribute("account", account);
            request.getRequestDispatcher("success.jsp").forward(request, response);

        } catch (InvalidAgeException e) {
            // Catches the checked exception explicitly
            System.err.println("[WARNING] InvalidAgeException caught in servlet: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("form.jsp?bank=" + bankType).forward(request, response);
        }
    }
}