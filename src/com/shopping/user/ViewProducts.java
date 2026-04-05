package com.shopping.user;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.*;

public class ViewProducts {

    private Scanner sc = new Scanner(System.in);

    public void startProductViewing() {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                System.out.println(" DB Connection Failed!");
                return;
            }

            while (true) {
                int cid = showCategories(con);
                if (cid == -1) return; 

                while (true) {
                    int sid = showSubCategories(con, cid);
                    if (sid == -1) break; 
                    if (sid == -7) return; 
                    
                    if (sid > 0) {
                        showProducts(con, sid,sc);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(" DB Error: " + e.getMessage());
        }
    }

    // --- showCategories and showSubCategories methods here (same as before) ---
    public int showCategories(Connection con) throws SQLException {
        Map<Integer, Integer> serialToIdMap = new HashMap<>();
        String sql = "SELECT id, category_name FROM categories WHERE parent_id IS NULL OR parent_id = 0 ORDER BY id ASC";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println("\n===============================");
            System.out.println("       MAIN CATEGORIES");
            System.out.println("===============================");
            int serialNo = 1;
            while (rs.next()) {
                int actualId = rs.getInt("id");
                System.out.println(serialNo + ". " + rs.getString("category_name"));
                serialToIdMap.put(serialNo, actualId);
                serialNo++;
            }
            System.out.println("0. Back to Dashboard");
            System.out.print(" Select Category: ");
            if (!sc.hasNextInt()) { sc.next(); return -1; }
            int choice = sc.nextInt(); sc.nextLine();
            if (choice == 0) return -1;
            return serialToIdMap.getOrDefault(choice, -1);
        }
    }

    public int showSubCategories(Connection con, int cid) throws SQLException {
        Map<Integer, Integer> serialToIdMap = new HashMap<>();
        String sql = "SELECT id, category_name FROM categories WHERE parent_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n--- SUB CATEGORIES ---");
            int serialNo = 1;
            while (rs.next()) {
                int actualId = rs.getInt("id");
                System.out.println(serialNo + ". " + rs.getString("category_name"));
                serialToIdMap.put(serialNo, actualId);
                serialNo++;
            }
            System.out.println("0. Back to Main Categories \n 7. Exit to Dashboard");
            System.out.print(" Select Sub Category: ");
            if (!sc.hasNextInt()) { sc.next(); return -1; }
            int choice = sc.nextInt(); sc.nextLine();
            if (choice == 0) return -1; 
            if (choice == 7) return -7; 
            return serialToIdMap.getOrDefault(choice, 0);
        }
    }

