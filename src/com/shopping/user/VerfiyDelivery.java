package com.shopping.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class VerfiyDelivery {
   public void verifyUserDelivery(Scanner sc, Connection con, String currentUserName) {
    System.out.println("\n--- ORDER DELIVERY VERIFICATION ---");
    System.out.print("Enter Order ID: ");
    
    int orderId;
    try {
        orderId = Integer.parseInt(sc.nextLine().trim());
    } catch (NumberFormatException e) {
        System.out.println("Invalid Order ID format.");
        return;
    }

    // Status kuda fetch chestunnam check cheyadaniki
    String sql = "SELECT payment_mode, txn_id, status FROM orders WHERE order_id = ? AND user_name = ?";
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, orderId);
        ps.setString(2, currentUserName);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            String mode = rs.getString("payment_mode");
            String dbTxnId = rs.getString("txn_id");
            String status = rs.getString("status");

            // --- IMPORTANT STATUS CHECKS ---
            if ("Verified".equalsIgnoreCase(status)) {
                System.out.println("This order is already verified.");
                return;
            }
            
            if ("Cancelled".equalsIgnoreCase(status)) {
                System.out.println("Cannot verify: This order has been CANCELLED.");
                return;
            }

            if ("Returned".equalsIgnoreCase(status)) {
                System.out.println("Cannot verify: This order was RETURNED.");
                return;
            }

            // Kevalam 'Pending' leda 'Placed' status unte ne mundhuki velthundi
            if (!"Pending".equalsIgnoreCase(status) && !"Placed".equalsIgnoreCase(status)) {
                System.out.println("Verification not allowed for current status: " + status);
                return;
            }

            // Online Payment check
            if ("Online".equalsIgnoreCase(mode)) {
                System.out.print("Enter Transaction ID: ");
                String inputTxnId = sc.nextLine().trim();
                
                if (!inputTxnId.equalsIgnoreCase(dbTxnId)) {
                    System.out.println("Transaction ID mismatch. Verification failed.");
                    return;
                }
            }

            // OTP Generation
            int generatedOtp = (int)(Math.random() * 9000) + 1000;
            System.out.println("-------------------------");
            System.out.println("OTP: " + generatedOtp);
            System.out.println("-------------------------");
            
            System.out.print("Enter OTP: ");
            String inputOtpStr = sc.nextLine().trim();

            if (inputOtpStr.equals(String.valueOf(generatedOtp))) {
                String updateSql = "UPDATE orders SET status = 'Verified' WHERE order_id = ?";
                try (PreparedStatement ups = con.prepareStatement(updateSql)) {
                    ups.setInt(1, orderId);
                    int rows = ups.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Order Status Updated to: VERIFIED");
                    }
                }
            } else {
                System.out.println("Incorrect OTP.");
            }

        } else {
            System.out.println("Order not found for this user.");
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}
}