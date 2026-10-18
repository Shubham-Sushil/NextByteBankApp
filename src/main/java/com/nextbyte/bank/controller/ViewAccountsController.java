package com.nextbyte.bank.controller;

import com.nextbyte.bank.repository.AccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/viewAccounts")
public class ViewAccountsController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int totalAccounts = AccountRepository.getAllAccounts().size();
        System.out.println("[DEBUG] ViewAccountsServlet: Fetching collection list. Total accounts in memory: " + totalAccounts);

        request.setAttribute("accountsList", AccountRepository.getAllAccounts());
        request.getRequestDispatcher("accountsList.jsp").forward(request, response);
    }
}