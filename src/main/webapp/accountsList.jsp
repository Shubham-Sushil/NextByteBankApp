<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>All Bank Accounts - NextByte Portal</title>
    <link rel="stylesheet" href="resources/css/style.css">
    <style>
        .table-container {
            width: 100%;
            overflow-x: auto;
            margin-top: 20px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.85rem;
            text-align: left;
        }
        th, td {
            padding: 10px 8px;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #00796b;
            color: white;
            font-weight: 600;
        }
        tr:hover {
            background-color: #f1f8f6;
        }
        .wide-container {
            max-width: 1050px !important;
        }
    </style>
</head>
<body>
    <div class="container wide-container">
        <div class="header">
            <img src="resources/images/NextByte_Logo.png" alt="NextByte Logo" class="logo">
            <h2>All Created Bank Accounts</h2>
        </div>

        <c:choose>
            <c:when test="${empty accountsList}">
                <p style="text-align: center; color: #777; padding: 20px;">No bank accounts have been opened yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Acc No</th>
                                <th>Bank Name</th>
                                <th>Full Name</th>
                                <th>DOB</th>
                                <th>Email</th>
                                <th>Contact</th>
                                <th>Type</th>
                                <th>Balance (₹)</th>
                                <th>Opening Time</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="acc" items="${accountsList}">
                                <tr>
                                    <td><strong>${acc.accountNumber}</strong></td>
                                    <td>${acc.bankName}</td>
                                    <td>${acc.fullName}</td>
                                    <td>${acc.dob}</td>
                                    <td>${acc.email}</td>
                                    <td>${acc.contactNumber}</td>
                                    <td>${acc.accountType}</td>
                                    <td>₹${acc.balanceAmount}</td>
                                    <td><small>${acc.openingTimeIST}</small></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <br>
        <a href="index.html" class="btn" style="max-width: 200px; text-decoration: none; text-align: center;">← Back to Home</a>
    </div>
</body>
</html>