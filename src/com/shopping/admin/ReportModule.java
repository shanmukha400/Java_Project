package com.shopping.admin;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class ReportModule {

    public void showReports(Scanner sc, String role) {
        
        if (!(role.equalsIgnoreCase("SUPER_ADMIN") || role.equalsIgnoreCase("PRODUCT_MANAGER"))) {
            System.out.println("\n***********************************************************");
            System.out.println(" ACCESS DENIED: Meeku Reports chuse permission ledhu!");
            System.out.println("***********************************************************");
            return;
        }

        System.out.println("\n===========================================================");
        System.out.println("                ADMIN STRATEGIC REPORTS                    ");
        System.out.println("===========================================================");

        try (Connection con = DBConnection.getConnection()) {
            
            System.out.println("\n---SECTION 1: OVERALL REVENUE SUMMARY---");
            String salesSql = "SELECT COUNT(*) as total_orders, IFNULL(SUM(total_paid), 0) as revenue FROM orders";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(salesSql)) {
                if (rs.next()) {
                    System.out.println("-----------------------------------------------------------");
                    System.out.printf("| Total Orders Processed : %-31d |\n", rs.getInt("total_orders"));
                    System.out.printf("| Gross Revenue Generated: Rs %-28.2f |\n", rs.getDouble("revenue"));
                    System.out.println("-----------------------------------------------------------");
                }
            }

            System.out.println("\n---SECTION 2: TOP SELLING PRODUCTS PERFORMANCE---");
            String perfSql = "SELECT p.product_id, p.product_name, c2.category_name as main_cat, " +
                             "c1.category_name as sub_cat, p.sales_count " +
                             "FROM product p " +
                             "JOIN categories c1 ON p.category = c1.category_name " +
                             "JOIN categories c2 ON c1.parent_id = c2.id " +
                             "WHERE p.sales_count > 0 " + 
                             "ORDER BY p.sales_count DESC";
            
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(perfSql)) {
                System.out.println("+------+----------------------+------------+------------+-------+");
                System.out.printf("| %-4s | %-20s | %-10s | %-10s | %-5s |\n", "ID", "Product Name", "Main Cat", "Sub Cat", "Sold");
                System.out.println("+------+----------------------+------------+------------+-------+");
                
                boolean foundSales = false;
                while (rs.next()) {
                    foundSales = true;
                    System.out.printf("| %-4d | %-20s | %-10s | %-10s | %-5d |\n", 
                        rs.getInt("product_id"), rs.getString("product_name"), 
                        rs.getString("main_cat"), rs.getString("sub_cat"), rs.getInt("sales_count"));
                }
                
                if (!foundSales) {
                    System.out.println("|        No products have been sold yet in the system.     |");
                }
                System.out.println("+------+----------------------+------------+------------+-------+");
            }

            System.out.println("\n---SECTION 3: LOW STOCK ALERTS (< 10 Units)---");
            String stockSql = "SELECT product_name, stock FROM product WHERE stock < 10";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(stockSql)) {
                System.out.println("+---------------------------+----------+------------------+");
                System.out.printf("| %-25s | %-8s | %-16s |\n", "Product Name", "Stock", "Status");
                System.out.println("+---------------------------+----------+------------------+");
                boolean foundStock = false;
                while (rs.next()) {
                    foundStock = true;
                    System.out.printf("| %-25s | %-8d | %-16s |\n", 
                        rs.getString("product_name"), rs.getInt("stock"), "REFILL NEEDED");
                }
                if (!foundStock) System.out.println("| All products are well-stocked.                          |");
                System.out.println("+---------------------------+----------+------------------+");
            }

            System.out.println("\n---SECTION 4: INVENTORY BY CATEGORY---");
            String catSql = "SELECT category, COUNT(*) as p_count FROM product GROUP BY category";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(catSql)) {
                while (rs.next()) {
                    System.out.printf(">> %-15s : %d Varieties Available\n", 
                        rs.getString("category"), rs.getInt("p_count"));
                }
            }

        } catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
        }
        
        System.out.println("\n===========================================================");
        System.out.print("Press Enter to return to Dashboard...");
        System.out.println("\n"); // MEERU ADIGINA LINE SPACE IKKADA ADD CHESA
        sc.nextLine(); 
    }
}