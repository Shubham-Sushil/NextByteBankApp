<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Success - NextByte Bank</title>
    <link rel="stylesheet" href="resources/css/style.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <img src="resources/images/NextByte_Logo.png" alt="NextByte Logo" class="logo">
            <h2>${account.bankName}</h2>
        </div>

        <div class="success-banner">
            <h3>🎉 Account Successfully Opened!</h3>
            <p>Congratulations, <strong>${account.fullName}</strong>! Your account has been created.</p>
        </div>

        <p><strong>Account Number:</strong> ${account.accountNumber}</p>
        <p><strong>Account Type:</strong> ${account.accountType}</p>
        <p><strong>Email:</strong> ${account.email}</p>
        <p><strong>Contact Number:</strong> ${account.contactNumber}</p>
        <p><strong>Date of Birth:</strong> ${account.dob}</p>
        <p><strong>Current Balance:</strong> ₹${account.balanceAmount}</p>
        <p><strong>Account Opening Time:</strong> ${account.openingTimeIST}</p>

        <a href="index.html" class="btn" style="text-align: center; text-decoration: none; margin-top: 20px;">Open Another Account</a>
    </div>
</body>
</html>