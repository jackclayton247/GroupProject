package ipos.pu.code.SalesPackage;

import ipos.pu.code.config.DatabaseConfig;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    /**
     * Returns all products with pending stock changes (pending_stock_change != 0).
     * Format: [{"productId":1,"pendingChange":-5}, ...]
     */
    @GetMapping("/pending-changes")
    public String getPendingChanges() {
        String sql = "SELECT product_id, pending_stock_change FROM product_cache WHERE pending_stock_change != 0";
        StringBuilder result = new StringBuilder("[");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) result.append(",");
                result.append("{")
                    .append("\"productId\":").append(rs.getInt("product_id")).append(",")
                    .append("\"pendingChange\":").append(rs.getInt("pending_stock_change"))
                    .append("}");
                first = false;
            }
            result.append("]");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Clears pending changes for a specific product after CA has synced.
     */
    @PostMapping("/clear-pending/{productId}")
    public String clearPending(@PathVariable int productId) {
        String sql = "UPDATE product_cache SET pending_stock_change = 0 WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, productId);
            pst.executeUpdate();
            return "cleared";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Clears all pending changes after CA has synced everything.
     */
    @PostMapping("/clear-all-pending")
    public String clearAllPending() {
        String sql = "UPDATE product_cache SET pending_stock_change = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.executeUpdate();
            return "all cleared";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Allows CA to push its full product list to update the PU cache.
     * Expects JSON array: [{"productId":1,"itemId":"ITEM001","description":"...","price":3.50,"stockQuantity":100,...}, ...]
     */
    @PostMapping("/update-cache")
    public String updateCache(@RequestBody String productsJson) {
        try {
            org.json.JSONArray arr = new org.json.JSONArray(productsJson);
            int updated = 0;
            int inserted = 0;
            
            try (Connection conn = DatabaseConfig.getConnection()) {
                // First try to UPDATE existing products by item_id
                String updateSql = "UPDATE product_cache SET description=?, package_type=?, " +
                         "units_in_pack=?, price=?, vat_rate=?, stock_quantity=?, " +
                         "min_stock_level=?, is_active=?, pending_stock_change=0 WHERE item_id=?";
                
                // If no rows updated, INSERT new product
                String insertSql = "INSERT INTO product_cache (item_id, description, package_type, units_in_pack, " +
                         "price, vat_rate, stock_quantity, min_stock_level, is_active, pending_stock_change) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
                
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                     PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject p = arr.getJSONObject(i);
                        String itemId = p.getString("itemId");
                        
                        // Try UPDATE first
                        updateStmt.setString(1, p.getString("description"));
                        updateStmt.setString(2, p.optString("packageType", ""));
                        updateStmt.setInt(3, p.optInt("unitsInPack", 1));
                        updateStmt.setDouble(4, p.getDouble("price"));
                        updateStmt.setDouble(5, p.optDouble("vatRate", 0.0));
                        updateStmt.setInt(6, p.getInt("stockQuantity"));
                        updateStmt.setInt(7, p.optInt("minStockLevel", 0));
                        updateStmt.setInt(8, p.optInt("isActive", 1));
                        updateStmt.setString(9, itemId);
                        
                        int rows = updateStmt.executeUpdate();
                        if (rows > 0) {
                            updated++;
                        } else {
                            // Product doesn't exist, INSERT it
                            insertStmt.setString(1, itemId);
                            insertStmt.setString(2, p.getString("description"));
                            insertStmt.setString(3, p.optString("packageType", ""));
                            insertStmt.setInt(4, p.optInt("unitsInPack", 1));
                            insertStmt.setDouble(5, p.getDouble("price"));
                            insertStmt.setDouble(6, p.optDouble("vatRate", 0.0));
                            insertStmt.setInt(7, p.getInt("stockQuantity"));
                            insertStmt.setInt(8, p.optInt("minStockLevel", 0));
                            insertStmt.setInt(9, p.optInt("isActive", 1));
                            insertStmt.executeUpdate();
                            inserted++;
                        }
                    }
                }
            }
            return "cache updated: " + updated + " updated, " + inserted + " inserted";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    /**
     * Returns full product cache for CA to pull when reconnecting.
     * CA uses this to reconcile offline changes.
     */
    @GetMapping("/cache")
    public String getFullCache() {
        String sql = "SELECT product_id, item_id, description, package_type, units_in_pack, price, vat_rate, stock_quantity, min_stock_level, is_active FROM product_cache WHERE is_active = 1";
        StringBuilder result = new StringBuilder("[");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) result.append(",");
                result.append("{")
                    .append("\"productId\":").append(rs.getInt("product_id")).append(",")
                    .append("\"itemId\":\"").append(rs.getString("item_id")).append("\",")
                    .append("\"description\":\"").append(rs.getString("description")).append("\",")
                    .append("\"packageType\":\"").append(rs.getString("package_type")).append("\",")
                    .append("\"unitsInPack\":").append(rs.getInt("units_in_pack")).append(",")
                    .append("\"price\":").append(rs.getDouble("price")).append(",")
                    .append("\"vatRate\":").append(rs.getDouble("vat_rate")).append(",")
                    .append("\"stockQuantity\":").append(rs.getInt("stock_quantity")).append(",")
                    .append("\"minStockLevel\":").append(rs.getInt("min_stock_level")).append(",")
                    .append("\"isActive\":").append(rs.getInt("is_active"))
                    .append("}");
                first = false;
            }
            result.append("]");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Health check endpoint for CA to ping.
     * Returns "pong" if PU is online.
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
