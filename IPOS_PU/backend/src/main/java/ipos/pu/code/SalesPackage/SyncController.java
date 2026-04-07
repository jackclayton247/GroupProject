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
            String sql = "INSERT INTO product_cache (product_id, item_id, description, package_type, units_in_pack, price, vat_rate, stock_quantity, min_stock_level, is_active, pending_stock_change) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0) " +
                         "ON DUPLICATE KEY UPDATE item_id=VALUES(item_id), description=VALUES(description), package_type=VALUES(package_type), " +
                         "units_in_pack=VALUES(units_in_pack), price=VALUES(price), vat_rate=VALUES(vat_rate), stock_quantity=VALUES(stock_quantity), " +
                         "min_stock_level=VALUES(min_stock_level), is_active=VALUES(is_active)";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject p = arr.getJSONObject(i);
                    pst.setInt(1, p.getInt("productId"));
                    pst.setString(2, p.getString("itemId"));
                    pst.setString(3, p.getString("description"));
                    pst.setString(4, p.optString("packageType", ""));
                    pst.setInt(5, p.optInt("unitsInPack", 1));
                    pst.setDouble(6, p.getDouble("price"));
                    pst.setDouble(7, p.optDouble("vatRate", 0.0));
                    pst.setInt(8, p.getInt("stockQuantity"));
                    pst.setInt(9, p.optInt("minStockLevel", 0));
                    pst.setInt(10, p.optInt("isActive", 1));
                    pst.addBatch();
                }
                pst.executeBatch();
                return "cache updated with " + arr.length() + " products";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
}
