package com.shopping.admin;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    public void manage(Scanner sc) {
        while (true) {
            System.out.println("\n========= ORDER MANAGEMENT =========");
            System.out.println("1. View Detailed Orders\n2. Update Status\n3. Remove Order\n0. Back");
            System.out.print("Select Option: ");

            if (!sc.hasNextInt()) {
                sc.next(); 
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 0) break;

            try (Connection con = DBConnection.getConnection()) {
                switch (choice) {
                    case 1: viewOrders(con); break;
                    case 2: updateStatus(sc, con); break;
                    case 3: removeOrder(sc, con); break;
                    default: System.out.println("Invalid Option!");
                }
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
            }
        }
    }

    private void viewOrders(Connection con) throws SQLException {
        String sql = "SELECT order_id, user_name, total_paid, status,payment_mode, order_date FROM orders";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
System.out.println("\n+----+----------------------+------------+------------------+--------------+---------------------+");
System.out.printf("| %-2s | %-20s | %-10s | %-16s | %-12s | %-19s |\n", 
                  "ID", "Customer Name", "Amount", "Status", "Payment Mode", "Date");
System.out.println("+----+----------------------+------------+------------------+--------------+---------------------+");

while (rs.next()) {
    int id = rs.getInt(1);
    String name = rs.getString(2);
    double amount = rs.getDouble(3);
    String status = rs.getString(4);
    String paymentMode = rs.getString(5);
    String date = rs.getString(6); // '2026-04-05 05:47:03' lanti format

    String displayName = (name.length() > 20) ? name.substring(0, 17) + "..." : name;

    System.out.printf("| %-2d | %-20s | %-10.2f | %-16s | %-12s | %-19s |\n", 
                      id, displayName, amount, status, paymentMode, date);
}
System.out.println("+----+----------------------+------------+------------------+--------------+---------------------+");
        }
    }

    private void updateStatus(Scanner sc, Connection con) throws SQLException {
        System.out.print("Enter Order ID: ");
        int id = sc.nextInt(); 
        sc.nextLine();
        System.out.print("New Status (Pending/Shipped/Delivered): ");
        String status = sc.nextLine();

        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            if (ps.executeUpdate() > 0) System.out.println(" Status Updated!");
            else System.out.println(" Order ID not found!");
        }
    }

    private void removeOrder(Scanner sc, Connection con) throws SQLException {
        System.out.print("Enter Order ID to remove: ");
        int id = sc.nextInt(); 
        sc.nextLine();
        
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) System.out.println(" Order Removed!");
            else System.out.println(" Order not found!");
        }
    }
}