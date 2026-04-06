package com.shopping.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class ManageAdmins {

    public void manageAdmins(Scanner sc, Connection con) {
        System.out.println("\n--- SUPER ADMIN: REGISTER NEW ADMIN ID ---");
        
        System.out.print("Enter New Admin ID: ");
        String adminId = sc.nextLine().trim();
        
        System.out.println("Assign Role: ");
        
        String role = sc.nextLine().toUpperCase().trim();

       
        String sql = "INSERT INTO admin (admin_id, role, admin_name, is_activated, status) VALUES (?, ?, 'New Admin', 0, 'Inactive')";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, adminId);
            ps.setString(2, role);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("SUCCESS: Admin ID [" + adminId + "] is registered.");
                System.out.println("Account is currently Inactive. Admin name is set to 'New Admin' by default.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("ERROR: This Admin ID already exists.");
            } else {
                System.out.println("Database Error: " + e.getMessage());
            }
        }
    }
}