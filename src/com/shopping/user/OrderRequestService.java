package com.shopping.user;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class OrderRequestService {

    public static void handleOrderRequests(Scanner sc, String email) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("\n--- ORDER REQUESTS ---");
            System.out.println("1. Cancel Order\n2. Return Product\n3. Exchange Product");
            System.out.print("Select an option: ");
            
            String choiceStr = sc.nextLine().trim();
            if (choiceStr.isEmpty()) return;
            
            int choice = Integer.parseInt(choiceStr);

            System.out.print("Enter Order ID : ");
            String rawId = sc.nextLine().trim();
            int orderId = Integer.parseInt(rawId.replaceAll("[^0-9]", ""));

            OrderRequestService ors = new OrderRequestService();
            switch (choice) {
                case 1: ors.cancelOrder(con, orderId, sc); break;
                case 2: ors.returnProduct(con, orderId, sc); break;
                case 3: ors.exchangeProduct(con, orderId, sc); break;
                default: System.out.println("Invalid option!");
            }
        } catch (Exception e) {
            System.out.println(" Input Error: Please enter valid numbers.");
        }
    }

    public void cancelOrder(Connection con, int orderId, Scanner sc) throws SQLException {
        String checkSql = "SELECT payment_mode, status, user_name FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String currentStatus = rs.getString("status");
                if (currentStatus.equalsIgnoreCase("Cancelled")) {
                    System.out.println(" Already cancelled."); return;
                }
                
                System.out.println("\n--- Cancel Reason ---");
                System.out.println("1. Mistake / Ordered by mistake");
                System.out.println("2. Delay / Delivery taking too long");
                System.out.println("3. Price / Found at lower price elsewhere");
                System.out.println("4. Other (Please specify)");
                int opt = Integer.parseInt(sc.nextLine().trim());
                
                String reason = getReason(opt, "Cancel", sc);
                processUpdate(con, orderId, "Cancelled", reason, rs.getString("payment_mode"));
            } else {
                System.out.println(" Invalid Order ID.");
            }
        }
    }

    public void returnProduct(Connection con, int orderId, Scanner sc) throws SQLException {
        String sql = "SELECT payment_mode FROM orders WHERE order_id = ? AND status = 'Delivered'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\n--- Return Reason ---");
                System.out.println("1. Quality\n2. Wrong Item\n3. Damaged\n4. Other");
                System.out.print("Choice: ");
                int opt = Integer.parseInt(sc.nextLine().trim());
                
                String reason = getReason(opt, "Return", sc);
                processUpdate(con, orderId, "Returned", reason, rs.getString("payment_mode"));
            } else {
                System.out.println(" Only 'Delivered' orders can be returned.");
            }
        }
    }

    public void exchangeProduct(Connection con, int orderId, Scanner sc) throws SQLException {
        System.out.print("\n Exchange Reason (Size/Color): ");
        String reason = sc.nextLine().trim();

        String sql = "UPDATE orders SET status = 'Exchange Initiated' WHERE order_id = ? AND status = 'Delivered'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            if (ps.executeUpdate() > 0) {
                System.out.println(" Exchange Initiated!");
            } else {
                System.out.println(" Order not eligible(Order Must be delivered to exchange).");
            }
        }
    }

    private String getReason(int opt, String type, Scanner sc) {
        if (opt == 4) {
            System.out.print(" Enter reason: ");
            return sc.nextLine().trim();
        }
        return (type.equals("Cancel")) ? "Order Mistake" : "Quality Issue";
    }

    private void processUpdate(Connection con, int orderId, String status, String reason, String payMode) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
            
            System.out.println("\n Order " + status + " successfully!");
            if (!payMode.equalsIgnoreCase("COD")) {
                System.out.println(" Refund of Rs. " + reason + " (Ref) Initiated.");
            }
        }
    }
}