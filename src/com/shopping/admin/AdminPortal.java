package com.shopping.admin;

import java.sql.*;
import java.util.Scanner;

public class AdminPortal implements IAdminAuth {

    // Main Entry Point for Admin Section
    public void showAdminMenu(Scanner sc, Connection con) {
        while (true) {
            System.out.println("\n===========================================================");
            System.out.println("                   ADMIN ACCESS PORTAL                     ");
            System.out.println("===========================================================");
            System.out.println("1. Login");
            System.out.println("2. Sign Up ");
            System.out.println("3. Forgot Password");
            System.out.println("4. Change Password");
            System.out.println("0. Back");
            System.out.print(" Choice: ");

            String choiceStr = sc.nextLine().trim();
            if (choiceStr.isEmpty()) continue;
            if (choiceStr.equals("0")) return;

            switch (choiceStr) {
                case "1": adminLogin(sc, con); break;
                case "2": adminSignUp(sc, con); break;
                case "3": adminForgotPassword(sc, con); break;
                case "4": adminChangePassword(sc, con); break;
                default: System.out.println(" Invalid Choice!");
            }
        }
    }

    @Override
    public void adminLogin(Scanner sc, Connection con) {
        System.out.println("\n---  ADMIN LOGIN ---");
        System.out.print(" Admin ID: "); String aid = sc.nextLine().trim();
        System.out.print(" Password: "); String pass = sc.nextLine().trim();

        String sql = "SELECT admin_name, role FROM admin WHERE admin_id = ? AND password = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, aid);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String name = rs.getString("admin_name");
                String role = rs.getString("role");
                System.out.println("\n Welcome, Admin " + name);
                
                // Successful Login ayyaka Dashboard ki pampali
                new AdminDashboard().showAdminDashboard(sc, name, role); 
            } else {
                System.out.println(" Invalid ID or Password!");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
   public void adminSignUp(Scanner sc, Connection con) {
    System.out.println("\n--- ADMIN SIGN UP ---");
    System.out.print("Admin ID: "); String aid = sc.nextLine().trim();
    System.out.print("Full Name: "); String name = sc.nextLine().trim();
    System.out.print("Password: "); String pass = sc.nextLine().trim();
    System.out.print("Phone Number: "); String phone = sc.nextLine().trim();
    System.out.print("Role (PRODUCT/ORDER/USER): "); String role = sc.nextLine().trim().toUpperCase();

    // Query: admin_name, password, phone_number, role, is_activated, status update chestunnam
    String sql = "UPDATE admin SET admin_name = ?, password = ?, phone_number = ?, role = ?, is_activated = 1, status = 'Active' WHERE admin_id = ? AND is_activated = 0";
    
    try (PreparedStatement pst = con.prepareStatement(sql)) {
        pst.setString(1, name);
        pst.setString(2, pass);
        pst.setString(3, phone);
        pst.setString(4, role);
        pst.setString(5, aid);
        
        int rows = pst.executeUpdate();
        if (rows > 0) {
            System.out.println(" Registered & Activated Successfully!");
        } else {
            System.out.println(" Error: Invalid ID or already activated.");
        }
    } catch (SQLException e) {
        System.out.println(" Database issue: " + e.getMessage());
    }
}

    @Override
    public void adminForgotPassword(Scanner sc, Connection con) {
        System.out.println("\n---  FORGOT PASSWORD ---");
        System.out.print(" Enter Admin ID: "); String aid = sc.nextLine().trim();
        System.out.print(" Confirm Admin Name: "); String name = sc.nextLine().trim();

        String sql = "SELECT password FROM admin WHERE admin_id = ? AND admin_name = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, aid);
            pst.setString(2, name);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println(" Your Password is: " + rs.getString("password"));
            } else {
                System.out.println(" Verification Failed! Details mismatch.");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void adminChangePassword(Scanner sc, Connection con) {
        System.out.println("\n---  CHANGE PASSWORD ---");
        System.out.print(" Admin ID: "); String aid = sc.nextLine().trim();
        System.out.print(" Old Password: "); String oldPass = sc.nextLine().trim();
        System.out.print(" New Password: "); String newPass = sc.nextLine().trim();

        String sql = "UPDATE admin SET password = ? WHERE admin_id = ? AND password = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, newPass);
            pst.setString(2, aid);
            pst.setString(3, oldPass);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println(" Password Updated Successfully!");
            } else {
                System.out.println(" Incorrect ID or Old Password!");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}