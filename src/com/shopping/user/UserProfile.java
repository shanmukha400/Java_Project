package com.shopping.user;
import com.shopping.connection.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserProfile {

    // Method ni define chesa - deeniki Scanner mariyu Email pass cheyali
    public void viewAndEditProfile(Scanner sc, String email) {
    try (Connection con = DBConnection.getConnection()) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    String dbUser = rs.getString("username");
                    String dbGender = rs.getString("gender");
                    String dbPhone = rs.getString("phone");
                    String dbAddress = rs.getString("address");

                    System.out.println("\n👤 ======= [ YOUR PROFILE ] ======= 👤");
                    System.out.println("Username     : " + dbUser);
                    System.out.println("Email        : " + email);
                    System.out.println("Gender       : " + (dbGender == null ? "Not Set" : dbGender));
                    System.out.println("Phone Number : " + (dbPhone == null ? "Not Set" : dbPhone));
                    System.out.println("Address      : " + (dbAddress == null ? "Not Set" : dbAddress));
                    System.out.println("----------------------------------------");

                    System.out.print(" Edit profile? (y/n): ");
                    String choice = sc.next();
                    sc.nextLine();

                    if (choice.equalsIgnoreCase("y")) {
                        System.out.println("\n(Press 'Enter' to keep the current value)");

                        // Username Edit
                        System.out.print("New Username [" + dbUser + "]: ");
                        String inputName = sc.nextLine().trim();
                        String finalName = inputName.isEmpty() ? dbUser : inputName;

                        
                        System.out.print("New Gender [" + (dbGender == null ? "" : dbGender) + "]: ");
                        String inputGender = sc.nextLine().trim();
                        String finalGender = inputGender.isEmpty() ? dbGender : inputGender;

                        
                        System.out.print("New Phone (10 digits) [" + (dbPhone == null ? "" : dbPhone) + "]: ");
                        String pInput = sc.nextLine().trim();
                        String finalPhone = dbPhone; 
                        
                        if (!pInput.isEmpty()) {
                            if (pInput.length() == 10 && pInput.matches("\\d+")) {
                                finalPhone = "+91" + pInput;
                            } else {
                                System.out.println(" Invalid phone format! Keeping old value.");
                            }
                        }

                        // Address Edit
                        System.out.print("New Address [" + (dbAddress == null ? "" : dbAddress) + "]: ");
                        String inputAddr = sc.nextLine().trim();
                        String finalAddr = inputAddr.isEmpty() ? dbAddress : inputAddr;

                        // Step 3: Database Update
                        String updateSql = "UPDATE users SET username=?, gender=?, phone=?, address=? WHERE email=?";
                        try (PreparedStatement psUp = con.prepareStatement(updateSql)) {
                            psUp.setString(1, finalName);
                            psUp.setString(2, finalGender);
                            psUp.setString(3, finalPhone);
                            psUp.setString(4, finalAddr);
                            psUp.setString(5, email);
                            
                            if (psUp.executeUpdate() > 0) {
                                System.out.println(" SUCCESS: Profile updated successfully!");
                            } else {
                                System.out.println(" No changes were made.");
                            }
                        }
                    }
                } else {
                    System.out.println(" User not found!");
                }
            }
        }
    } catch (SQLException e) { 
        System.err.println(" Database Error: " + e.getMessage()); 
    }
}
}