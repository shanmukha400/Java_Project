package com.shopping.user;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class CartManagemnt {

   public void addToCart(Scanner sc, String email) {
        System.out.print("\n Enter Product Name to Add: ");
        String pName = sc.nextLine().trim();

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT product_name, final_price, size, color FROM product WHERE product_name = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, pName);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println(" Product not found!");
                    return;
                }

                String productName = rs.getString("product_name");
                double finalPrice = rs.getDouble("final_price");
                String dbSizes = rs.getString("size");
                String dbColors = rs.getString("color");

                String selectedSize = selectOption(sc, "Size", dbSizes);
                String selectedColor = selectOption(sc, "Color", dbColors);

                System.out.print(" Enter Quantity: ");
                int qty = Integer.parseInt(sc.nextLine().trim());

                saveToCart(con, email, productName, qty, finalPrice, selectedSize, selectedColor);
            }
        } catch (Exception e) { System.out.println("❌ Error: " + e.getMessage()); }
    }

    private String selectOption(Scanner sc, String label, String options) {
        if (options == null || options.trim().isEmpty() || options.equalsIgnoreCase("NULL")) return "N/A";
        List<String> list = Arrays.stream(options.split(",")).map(String::trim).collect(Collectors.toList());
        System.out.println(" Available " + label + "s: " + list);
        while (true) {
            System.out.print(" Select " + label + ": ");
            String input = sc.nextLine().trim();
            if (list.stream().anyMatch(s -> s.equalsIgnoreCase(input))) return input.toUpperCase();
            System.out.println(" Invalid " + label + "!");
        }
    }

    private void saveToCart(Connection con, String email, String pName, int qty, double price, String size, String color) throws SQLException {
        String checkSql = "SELECT quantity FROM cart WHERE user_email = ? AND product_name = ? AND size = ? AND color = ?";
        
        try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setString(1, email);
            checkPs.setString(2, pName);
            checkPs.setString(3, size);
            checkPs.setString(4, color);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                String updateSql = "UPDATE cart SET quantity = ?, price = ? WHERE user_email = ? AND product_name = ? AND size = ? AND color = ?";
                try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                    updatePs.setInt(1, qty);
                    updatePs.setDouble(2, price);
                    updatePs.setString(3, email);
                    updatePs.setString(4, pName);
                    updatePs.setString(5, size);
                    updatePs.setString(6, color);
                    updatePs.executeUpdate();
                    System.out.println(" SUCCESS: Cart Updated!");
                }
            } else {
                String insertSql = "INSERT INTO cart (user_email, product_name, quantity, price, size, color) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                    insertPs.setString(1, email);
                    insertPs.setString(2, pName);
                    insertPs.setInt(3, qty);
                    insertPs.setDouble(4, price);
                    insertPs.setString(5, size);
                    insertPs.setString(6, color);
                    insertPs.executeUpdate();
                    System.out.println(" SUCCESS: Added to Cart!");
                }
            }
        }
        }
    public void updateCartMenu(Scanner sc, String email) {
        while (true) {
            System.out.println("\n---  CART MANAGEMENT ---");
            System.out.println("1. Search product \n 2. Update Qty \n 3. Remove product \n 4. Clear cart  \n 0. Back");
            System.out.print(" Enter your Option: ");
            
            String input = sc.nextLine().trim(); 
            if (input.equals("0")) break;

            try (Connection con = DBConnection.getConnection()) {
                switch (input) {
                    case "1": searchInCart(sc, con, email); break;
                    case "2": updateQuantity(sc, con, email); break;
                    case "3": removeProducts(sc, con, email); break;
                    case "4": clearCart(sc, con, email); break;
                    default: System.out.println(" Invalid Option!");
                }
            } catch (SQLException e) { 
                System.out.println(" Error: " + e.getMessage()); 
            }
        }
    }

    private void searchInCart(Scanner sc, Connection con, String email) throws SQLException {
        System.out.print(" Search Product Name: ");
        String name = sc.nextLine().trim();
        String sql = "SELECT product_name, size, color, quantity, price FROM cart WHERE user_email = ? AND product_name LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email); 
            ps.setString(2, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("\n " + rs.getString("product_name") + " [" + rs.getString("size") + "/" + rs.getString("color") + "]");
                System.out.println("   Qty: " + rs.getInt("quantity") + " | Price: Rs " + rs.getDouble("price"));
            }
            if(!found) System.out.println("📭 Not found in cart.");
        }
    }

    private void updateQuantity(Scanner sc, Connection con, String email) throws SQLException {
        System.out.print(" Product Name: "); String name = sc.nextLine().trim();
        System.out.print(" New Qty: "); 
        int qty = Integer.parseInt(sc.nextLine().trim()); 
        String sql = "UPDATE cart SET quantity = ? WHERE user_email = ? AND product_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, qty); ps.setString(2, email); ps.setString(3, name);
            if (ps.executeUpdate() > 0) System.out.println(" Quantity Updated!");
            else System.out.println(" Product not found.");
        }
    }

    private void removeProducts(Scanner sc, Connection con, String email) throws SQLException {
        System.out.print("Enter Name(s) to remove : ");
        String input = sc.nextLine();
        String[] prods = input.split(",");
        String sql = "DELETE FROM cart WHERE user_email = ? AND product_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String p : prods) {
                ps.setString(1, email); ps.setString(2, p.trim());
                ps.executeUpdate();
            }
            System.out.println(" Items removed.");
        }
    }

    private void clearCart(Scanner sc, Connection con, String email) throws SQLException {
        System.out.print("\n Clear everything? (y/n): ");
        String choice = sc.nextLine().trim().toLowerCase();
        if (choice.equals("y")) {
            String sql = "DELETE FROM cart WHERE user_email = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.executeUpdate();
                System.out.println(" Cart Cleared!");
            }
        }
    }
}