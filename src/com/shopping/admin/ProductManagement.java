package com.shopping.admin;

import com.shopping.connection.DBConnection;
import java.sql.*;
import java.util.*;

public class ProductManagement {

    public void manage(Scanner sc) {
        while (true) {
            System.out.println("\n===========================================================");
            System.out.println("                 ADMIN - PRODUCT MANAGEMENT                ");
            System.out.println("===========================================================");
            System.out.println("1. Browse All Products\n2. Add (Category/Sub-Cat/Product)\n3. Update Price/Stock/Discount\n4. Remove(Category/Sub-cat/Product)\n0. Back");
            System.out.print("\n Enter Choice: ");

            String input = sc.nextLine().trim();
            if (input.isEmpty()) continue;
            int ch;
            try { ch = Integer.parseInt(input); } catch (Exception e) { continue; }
            if (ch == 0) break;

            try (Connection con = DBConnection.getConnection()) {
                switch (ch) {
                    case 1: startBrowsing(sc, con); break;
                    case 2: showAddMenu(sc, con); break;
                    case 3: updateProductDetails(sc, con); break;
                    case 4: showRemoveMenu(sc, con); break;
                    default: System.out.println("Invalid Selection!");
                }
            } catch (Exception e) { System.out.println("Database Error: " + e.getMessage()); }
        }
    }

    // --- ADD LOGIC ---
    private void showAddMenu(Scanner sc, Connection con) throws SQLException {
        System.out.println("\n---  ADD MENU ---");
        System.out.println("1. Add Main Category\n2. Add Sub-Category\n3. Add Product\n0. Back");
        System.out.print(" Choice: ");
        int ch = Integer.parseInt(sc.nextLine());

        switch (ch) {
            case 1: saveCategory(sc, con, null); break;
            case 2: 
                int mIdSub = selectFrom(con, null, "Main Category");
                if (mIdSub != -1) saveCategory(sc, con, mIdSub);
                break;
            case 3:
                int mId = selectFrom(con, null, "Main Category");
                if (mId != -1) {
                    int sId = selectFrom(con, mId, "Sub-Category");
                    if (sId != -1) saveProduct(sc, con, sId, mId);
                }
                break;
        }
    }

