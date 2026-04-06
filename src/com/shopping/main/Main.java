package com.shopping.main;

import com.shopping.admin.AdminPortal;
import com.shopping.admin.IAdminAuth;
import com.shopping.connection.DBConnection;
import com.shopping.user.UserPortal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    private static Connection con;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        
        IAdminAuth adminAuth = (IAdminAuth) new AdminPortal(); 
        
        UserPortal userPortal = new UserPortal();

        while (true) {
            System.out.println("\n***********************************************************");
            System.out.println("           ONLINE SHOPPING CART SYSTEM - MAIN MENU");
            System.out.println("***********************************************************");
            System.out.println("1.   ADMIN PORTAL ");
            System.out.println("2.   USER PORTAL");
            System.out.println("0.   EXIT");
            System.out.println("***********************************************************");
            System.out.print(" Select Portal: ");

            String choiceStr = sc.nextLine().trim();
            if (choiceStr.isEmpty()) continue;

            try {
                int choice = Integer.parseInt(choiceStr);

                switch (choice) {
  case 1:
    try (Connection con = DBConnection.getConnection()) {
        if (con != null) {
            AdminPortal adminPortal = new AdminPortal();
            adminPortal.showAdminMenu(sc, con); 
        }
    } catch (SQLException e) {
        System.out.println(" Connection Error!");
    }
    break;

                    case 2:
                        System.out.println("\n Navigating to User Portal...");
                        userPortal.showUserMenu(); 
                        break;

                    case 0:
                        System.out.println(" Thank you for using our service. Goodbye!");
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println(" Invalid Choice! Please select 1, 2, or 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println(" Error: Please enter a valid number (0, 1, or 2).");
            }
        }
    }
}