package com.shopping.user;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class UserPortal {

    private String email;

    public void showUserMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===========================================================");
            System.out.println("                   WELCOME TO USER PORTAL                   ");
            System.out.println("===========================================================");
            System.out.println("1. Login");
            System.out.println("2. Sign Up (Create New Account)");
            System.out.println("3. Forgot Password");
            System.out.println("4. Change Password");
            System.out.println("0. Exit");
            System.out.print("Enter Choice: ");

            String choiceStr = sc.nextLine().trim();
            if (choiceStr.isEmpty()) continue;
            int choice = Integer.parseInt(choiceStr);

            try (Connection con = DBConnection.getConnection()) {
                switch (choice) {
                    case 1:
                        userLogin(sc, con);
                        break;
                    case 2:
                        userSignUp(sc, con);
                        break;
                    case 3:
                        forgotPassword(sc, con);
                        break;
                    case 4:
                        System.out.print(" Verify Email for Security: ");
                        String verifyEmail = sc.nextLine().trim();
                        changePassword(sc, con, verifyEmail);
                        break;
                    case 0:
                        System.out.println("*****Thank you for visiting!*****");
                        return;
                    default:
                        System.out.println(" Invalid Choice!");
                }
            } catch (SQLException | NumberFormatException e) {
                System.out.println(" Error: " + e.getMessage());
            }
        }
    }

    private void userLogin(Scanner sc, Connection con) {
        System.out.println("\n--- LOGIN ---");
        System.out.print(" Enter Email: ");
        String loginEmail = sc.nextLine().trim();
        
        // Hidden Password Logic for VS Code
        System.out.print(" Enter Password: ");
        String pass = sc.nextLine().trim();
        System.out.print("\033[1A\033[2K"); // Clear the password line
        System.out.println(" Enter Password: [PROTECTED]");

        String sql = "SELECT username FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, loginEmail);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                System.out.println("\n Login Success! Welcome, " + username);
                
                UserDashboard ud = new UserDashboard(sc);
                ud.showDashboard(loginEmail, username); 
                
            } else {
                System.out.println(" Invalid Email or Password!");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void userSignUp(Scanner sc, Connection con) {
        System.out.println("\n--- SIGN UP ---");
        System.out.print(" Username: ");
        String uname = sc.nextLine().trim();

        // Email Validation
        String mail;
        while (true) {
            System.out.print(" Enter Email (eg: @gmail.com): ");
            mail = sc.nextLine().trim();
            if (mail.toLowerCase().endsWith("@gmail.com") && mail.length() > 10) {
                break; 
            } else {
                System.out.println(" Invalid Email! Please use a valid address ending with @gmail.com");
            }
        }

        // Hidden Password Logic
        System.out.print(" Password: ");
        String pass = sc.nextLine().trim();
        System.out.print("\033[1A\033[2K");
        System.out.println(" Password: [PROTECTED]");

        System.out.print(" Gender (M/F): ");
        String gender = sc.nextLine().trim();
        System.out.print(" Phone: ");
        String phone = sc.nextLine().trim();
        System.out.print(" Address: ");
        String address = sc.nextLine().trim();

        String sql = "INSERT INTO users (username, email, password, gender, phone, address) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, uname);
            pst.setString(2, mail);
            pst.setString(3, pass);
            pst.setString(4, gender);
            pst.setString(5, phone);
            pst.setString(6, address);

            if (pst.executeUpdate() > 0) {
                System.out.println("\n Sign Up Success! Please login.");
            }
        } catch (SQLException e) {
            System.out.println(" Sign Up Failed: " + e.getMessage());
        }
    }

    public void forgotPassword(Scanner sc, Connection con) {
        System.out.print("\n Registered Email: ");
        String mail = sc.nextLine().trim();
        String sql = "SELECT password FROM users WHERE email = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, mail);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println(" Your Password: " + rs.getString("password"));
            } else {
                System.out.println(" Email not found!");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void changePassword(Scanner sc, Connection con, String mail) {
        System.out.println("\n--- CHANGE PASSWORD ---");
        
        System.out.print(" Current Password: ");
        String oldPass = sc.nextLine().trim();
        System.out.print("\033[1A\033[2K");
        System.out.println(" Current Password: [PROTECTED]");

        System.out.print(" New Password: ");
        String newPass = sc.nextLine().trim();
        System.out.print("\033[1A\033[2K");
        System.out.println(" New Password: [PROTECTED]");

        String sql = "UPDATE users SET password = ? WHERE email = ? AND password = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, newPass);
            pst.setString(2, mail);
            pst.setString(3, oldPass);

            if (pst.executeUpdate() > 0) {
                System.out.println(" Password Updated!");
            } else {
                System.out.println(" Old password mismatch!");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}