package com.shopping.admin;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class UserManagement {
    public void manage(Scanner sc) {
        while (true) {
            System.out.println("\n========= USER MANAGEMENT =========");
            System.out.println("1. View All Registered Users\n2. Remove User\n0. Back");
            System.out.print("Select Option: ");
            
            if (!sc.hasNextInt()) { 
                sc.next(); 
                continue; 
            }
            int ch = sc.nextInt();
            if (ch == 0) break;

            switch (ch) {
                case 1: displayUsersTable(); break;
                case 2: removeUser(sc); break;
                default: System.out.println("Invalid Selection!");
            }
        }
    }

private void displayUsersTable() {
    try (Connection con = DBConnection.getConnection()) {
        String sql = "SELECT id, username, email, gender, phone, address, status FROM users";
        ResultSet rs = con.createStatement().executeQuery(sql);

        String hr = "+-----+------------+---------------------------+--------+--------------+------------------+----------+";
        System.out.println("\n" + hr);
        System.out.printf("| %-3s | %-10s | %-25s | %-6s | %-12s | %-16s | %-8s |\n", 
                          "ID", "USERNAME", "EMAIL ADDRESS", "GENDER", "PHONE", "ADDRESS", "STATUS");
        System.out.println(hr);
        
        while (rs.next()) {
            System.out.printf("| %-3d | %-10.10s | %-25.25s | %-6.6s | %-12.12s | %-16.16s | %-8.8s |\n", 
                rs.getInt("id"), 
                rs.getString("username"), 
                rs.getString("email"),
                (rs.getString("gender") == null ? "-" : rs.getString("gender")),
                (rs.getString("phone") == null ? "-" : rs.getString("phone")),
                (rs.getString("address") == null ? "-" : rs.getString("address")),
                rs.getString("status"));
        }
        System.out.println(hr);
    } catch (SQLException e) { 
        System.err.println(" Database Error: " + e.getMessage()); 
    }
}

    private void removeUser(Scanner sc) {
    sc.nextLine();
    System.out.print(" Enter User Email(s) to remove: ");
    String input = sc.nextLine().trim();

    if (input.isEmpty()) {
        System.out.println(">> ERROR: Email input cannot be empty!");
        return;
    }

    String[] emails = input.split(",");
    
    System.out.print(" Are you sure you want to delete " + emails.length + " user(s)? (1 for Yes / 0 for No): ");
    
    if (!sc.hasNextInt()) {
        sc.next(); 
        System.out.println("Invalid input. Deletion cancelled.");
        return;
    }

    int confirm = sc.nextInt();
    if (confirm == 1) {
        String sql = "DELETE FROM users WHERE LOWER(email) = LOWER(?)";
        int totalRemoved = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            for (String email : emails) {
                String targetEmail = email.trim(); 
                if (targetEmail.isEmpty()) continue;

                ps.setString(1, targetEmail);
                int rows = ps.executeUpdate();
                
                if (rows > 0) {
                    System.out.println(" SUCCESS: '" + targetEmail + "' removed!");
                    totalRemoved++;
                } else {
                    System.out.println(" FAILED: No user found with '" + targetEmail + "'.");
                }
            }
            System.out.println("\n TOTAL SUMMARY: " + totalRemoved + " user(s) successfully removed.");
            
        } catch (SQLException e) { 
            System.out.println("Database Error: " + e.getMessage()); 
        }
    } else {
        System.out.println("Deletion Cancelled.");
    }
}

public void viewAllReviews(Connection con) {
    String sql = "SELECT username, product_name, rating, feedback, review_date FROM reviews ORDER BY review_id DESC";
    
    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        System.out.println("");
        printLine('=', 115);
        System.out.println("                      [ ADMIN REVIEWS DASHBOARD ] ");
        printLine('-', 115);

        System.out.printf("| %-15s | %-25s | %-18s | %-30s |%-10s\n", 
                          "username", "Product Name", "Rating Status", "Feedback", "Date");
        printLine('-', 115);

        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            
            String uName = rs.getString("username");
            if (uName == null || uName.equalsIgnoreCase("null")) uName = "Guest User";

            int r = rs.getInt("rating");
            String status = "";
            if (r == 5) status = "Excellent (5*)";
            else if (r == 4) status = "Good      (4*)";
            else if (r == 3) status = "Average   (3*)";
            else status = "Poor      (1-2*)";

            String feedback = rs.getString("feedback");
            if (feedback == null) feedback = "No Feedback";
            if (feedback.length() > 28) feedback = feedback.substring(0, 25) + "...";

            String fullDate = rs.getString("review_date");
            String shortDate = (fullDate != null && fullDate.length() > 10) ? fullDate.substring(0, 10) : fullDate;

            System.out.printf("| %-15s | %-25s | %-18s | %-30s | %-15s \n", 
                              uName, rs.getString("product_name"), status, feedback, shortDate);
        }

        if (!hasData) {
            System.out.println("| " + String.format("%-111s", "No reviews found.") + " |");
        }
        printLine('=', 115);

    } catch (SQLException e) {
        System.out.println(" Database Error: " + e.getMessage());
    }
}

private void printLine(char c, int len) {
    for (int i = 0; i < len; i++) System.out.print(c);
    System.out.println();
}

}

    