    private void saveProduct(Scanner sc, Connection con, int subId, int mainId) throws SQLException {
        System.out.println("\n---  NEW PRODUCT BASIC DETAILS ---");
        System.out.print("Product Name: "); String name = sc.nextLine();
        System.out.print("Brand Name: "); String brand = sc.nextLine();
        System.out.print("Price: "); double price = Double.parseDouble(sc.nextLine());
        System.out.print("Discount %: "); double disc = Double.parseDouble(sc.nextLine());
        System.out.print("Stock: "); int stock = Integer.parseInt(sc.nextLine()); 

        String mainCatName = getCatName(con, mainId);
        String subCatName = getCatName(con, subId);

        String sql = "INSERT INTO product (product_name, brand, price, discount, stock, category) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, brand); ps.setDouble(3, price);
            ps.setDouble(4, disc); ps.setInt(5, stock); ps.setString(6, subCatName);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int pId = rs.getInt(1);
                System.out.print("\n Basic Details Saved! Add ALL specifications for " + subCatName + "? (yes/no): ");
                if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
                    handleSpecifications(sc, con, pId, mainCatName, subCatName);
                }
            }
        }
    }

    private void handleSpecifications(Scanner sc, Connection con, int pId, String mainCatName, String subCatName) throws SQLException {
        Map<String, String> dynamicCols = new LinkedHashMap<>();
        String mCat = mainCatName.toUpperCase();
        String sCat = subCatName.toUpperCase();

        if (mCat.contains("FASHION") || sCat.contains("MEN") || sCat.contains("WOMEN") || sCat.contains("KIDS")) {
            System.out.println("\n ENTERING FASHION SPECIFICATIONS...");
            dynamicCols.put("Size (S/M/L/XL)", "size");
            dynamicCols.put("Fabric Material", "fabric");
            dynamicCols.put("Fit/Shape", "fit_shape");
            dynamicCols.put("Neck Type", "neck");
            dynamicCols.put("Sleeve Length", "sleeve_length");
            dynamicCols.put("Pattern Type", "pattern");
            dynamicCols.put("Occasion", "occasion");
            dynamicCols.put("Stitch Type", "stitch_type");
        } 
        else if (mCat.contains("ELECTRONICS") || sCat.contains("MOBILE") || sCat.contains("LAPTOP") || sCat.contains("WATCH")) {
            System.out.println("\n ENTERING Electronic SPECIFICATIONS...");
            dynamicCols.put("Storage Capacity", "storage");
            dynamicCols.put("RAM (GB)", "ram");
            dynamicCols.put("Processor Name", "processor");
            dynamicCols.put("Screen Size", "screen_size");
            dynamicCols.put("Battery Capacity", "battery_capacity");
            dynamicCols.put("Camera Resolution", "camera_res");
            dynamicCols.put("Water Resistance", "water_resistance");
            dynamicCols.put("Noise Cancellation", "noise_cancellation");
            dynamicCols.put("Playback Time", "playback_time");
        }
        else if (mCat.contains("BEAUTY") || sCat.contains("MAKEUP") || sCat.contains("SKIN")) {
            System.out.println("\n ENTERING BEAUTY SPECIFICATIONS...");
            dynamicCols.put("Shade Name/Code", "color");
            dynamicCols.put("Skin or Hair Type", "skin_hair_type");
            dynamicCols.put("Volume/Weight (ml/g)", "volume_weight");
            dynamicCols.put("Expiry Date", "warranty_period");
        }
        else if (mCat.contains("BOOKS") || sCat.contains("NOVEL")) {
            System.out.println("\n ENTERING BOOK SPECIFICATIONS...");
            dynamicCols.put("Author/Artist Name", "author_artist");
            dynamicCols.put("Genre/Category", "genre_category");
            dynamicCols.put("Language", "language");
            dynamicCols.put("Number of Pages", "page_count_set");
        }
        else if (mCat.contains("HOME") || mCat.contains("KITCHEN") || sCat.contains("DECOR")) {
            System.out.println("\n ENTERING HOME & KITCHEN SPECS...");
            dynamicCols.put("Material", "material");
            dynamicCols.put("Dimensions (LxWxH)", "dimensions");
            dynamicCols.put("Capacity (L/Kg)", "capacity");
            dynamicCols.put("Power Consumption", "power_consumption");
            dynamicCols.put("Warranty Period", "warranty_period");
        }
        else {
            dynamicCols.put("Material", "material");
            dynamicCols.put("Color", "color");
            dynamicCols.put("Warranty", "warranty_period");
        }

        StringBuilder sb = new StringBuilder("UPDATE product SET ");
        for (String col : dynamicCols.values()) sb.append(col).append(" = ?, ");
        sb.setLength(sb.length() - 2); 
        sb.append(" WHERE product_id = ?");

        try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int i = 1;
            for (String label : dynamicCols.keySet()) {
                System.out.print(" " + label + ": ");
                ps.setString(i++, sc.nextLine().trim());
            }
            ps.setInt(i, pId);
            ps.executeUpdate();
            System.out.println("\n All Specifications updated correctly!");
            
        }
    }

    public void startBrowsing(Scanner sc, Connection con) throws SQLException {
        int mId = selectFrom(con, null, "Main Category");
        if (mId == -1) return;

        int sId = selectFrom(con, mId, "Sub-Category");
        
        if (sId != -1) {
            String subName = getCatName(con, sId);
            displayProductsInTable(sc, con, subName);
        } else {
            System.out.println("❌ Invalid Sub-Category Selection!");
        }
    }

   private void displayProductsInTable(Scanner sc, Connection con, String sub) throws SQLException {
        String sql = "SELECT product_id, product_name, brand, price, discount, stock FROM product WHERE category = ?";
        
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, sub); 
            ResultSet rs = pst.executeQuery();

            System.out.println("\n📦 PRODUCTS IN: " + sub.toUpperCase());
            boolean found = false;
            
            System.out.println("+---------+-------------------------------+------------+--------+---------+");
            System.out.printf("| %-7s | %-29s | %-10s | %-6s | %-7s |\n", "ID", "PRODUCT NAME", "PRICE", "DISC%", "STOCK");
            System.out.println("+---------+-------------------------------+------------+--------+---------+");

            while (rs.next()) {
                found = true;
                System.out.printf("| %-7d | %-29s | %-10.2f | %-5.0f%% | %-7d |\n",
                    rs.getInt("product_id"), rs.getString("product_name"), 
                    rs.getDouble("price"), rs.getDouble("discount"), rs.getInt("stock"));
            }

            if (!found) {
                System.out.println("|               No products found in this category.               |");
            }
            System.out.println("+---------+-------------------------------+------------+--------+---------+");

            if (found) {
                System.out.print("\n Enter Product Name to see Full Specs (or press Enter to skip): ");
                String pName = sc.nextLine().trim();
                
                if (!pName.isEmpty()) {
                    showFullProductInfo(con, pName);
                    // Specs chusaka menu ki velle mundu chinna pause
                    System.out.println("\nPress Enter to return to Menu...");
                    sc.nextLine();
                }
            }
        }
    }

    private void showFullProductInfo(Connection con, String productName) throws SQLException {
        String sql = "SELECT * FROM product WHERE product_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String mCat = rs.getString("category").toUpperCase();
                
                System.out.println("\n=================  FULL SPECIFICATIONS =================");
                System.out.println("PRODUCT  : " + rs.getString("product_name").toUpperCase());
                System.out.println("BRAND    : " + rs.getString("brand"));
                System.out.println("COLOUR   : " + rs.getString("color"));
                System.out.println("---------------------------------------------------------");

                List<String> displayCols = new ArrayList<>();

                if (mCat.contains("FASHION") || mCat.contains("MEN") || mCat.contains("WOMEN") || mCat.contains("KIDS")) {
                    displayCols.addAll(Arrays.asList("size", "fabric", "fit_shape", "neck", "sleeve_length", "pattern", "occasion", "stitch_type", "color", "origin"));
                } 
                else if (mCat.contains("ELECTRONICS") || mCat.contains("MOBILE") || mCat.contains("LAPTOP") || mCat.contains("WATCH")) {
                    displayCols.addAll(Arrays.asList("storage", "ram", "processor", "screen_size", "battery_capacity", "camera_res", "water_resistance", "noise_cancellation", "playback_time", "color", "origin"));
                }
                else if (mCat.contains("BEAUTY") || mCat.contains("MAKEUP") || mCat.contains("SKIN")) {
                    displayCols.addAll(Arrays.asList("color", "skin_hair_type", "volume_weight", "expiry_period", "origin"));
                }
                else if (mCat.contains("BOOKS") || mCat.contains("NOVEL")) {
                    displayCols.addAll(Arrays.asList("author_artist", "genre_category", "language", "page_count_set", "origin"));
                }
                // 5. HOME & KITCHEN
                else if (mCat.contains("HOME") || mCat.contains("KITCHEN") || mCat.contains("DECOR")) {
                    displayCols.addAll(Arrays.asList("material", "dimensions", "capacity", "power_consumption", "warranty_period", "color", "origin"));
                }
                else {
                    displayCols.addAll(Arrays.asList("material", "color", "warranty_period", "origin"));
                }

                // Filtering: Only display columns that exist in the above lists AND have values in DB
                for (String col : displayCols) {
                    try {
                        String val = rs.getString(col);
                        if (val != null && !val.trim().isEmpty() && !val.equalsIgnoreCase("N/A")) {
                            String label = col.replace("_", " ").toUpperCase();
                            System.out.printf("🔹 %-18s : %s\n", label, val);
                        }
                    } catch (SQLException e) {
                        // Skip if column doesn't exist in DB
                    }
                }
                System.out.println("=========================================================");
            } else {
                System.out.println(" Product Not Found!");
            }
        }
    }
    // --- HELPERS ---
    private int selectFrom(Connection con, Integer parentId, String type) throws SQLException {
        String query = (parentId == null) ? "SELECT id, category_name FROM categories WHERE parent_id IS NULL" 
                                          : "SELECT id, category_name FROM categories WHERE parent_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            if (parentId != null) ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n--- Select " + type + " ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                System.out.println(id + ". " + rs.getString("category_name"));
                ids.add(id);
            }
        }
        if (ids.isEmpty()) return -1;
        System.out.print(" Choice ID: ");
        Scanner sc = new Scanner(System.in);
        int sel = Integer.parseInt(sc.nextLine());
        return ids.contains(sel) ? sel : -1;
    }

    private void saveCategory(Scanner sc, Connection con, Integer pId) throws SQLException {
        System.out.print("New Category Name: ");
        String name = sc.nextLine();
        String sql = "INSERT INTO categories (category_name, parent_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            if (pId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, pId);
            ps.executeUpdate();
            System.out.println(" Added!");
        }
    }

    private String getCatName(Connection con, int id) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery("SELECT category_name FROM categories WHERE id=" + id);
        return rs.next() ? rs.getString(1) : "";
    }
