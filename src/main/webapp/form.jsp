<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Open Account - NextByte Bank</title>
    <link rel="stylesheet" href="resources/css/style.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <img src="resources/images/NextByte_Logo.png" alt="NextByte Logo" class="logo">
            <h2><c:out value="${param.bank eq 'hdfc' ? 'HDFC Bank Account Form' : 'SBI Bank Account Form'}"/></h2>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="error-banner">
                <strong>Age Validation Error:</strong> ${errorMessage}
            </div>
        </c:if>

        <form action="openAccount" method="POST">
            <input type="hidden" name="bankType" value="${param.bank}">

            <div class="form-group">
                <label>Full Name:</label>
                <input type="text" name="fullName" required placeholder="Enter full name">
            </div>

            <div class="form-group">
                <label>Date of Birth (DOB):</label>
                <input type="date" name="dob" required>
            </div>

            <div class="form-group">
                <label>Email Address:</label>
                <input type="email" name="email" required placeholder="name@example.com">
            </div>

            <div class="form-group">
                <label>Contact Number:</label>
                <input type="tel" name="contactNumber" required placeholder="9876543210">
            </div>

            <div class="form-group">
                <label>Account Type:</label>
                <select name="accountType" required>
                    <option value="">Select Account Type</option>
                    <option value="Savings">Savings Account</option>
                    <option value="Current">Current Account</option>
                </select>
            </div>

            <div class="form-group">
                <label>Initial Deposit Amount (₹):</label>
                <input type="number" step="0.01" name="balanceAmount" required placeholder="5000">
            </div>

            <button type="submit" class="btn">Submit & Open Account</button>
        </form>

        <a href="index.html" class="back-link">← Back to Home</a>
    </div>
</body>
</html>