package com.shopping.user;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Checkout {

    public void processCheckout(Scanner sc, String email, String currentUsername) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); 

            displayDetailedCart(con, email);
            
            double cartTotal = getCartTotal(con, email);
            if (cartTotal <= 0) {
                System.out.println(" Your cart is empty!");
                return;
            }

            double discount = handleCoupons(sc, con, cartTotal);
            double finalBill = cartTotal - discount;

            System.out.println("\n==================================================");
            System.out.printf("%-30s : Rs %10.2f\n", "CART SUBTOTAL", cartTotal);
            System.out.printf("%-30s : -Rs %9.2f\n", "COUPON DISCOUNT", discount);
            System.out.println("--------------------------------------------------");
            System.out.printf("%-30s : Rs %10.2f\n", "FINAL AMOUNT TO PAY", finalBill);
            System.out.println("==================================================");

            System.out.println("\n SELECT PAYMENT METHOD:");
            System.out.println("1. UPI\n2. Card\n3. Cash on Delivery (COD)");
            System.out.print(" Selection: ");
            int payMethod = Integer.parseInt(sc.nextLine()); // Buffer issue avoid cheyadaniki parseNextLine best

            String paymentMode = "";
            String txnId = "N/A";
            boolean isPaid = false;

            if (payMethod == 1) { 
                paymentMode = "UPI";
                System.out.print(" Enter UPI ID: "); String upiId = sc.nextLine().trim();
                System.out.print(" Enter 4-Digit PIN: "); String upiPin = sc.nextLine().trim();
                isPaid = processUPIPayment(con, email, upiId, upiPin, finalBill);
            } 
            else if (payMethod == 2) { 
                paymentMode = "CARD";
                System.out.print(" Card Number: "); String cardNum = sc.nextLine().trim();
                System.out.print(" CVV: "); String cvv = sc.nextLine().trim();
                isPaid = processCardPayment(con, email, cardNum, cvv, finalBill);
            } 
            else if (payMethod == 3) { 
                paymentMode = "COD";
                isPaid = true;
            }

            if (isPaid) {
                if (!paymentMode.equals("COD")) txnId = "TXN" + System.currentTimeMillis();
                String summary = getItemsSummary(con, email);
                int orderId = createOrderEntry(con, currentUsername, summary, finalBill, paymentMode, txnId);
                System.out.println("\n Payment successful! Processing your order...");
                updateProductStockAndSales(con, email); 
                clearUserCart(con, email);
                con.commit(); 
                
                printReceipt(orderId, currentUsername, finalBill, paymentMode, txnId);
                System.out.print("\n Would you like to rate us? (y/n): ");
                if (sc.nextLine().equalsIgnoreCase("y")) captureUserReview(sc, con, currentUsername);
                System.out.println("\n STATUS: ORDER_CONFIRMED");
            } else {
                con.rollback();
                System.out.println(" Checkout failed due to payment issues.");
            }

        } catch (Exception e) {
            System.out.println(" System Error: " + e.getMessage());
            try { if(con != null) con.rollback(); } catch(SQLException ex) {}
        }
    }

