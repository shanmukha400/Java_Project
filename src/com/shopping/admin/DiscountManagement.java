package com.shopping.admin;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class DiscountManagement {
    
    

    public void manageCoupons(Scanner sc) {
        while (true) {
            System.out.println("\n====================================================");
            System.out.println("         SEASONAL DISCOUNT & COUPON CONTROL");
            System.out.println("====================================================");
            System.out.println("1. View All Active Coupons ");
            System.out.println("2. Add New Seasonal Coupon");
            System.out.println("3. Update Coupon Details");
            System.out.println("4. Remove Coupon");
            System.out.println("0. Back");
            System.out.print("Select Option: ");
            
            if (!sc.hasNextInt()) { sc.next(); continue; }
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 0) break;

            try (Connection con = DBConnection.getConnection()) {
                switch (choice) {
                    case 1: viewCoupons(con); break;
                    case 2: addCoupon(sc, con); break;
                    case 3: updateCoupon(sc, con); break;
                    case 4: removeCoupon(sc, con); break;
                    default: System.out.println(" Invalid Selection!");
                }
            } catch (SQLException e) {
                System.out.println(" Database Error: " + e.getMessage());
            }
        }
    }

    private void viewCoupons(Connection con) throws SQLException {
        String sql = "SELECT * FROM coupons";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        // Table Header
        System.out.println("\n+--------------+----------------------+--------+--------------+------------+");
        System.out.printf("| %-12s | %-20s | %-6s | %-12s | %-10s |\n", 
                          "COUPON CODE", "SEASON NAME", "DISC%", "MIN ORDER", "EXPIRY");
        System.out.println("+--------------+----------------------+--------+--------------+------------+");

        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("| %-12s | %-20s | %-5d%% | %-12.2f | %-10s |\n", 
                rs.getString("coupon_code"), 
                rs.getString("season_name"),
                rs.getInt("discount_percentage"),
                rs.getDouble("min_order_value"),
                rs.getDate("expiry_date"));
        }

        if (!found) {
            System.out.println("|                 No active coupons found in the database.                 |");
        }
        System.out.println("+--------------+----------------------+--------+--------------+------------+");
    }

    private void addCoupon(Scanner sc, Connection con) throws SQLException {
        System.out.println("\n--- Create New Seasonal Coupon ---");
        System.out.print("Season Name: "); String season = sc.nextLine();
        System.out.print("Coupon Code: "); String code = sc.next().toUpperCase();
        System.out.print("Discount %: "); int disc = sc.nextInt();
        System.out.print("Min Shopping Amount: "); double minOrder = sc.nextDouble();
        System.out.print("Expiry (YYYY-MM-DD): "); String expiry = sc.next();

        String sql = "INSERT INTO coupons (coupon_code, season_name, discount_percentage, min_order_value, expiry_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, season);
            ps.setInt(3, disc);
            ps.setDouble(4, minOrder);
            ps.setString(5, expiry);
            ps.executeUpdate();
            System.out.println(" Coupon '" + code + "' for " + season + " added!");
        }
    }

    private void updateCoupon(Scanner sc, Connection con) throws SQLException {
        System.out.print("\nEnter Code to Update: ");
        String code = sc.next().toUpperCase();
        System.out.print("New Discount %: "); int disc = sc.nextInt();
        System.out.print("New Min Order: "); double minVal = sc.nextDouble();

        String sql = "UPDATE coupons SET discount_percentage = ?, min_order_value = ? WHERE coupon_code = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, disc);
            ps.setDouble(2, minVal);
            ps.setString(3, code);
            if (ps.executeUpdate() > 0) System.out.println(" Updated!");
            else System.out.println(" Not found.");
        }
    }

    private void removeCoupon(Scanner sc, Connection con) throws SQLException {
        System.out.print("\nEnter Code to Remove: ");
        String code = sc.next().toUpperCase();
        String sql = "DELETE FROM coupons WHERE coupon_code = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            if (ps.executeUpdate() > 0) System.out.println(" Removed.");
            else System.out.println(" Not found.");
        }
    }
}