private void updateProductDetails(Scanner sc, Connection con) throws SQLException {
    System.out.print(" Enter Product ID to Update: ");
    int id = Integer.parseInt(sc.nextLine());

    // 1. SELECT query lo product_name kuda add chesam
    String checkSql = "SELECT product_name, price, stock, discount, color, size FROM product WHERE product_id = ?";
    try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
        psCheck.setInt(1, id);
        ResultSet rs = psCheck.executeQuery();

        if (rs.next()) {
            // Product ID enter cheyagane Name display avthundi
            System.out.println("--- Current Details Found. Enter New Values (or press Enter to keep current) ---");
            System.out.println("\nProduct: " + rs.getString("product_name"));

            // 1. New Price
            System.out.print("New Price [" + rs.getDouble("price") + "]: ");
            String priceStr = sc.nextLine().trim();
            if (!priceStr.isEmpty()) updateField(con, "price", priceStr, id);

            // 2. New Stock
            System.out.print("New Stock [" + rs.getInt("stock") + "]: ");
            String stockStr = sc.nextLine().trim();
            if (!stockStr.isEmpty()) updateField(con, "stock", stockStr, id);

            // 3. New Discount
            System.out.print("New Discount % [" + rs.getDouble("discount") + "]: ");
            String discStr = sc.nextLine().trim();
            if (!discStr.isEmpty()) updateField(con, "discount", discStr, id);

            // 4. New Color (Append Logic)
            String currentColors = rs.getString("color");
            System.out.print("New Color [" + currentColors + "]: ");
            String colorStr = sc.nextLine().trim();
            if (!colorStr.isEmpty()) {
                // Patha colors ki kotha color add chesthunnam
                String updatedColors = currentColors + ", " + colorStr;
                updateField(con, "color", "'" + updatedColors + "'", id);
            }

            // 5. New Size
            System.out.print("New Size [" + rs.getString("size") + "]: ");
            String sizeStr = sc.nextLine().trim();
            if (!sizeStr.isEmpty()) updateField(con, "size", "'" + sizeStr + "'", id);

            System.out.println("\n Product details updated successfully!");
        } else {
            System.out.println(" Product ID not found!");
        }
    }
}
// Helper method code ni neat ga ఉంచడానికి
private void updateField(Connection con, String column, String value, int id) throws SQLException {
    String sql = "UPDATE product SET " + column + " = " + value + " WHERE product_id = " + id;
    try (Statement stmt = con.createStatement()) {
        stmt.executeUpdate(sql);
    }
}

    private void showRemoveMenu(Scanner sc, Connection con) throws SQLException {
    System.out.println("\n---  REMOVE MANAGEMENT ---");
    System.out.println("1. Remove Category/Sub-Category");
    System.out.println("2. Remove Product(s)");
    System.out.println("0. Back");
    System.out.print("Select  Choice: ");
    
    int ch = Integer.parseInt(sc.nextLine());
    if (ch == 0) return;

    if (ch == 1) {
        handleCategoryRemoval(sc, con);
    } 
    else if (ch == 2) {
        System.out.print(" Enter Product ID(s) to remove: ");
        String inputIds = sc.nextLine().trim();

        if (inputIds.isEmpty()) return;

        String query = "SELECT product_id, product_name FROM product WHERE product_id IN (" + inputIds + ")";
        
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n  PRODUCTS FOUND:");
            boolean found = false;
            while (rs.next()) {
                System.out.println(" ID: " + rs.getInt(1) + " | Name: " + rs.getString(2));
                found = true;
            }

            if (found) {
                System.out.print("\nAre you sure you want to remove these product(s)? (yes/no): ");
                if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
                    int count = stmt.executeUpdate("DELETE FROM product WHERE product_id IN (" + inputIds + ")");
                    System.out.println(" Successfully removed " + count + " product(s)!");
                } else {
                    System.out.println(" Removal Cancelled.");
                }
            } else {
                System.out.println(" No products found with the given ID(s).");
            }
        } catch (SQLException e) {
            System.out.println(" Error: Please check the ID format (e.g., 701, 702).");
        }
    }
}

private void handleCategoryRemoval(Scanner sc, Connection con) throws SQLException {
    int mId = selectFrom(con, null, "Main Category");
    if (mId == -1) return;
    
    System.out.println("1. Delete this Main Category\n2. Delete a Sub-Category under it");
    int type = Integer.parseInt(sc.nextLine());
    
    
    if (type == 1) {
        System.out.println("Select choice :");
        con.createStatement().executeUpdate("DELETE FROM categories WHERE id = " + mId);
        System.out.println(" Main Category Removed!");
    } else {
        int sId = selectFrom(con, mId, "Sub-Category");
        if (sId != -1) {
            System.out.println("Select choice :");
            con.createStatement().executeUpdate("DELETE FROM categories WHERE id = " + sId);
            System.out.println(" Sub-Category Removed!");
        }
    }
}
}