private void displayDetailedCart(Connection con, String email) throws SQLException {
    
    String sql = "SELECT product_name, price, quantity, size, color FROM cart WHERE user_email = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        double grandTotal = 0;
        boolean hasItems = false;

        String border = "+-----+---------------------------+--------+----------+----------+------------+";
        System.out.println("\n--- Checkout: Item Wise Billing ---");
        System.out.println(border);
        System.out.printf("| %-3s | %-25s | %-6s | %-8s | %-8s | %-10s |\n", 
                          "Qty", "Product Name", "Size", "Color", "Price", "Subtotal");
        System.out.println(border);

        while (rs.next()) {
            hasItems = true;
            
            int qty = rs.getInt("quantity"); 
            double unitPrice = rs.getDouble("price"); // Direct price from cart table
            double subTotal = unitPrice * qty;
            grandTotal += subTotal;

            System.out.printf("| %-3d | %-25s | %-6s | %-8s | %-8.2f | %-10.2f |\n",
                              qty, 
                              rs.getString("product_name"), 
                              rs.getString("size"), 
                              rs.getString("color"), 
                              unitPrice, 
                              subTotal);
        }

        if (!hasItems) {
            System.out.println("|                   Your cart is empty!                               |");
        } else {
            System.out.println(border);
            // Total Payable alignment
            System.out.printf("| %-59s | %-10.2f |\n", "TOTAL PAYABLE AMOUNT", grandTotal);
        }
        System.out.println(border);
    }
}


    private boolean processUPIPayment(Connection con, String email, String upiId, String pin, double bill) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE upi_id = ? AND upi_pin = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setString(1, upiId); ps.setString(2, pin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double currentBal = rs.getDouble("balance");
                if (currentBal >= bill) {
                    String update = "UPDATE accounts SET balance = balance - ? WHERE upi_id = ?";
                    try (PreparedStatement ups = con.prepareStatement(update)) {
                        ups.setDouble(1, bill); ups.setString(2, upiId);
                        ups.executeUpdate();
                        return true;
                    }
                } else { System.out.println(" INSUFFICIENT BALANCE!"); }
            } else { System.out.println(" INVALID UPI ID or PIN!"); }
        }
        return false;
    }

    private boolean processCardPayment(Connection con, String email, String cardNum, String cvv, double bill) throws SQLException {
        String sql = "SELECT balance FROM card_details WHERE card_number = ? AND cvv = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cardNum); ps.setString(2, cvv);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double currentBal = rs.getDouble("balance");
                if (currentBal >= bill) {
                    String update = "UPDATE card_details SET balance = balance - ? WHERE card_number = ?";
                    try (PreparedStatement ups = con.prepareStatement(update)) {
                        ups.setDouble(1, bill); ups.setString(2, cardNum);
                        ups.executeUpdate();
                        return true;
                    }
                } else { System.out.println(" INSUFFICIENT BALANCE!"); }
            } else { System.out.println(" INVALID CARD DETAILS!"); }
        }
        return false;
    }

    private String getItemsSummary(Connection con, String email) throws SQLException {
        List<String> items = new ArrayList<>();
        String sql = "SELECT product_name, quantity FROM cart WHERE user_email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { items.add(rs.getString(1) + " (x" + rs.getInt(2) + ")"); }
        }
        return String.join(", ", items);
    }

    private int createOrderEntry(Connection con, String name, String summary, double bill, String mode, String txn) throws SQLException {
        String sql = "INSERT INTO orders (user_name, order_date, items_summary, total_paid, payment_mode, txn_id, status) " +
                     "VALUES (?, NOW(), ?, ?, ?, ?, 'ORDER_CONFIRMED')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, summary); ps.setDouble(3, bill); 
            ps.setString(4, mode); ps.setString(5, txn);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double getCartTotal(Connection con, String email) throws SQLException {
   
    String sql = "SELECT SUM(total_price) FROM (" +
                 "  SELECT MIN(price) * MIN(quantity) as total_price " +
                 "  FROM cart WHERE user_email = ? " +
                 "  GROUP BY product_name" +
                 ") as subquery";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getDouble(1);
        }
    }
    return 0.0;
}

    private double handleCoupons(Scanner sc, Connection con, double total) throws SQLException {
        System.out.println("\n AVAILABLE COUPONS:");
        String sql = "SELECT coupon_code, discount_percentage FROM coupons WHERE min_order_value <= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, total);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("- CODE: " + rs.getString(1) + " | SAVE: " + rs.getDouble(2) + "%");
            }
            if(!found) System.out.println("   (No coupons available for this amount)");
        }
        System.out.print("\n Enter Coupon Code (Enter to skip): ");
        String code = sc.nextLine().trim();
        if (code.isEmpty()) return 0;
        
        String check = "SELECT discount_percentage FROM coupons WHERE coupon_code = ? AND min_order_value <= ?";
        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setString(1, code); ps.setDouble(2, total);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                double perc = rs.getDouble(1);
                return (total * perc / 100);
            }
        }
        System.out.println(" Invalid Coupon!");
        return 0;
    }

    private void updateProductStockAndSales(Connection con, String email) throws SQLException {
        String sql = "UPDATE product p JOIN cart c ON p.product_name = c.product_name " +
                     "SET p.stock = p.stock - c.quantity, p.sales_count = IFNULL(p.sales_count, 0) + c.quantity " +
                     "WHERE c.user_email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email); ps.executeUpdate();
        }
    }

    private void clearUserCart(Connection con, String email) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM cart WHERE user_email = ?")) {
            ps.setString(1, email); ps.executeUpdate();
        }
    }

    private void printReceipt(int id, String name, double bill, String mode, String txn) {
        System.out.println("\n*********************************************");
        System.out.println("              FINAL ORDER RECEIPT");
        System.out.println("*********************************************");
        System.out.printf("%-18s : #ORD-%d\n", "ORDER ID", id);
        System.out.printf("%-18s : %s\n", "CUSTOMER NAME", name);
        System.out.printf("%-18s : %s\n", "PAYMENT MODE", mode);
        if (!mode.equals("COD")) System.out.printf("%-18s : %s\n", "TXN ID", txn);
        System.out.printf("%-18s : Rs %.2f\n", "TOTAL PAID", bill);
        System.out.println("*********************************************");
    }

    private void captureUserReview(Scanner sc, Connection con, String username) throws SQLException {
        System.out.print(" Product Name for review: "); String pName = sc.nextLine();
        System.out.print(" Stars (1-5): "); 
        int stars = Integer.parseInt(sc.nextLine());
        System.out.print(" Feedback: "); String feed = sc.nextLine();
        String sql = "INSERT INTO reviews (username, product_name, rating, feedback, review_date) VALUES (?,?,?,?,NOW())";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, pName); ps.setInt(3, stars); ps.setString(4, feed);
            ps.executeUpdate();
            System.out.println(" Feedback saved!");
        }
    }
}