public void showProducts(Connection con, int sid, Scanner sc) throws SQLException {
    String subCatName = "";
    String getNameSql = "SELECT category_name FROM categories WHERE id = ?";
    try (PreparedStatement psName = con.prepareStatement(getNameSql)) {
        psName.setInt(1, sid);
        ResultSet rsName = psName.executeQuery();
        if (rsName.next()) {
            subCatName = rsName.getString("category_name");
        }
    }

    if (subCatName.isEmpty()) {
        System.out.println(" Invalid Category ID.");
        return;
    }

    while (true) {
        String sql = "SELECT product_id, product_name, price, discount, color, size FROM product WHERE LOWER(TRIM(category)) = LOWER(TRIM(?))";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, subCatName);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            
            int idW = 4;
            int nameW = 30;
            int priceW = 10;
            int discW = 6;
            int colorW = 35; 
            int sizeW = 20;  
            int finalW = 12;

            // Total border calculation
            int totalWidth = idW + nameW + priceW + discW + colorW + sizeW + finalW + 22;

            while (rs.next()) {
                if (!found) {
                    System.out.println("\n CATEGORY: " + subCatName.toUpperCase());
                    printBorder('+', totalWidth);
                    System.out.printf("| %-" + idW + "s | %-" + nameW + "s | %-" + priceW + "s | %-" + discW + "s | %-" + colorW + "s | %-" + sizeW + "s | %-" + finalW + "s |\n", 
                                      "ID", "PRODUCT NAME", "PRICE", "DISC%", "COLOR", "SIZE", "FINAL PRICE");
                    printBorder('+', totalWidth);
                    found = true;
                }

                double price = rs.getDouble("price");
                double disc = rs.getDouble("discount");
                double finalPrice = price * (1 - disc / 100);
                
                // Fetch and trim extra spaces
                String pName = (rs.getString("product_name") == null) ? "" : rs.getString("product_name").trim();
                String pColor = (rs.getString("color") == null || rs.getString("color").isEmpty()) ? "N/A" : rs.getString("color").trim();
                String pSize = (rs.getString("size") == null || rs.getString("size").isEmpty()) ? "N/A" : rs.getString("size").trim();

                System.out.printf("| %-" + idW + "d | %-" + nameW + "s | %-" + priceW + ".2f | %-" + discW + ".1f | %-" + colorW + "s | %-" + sizeW + "s | %-" + finalW + ".2f |\n", 
                                  rs.getInt("product_id"), pName, price, disc, pColor, pSize, finalPrice);
            }

            if (!found) {
                System.out.println(" No products available in " + subCatName);
                return;
            }
            printBorder('+', totalWidth);

        } catch (SQLException e) {
            System.out.println(" Database Error: " + e.getMessage());
            break;
        }

        System.out.print("\n🔍 Enter Product Name for Full Specs (or '0' to Back): ");
        String choice = sc.nextLine().trim();
        
        if (choice.equals("0")) break;
        
        if (!choice.isEmpty()) {
            viewAdditionalSpecs(con, choice, subCatName);
            System.out.println("\nPress Enter to continue browsing...");
            sc.nextLine(); 
        }
    }
}


    private void viewAdditionalSpecs(Connection con, String productName, String sCatName) throws SQLException {
        String mCatName = "";
        String mSql = "SELECT c2.category_name FROM categories c1 JOIN categories c2 ON c1.parent_id = c2.id WHERE c1.category_name = ?";
        try (PreparedStatement psM = con.prepareStatement(mSql)) {
            psM.setString(1, sCatName);
            ResultSet rsM = psM.executeQuery();
            if (rsM.next()) mCatName = rsM.getString("category_name").toUpperCase();
        }

        String sCat = sCatName.toUpperCase();
        String mCat = mCatName;
        Map<String, String> dynamicCols = new LinkedHashMap<>();

        if (mCat.contains("FASHION") || sCat.contains("MEN") || sCat.contains("WOMEN") || sCat.contains("KIDS")) {
            System.out.println("\n  ENTERING ALL FASHION SPECIFICATIONS...");
            dynamicCols.put("Brand Name", "brand");
            dynamicCols.put("Size (S/M/L/XL)", "size");
            dynamicCols.put("Fabric Material", "fabric");
            dynamicCols.put("Fit/Shape", "fit_shape");
            dynamicCols.put("Neck Type", "neck");
            dynamicCols.put("Sleeve Length", "sleeve_length");
            dynamicCols.put("Pattern Type", "pattern");
            dynamicCols.put("Occasion (Casual/Formal)", "occasion");
            dynamicCols.put("Stitch Type", "stitch_type");
            dynamicCols.put("Country of Origin", "country_of_origin");
        } 
        else if (mCat.contains("ELECTRONICS") || sCat.contains("MOBILE") || sCat.contains("LAPTOP") || sCat.contains("WATCH") || sCat.contains("HEADPHONE")) {
            System.out.println("\n ENTERING ALL TECH SPECIFICATIONS...");
            dynamicCols.put("Brand", "brand");
            dynamicCols.put("Model Color", "color");
            dynamicCols.put("Storage Capacity", "storage");
            dynamicCols.put("RAM (GB)", "ram");
            dynamicCols.put("Processor Name", "processor");
            dynamicCols.put("Screen Size", "screen_size");
            dynamicCols.put("Battery Capacity", "battery_capacity");
            dynamicCols.put("Camera Resolution", "camera_res");
            dynamicCols.put("Connectivity (5G/BT)", "connectivity");
            dynamicCols.put("Water Resistance", "water_resistance");
            dynamicCols.put("Noise Cancellation", "noise_cancellation");
            dynamicCols.put("Playback Time", "playback_time");
            dynamicCols.put("Warranty Period", "warranty");
        }
        else if (mCat.contains("BEAUTY") || sCat.contains("MAKEUP") || sCat.contains("SKIN") || sCat.contains("HAIR")) {
            System.out.println("\n ENTERING ALL BEAUTY SPECIFICATIONS...");
            dynamicCols.put("Brand", "brand");
            dynamicCols.put("Shade Name/Code", "shade_code");
            dynamicCols.put("Skin or Hair Type", "skin_hair_type");
            dynamicCols.put("Volume/Weight (ml/g)", "volume_weight");
            dynamicCols.put("Battery Power", "battery_power");
            dynamicCols.put("Warranty/Expiry", "warranty_period");
        }
        else if (mCat.contains("BOOKS") || sCat.contains("NOVEL") || sCat.contains("BOOK") || sCat.contains("COMIC")) {
            System.out.println("\n ENTERING ALL BOOK SPECIFICATIONS...");
            dynamicCols.put("Publisher/Brand", "brand");
            dynamicCols.put("Author/Artist Name", "author_artist");
            dynamicCols.put("Genre/Category", "genre_category");
            dynamicCols.put("Binding Type", "binding_type");
            dynamicCols.put("Language", "language");
            dynamicCols.put("Number of Pages", "page_count_set");
        }
        else if (mCat.contains("TOY") || sCat.contains("GAME") || sCat.contains("BABY") || sCat.contains("DIAPER")) {
            System.out.println("\n ENTERING ALL TOYS & BABY SPECS...");
            dynamicCols.put("Brand", "brand");
            dynamicCols.put("Main Material", "material");
            dynamicCols.put("Pieces Count", "pieces_count");
            dynamicCols.put("Battery Required (Yes/No)", "battery_req");
            dynamicCols.put("Age Group/Usage", "occasion"); 
            dynamicCols.put("Weight/Size", "weight_size");
        }
        else if (mCat.contains("HOME") || mCat.contains("KITCHEN") || sCat.contains("DECOR") || sCat.contains("COOKWARE")) {
            System.out.println("\n ENTERING ALL HOME & KITCHEN SPECS...");
            dynamicCols.put("Brand Name", "brand");
            dynamicCols.put("Material", "material");
            dynamicCols.put("Dimensions (LxWxH)", "dimensions");
            dynamicCols.put("Capacity (L/Kg)", "capacity");
            dynamicCols.put("Weight", "weight");
            dynamicCols.put("Power Consumption", "power_consumption");
            dynamicCols.put("Warranty Period", "warranty_period");
            dynamicCols.put("Product Color", "color");
        }
        else {
            System.out.println("\n GENERAL SPECIFICATIONS...");
            dynamicCols.put("Brand", "brand");
            dynamicCols.put("Color", "color");
            dynamicCols.put("Material", "material");
            dynamicCols.put("Warranty", "warranty_period");
        }

        String sql = "SELECT * FROM product WHERE product_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n╔══════════════════════════════════════════════════════════════════════════╗");
                System.out.printf("║ %-72s ║\n", "SPECIFICATIONS: " + productName.toUpperCase());
                System.out.println("╠══════════════════════════════════════════════════════════════════════════╣");
                for (Map.Entry<String, String> entry : dynamicCols.entrySet()) {
                    String dbCol = entry.getValue();
                    String val = rs.getString(dbCol);
                    if (val != null && !val.equalsIgnoreCase("NULL") && !val.isEmpty()) {
                        System.out.printf("║ %-28s : %-41s ║\n", entry.getKey(), val);
                    }
                }
                System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
               
            } else { 
                System.out.println(" Product not found!"); 
            }
        }
    }

    private void printBorder(char c, int len) {
        for (int i = 0; i < len; i++) System.out.print(c);
        System.out.println();
    }
}