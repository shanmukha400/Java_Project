package com.shopping.admin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import com.shopping.connection.DBConnection;
//Varshitha
public class AdminDashboard {

    public void showAdminDashboard(Scanner sc, String adminName, String role) {
        
        String userRole = (role != null) ? role.toUpperCase().trim() : "GUEST";

        while (true) {
            System.out.println("\n***************************************************************************");
            System.out.println("                ADMIN DASHBOARD - " + adminName.toUpperCase());
            System.out.println("                ROLE: [" + userRole + "]");
            System.out.println("***************************************************************************");

            if (userRole.equals("SUPER_ADMIN") || userRole.equals("PRODUCT_MANAGER")) {
                System.out.println("1. Product Management");
                System.out.println("2. Discount Management");
            }

            if (userRole.equals("SUPER_ADMIN") || userRole.equals("USER_MANAGER")) {
                System.out.println("3. User Management");
            }

            System.out.println("4. View User Reviews");

            if (userRole.equals("SUPER_ADMIN") || userRole.equals("ORDER_MANAGER")) {
                System.out.println("5. Order Management");
            }

            if (userRole.equals("SUPER_ADMIN") || userRole.equals("PRODUCT_MANAGER")) {
                System.out.println("6. Strategic Reports");
            }

            if (userRole.equals("SUPER_ADMIN")) {
                System.out.println("7. Admin Controls");
            }

            System.out.println("0. Logout");
            System.out.println("---------------------------------------------------------------------------");
            System.out.print("Selection: ");

            String choiceStr = sc.nextLine().trim();
            if (choiceStr.isEmpty()) continue;
            
            int option;
            try {
                option = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Input! Enter a number.");
                continue;
            }

            if (option == 0) break;

            try (Connection con = DBConnection.getConnection()) {
                switch (option) {
                    case 1: 
                        if (userRole.equals("SUPER_ADMIN") || userRole.equals("PRODUCT_MANAGER"))
                            new ProductManagement().manage(sc); 
                        else accessDenied();
                        break;

                    case 2: 
                        if (userRole.equals("SUPER_ADMIN") || userRole.equals("PRODUCT_MANAGER"))
                            new DiscountManagement().manageCoupons(sc);
                        else accessDenied();
                        break;

                    case 3: 
                        if (userRole.equals("SUPER_ADMIN") || userRole.equals("USER_MANAGER"))
                            new UserManagement().manage(sc);
                        else accessDenied();
                        break;

                    case 4: 
                        new UserManagement().viewAllReviews(con); 
                        break;

                    case 5: 
                        if (userRole.equals("SUPER_ADMIN") || userRole.equals("ORDER_MANAGER"))
                            new OrderManagement().manage(sc);
                        else accessDenied();
                        break;

                    case 6: 
                        // Ikkada kooda ORDER_MANAGER access ni block chesa
                        if (userRole.equals("SUPER_ADMIN") || userRole.equals("PRODUCT_MANAGER"))
                            new ReportModule().showReports(sc, userRole);
                        else accessDenied();
                        break;

                    case 7:
                        if (userRole.equals("SUPER_ADMIN"))
                            new ManageAdmins().manageAdmins(sc, con);
                        else accessDenied();
                        break;

                    default: 
                        System.out.println("INVALID OPTION!");
                        break;
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void accessDenied() {
        System.out.println("ACCESS DENIED: Insufficient Permissions for your role.");
    }
}