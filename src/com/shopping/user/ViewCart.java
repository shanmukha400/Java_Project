package com.shopping.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.shopping.connection.DBConnection;

public class ViewCart {

    public void viewCart(String email) {
        // SQL query to fetch all details including price and quantity
        String sql = "SELECT product_name, quantity, price, size, color FROM cart WHERE user_email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            // Table Header - Width adjusted to 90 characters for the "Reason" column
            String hr = "+------+----------------------+-------+--------+----------+------------+-----------------------+";
            System.out.println("\n" + hr);
            System.out.printf("| %-4s | %-20s | %-5s | %-6s | %-8s | %-10s | %-21s |\n", 
                              "Qty", "Product Name", "Size", "Color", "Price", "Subtotal", "Calculation Reason");
            System.out.println(hr);

            double grandTotal = 0;
            boolean hasItems = false;

            while (rs.next()) {
                hasItems = true;
                String name = rs.getString("product_name");
                String size = (rs.getString("size") != null) ? rs.getString("size") : "-";
                String color = (rs.getString("color") != null) ? rs.getString("color") : "-";
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");

                // Calculation
                double subtotal = price * qty;
                grandTotal += subtotal;

                // Reason/Breakdown text (Helping user understand WHY the subtotal is that amount)
                String reason = String.format("%.2f x %d units", price, qty);

                // Row Printing
                System.out.printf("| %-4d | %-20.20s | %-5.5s | %-6.6s | %-8.2f | %-10.2f | %-21s |\n", 
                                  qty, name, size, color, price, subtotal, reason);
                System.out.println(hr);
            }

            if (!hasItems) {
                System.out.println("|                      Your cart is empty!                                      |");
                System.out.println(hr);
            } else {
                // Footer
                System.out.printf("|%-71s | %-10.2f |\n", " TOTAL PAYABLE AMOUNT", grandTotal);
                System.out.println(hr);
            }
        } catch (SQLException e) {
            System.out.println(" SQL Error: " + e.getMessage());
        }
    }
}