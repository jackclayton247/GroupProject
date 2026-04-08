package integration;

import database.DatabaseManager;
import database.ProductDB;
import domain.Product;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles synchronization between CA and PU systems.
 * - On CA startup: pulls pending stock changes from PU and applies them
 * - Pushes CA's product catalog to PU cache
 * - When CA is online: periodically pushes updates to PU (every 30 seconds)
 */
public class PUSync {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static volatile boolean isOnline = false;

    /**
     * Starts periodic sync with PU when CA is online.
     * Runs every 30 seconds to keep PU cache up to date.
     */
    public static void startPeriodicSync() {
        System.out.println("[PUSync] Starting periodic sync - pushing cache to PU every 30 seconds");
        scheduler.scheduleAtFixedRate(() -> {
            if (isCAOnline()) {
                pushProductsToPU();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Stops the periodic sync scheduler.
     */
    public static void stopPeriodicSync() {
        scheduler.shutdown();
    }

    /**
     * Checks if PU is reachable (CA considers itself "online" when PU is reachable).
     */
    private static boolean isCAOnline() {
        try {
            // Quick ping to check if PU is online
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                new java.net.URL("http://localhost:8080/api/sync/ping").openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int response = conn.getResponseCode();
            boolean online = (response == 200);
            if (online != isOnline) {
                isOnline = online;
                System.out.println("[PUSync] PU connection status: " + (online ? "ONLINE" : "OFFLINE"));
            }
            return online;
        } catch (Exception e) {
            if (isOnline) {
                isOnline = false;
                System.out.println("[PUSync] PU connection status: OFFLINE");
            }
            return false;
        }
    }

    /**
     * Called on CA startup to sync with PU.
     * When CA comes back online, it pulls the PU cache to reconcile offline sales.
     * Then starts periodic sync to keep PU updated.
     * 
     * Flow:
     * 1. Pull full cache from PU (get any sales that happened while CA was offline)
     * 2. Update CA database with PU cache data
     * 3. Push CA's current state to PU (sync any CA-side changes)
     * 4. Start periodic sync
     */
    public static void syncWithPU() {
        System.out.println("[PUSync] Starting sync with PU system...");
        
        // Step 1: Pull full cache from PU (reconcile offline sales)
        // The PU cache already contains the correct stock values (including offline sales)
        System.out.println("[PUSync] Pulling cache from PU...");
        JSONArray puCache = PUApiClient.getFullCache();
        if (puCache.length() > 0) {
            int updated = updateCAFromPUCache(puCache);
            System.out.println("[PUSync] Updated " + updated + " products in CA from PU cache.");
        } else {
            System.out.println("[PUSync] PU cache is empty or PU is offline.");
        }
        
        // Step 2: Clear any pending stock changes in PU
        // The pending changes have already been applied to the cache we just pulled
        JSONArray pendingChanges = PUApiClient.getPendingStockChanges();
        if (pendingChanges.length() > 0) {
            System.out.println("[PUSync] Found " + pendingChanges.length() + " pending stock changes in PU (will clear).");
            boolean cleared = PUApiClient.clearAllPendingChanges();
            if (cleared) {
                System.out.println("[PUSync] Cleared pending changes in PU.");
            } else {
                System.err.println("[PUSync] Warning: Failed to clear pending changes in PU.");
            }
        }
        
        // Step 3: Push CA products to PU cache (CA is now master)
        pushProductsToPU();
        
        // Step 4: Start periodic sync to keep PU updated
        startPeriodicSync();
        
        System.out.println("[PUSync] Sync complete. CA is now master. Periodic updates started (every 30s).");
    }

    /**
     * Updates CA database from PU cache data.
     * Matches products by item_id and updates stock, price, description, etc.
     */
    private static int updateCAFromPUCache(JSONArray puCache) {
        int updated = 0;
        String sql = "UPDATE products SET description = ?, package_type = ?, units_in_pack = ?, " +
                     "price = ?, vat_rate = ?, stock_quantity = ?, min_stock_level = ?, is_active = ? " +
                     "WHERE item_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < puCache.length(); i++) {
                JSONObject p = puCache.getJSONObject(i);
                stmt.setString(1, p.optString("description", ""));
                stmt.setString(2, p.optString("packageType", ""));
                stmt.setInt(3, p.optInt("unitsInPack", 1));
                stmt.setDouble(4, p.optDouble("price", 0.0));
                stmt.setDouble(5, p.optDouble("vatRate", 0.0));
                stmt.setInt(6, p.optInt("stockQuantity", 0));
                stmt.setInt(7, p.optInt("minStockLevel", 0));
                stmt.setInt(8, p.optInt("isActive", 1));
                stmt.setString(9, p.getString("itemId"));
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            for (int r : results) {
                if (r > 0) updated++;
            }
        } catch (Exception e) {
            System.err.println("[PUSync] Error updating CA from PU cache: " + e.getMessage());
            e.printStackTrace();
        }
        
        return updated;
    }

    /**
     * Applies pending stock changes from PU to CA's database.
     * pendingChange is negative for stock deductions (e.g., -5 means reduce by 5).
     */
    private static int applyStockChanges(JSONArray changes) {
        int applied = 0;
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < changes.length(); i++) {
                JSONObject change = changes.getJSONObject(i);
                int productId = change.getInt("productId");
                int pendingChange = change.getInt("pendingChange");
                
                stmt.setInt(1, pendingChange);  // pendingChange is already negative for deductions
                stmt.setInt(2, productId);
                stmt.executeUpdate();
                applied++;
                
                System.out.println("[PUSync] Applied stock change: product " + productId + " change " + pendingChange);
            }
        } catch (Exception e) {
            System.err.println("[PUSync] Error applying stock changes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return applied;
    }

    /**
     * Pushes CA's current product catalog to PU cache.
     * Called periodically when CA is online to keep PU in sync.
     */
    public static void pushProductsToPU() {
        List<Product> products = ProductDB.getAllProducts();
        
        if (products.isEmpty()) {
            System.out.println("[PUSync] No products to push to PU.");
            return;
        }
        
        JSONArray arr = new JSONArray();
        for (Product p : products) {
            JSONObject obj = new JSONObject();
            obj.put("productId", p.getProductId());
            obj.put("itemId", p.getItemId());
            obj.put("description", p.getDescription());
            obj.put("packageType", p.getPackageType() != null ? p.getPackageType() : "");
            obj.put("unitsInPack", p.getUnitsInPack());
            obj.put("price", p.getPrice());
            obj.put("vatRate", p.getVatRate());
            obj.put("stockQuantity", p.getStockQuantity());
            obj.put("minStockLevel", p.getMinStockLevel());
            obj.put("isActive", 1);
            arr.put(obj);
        }
        
        boolean pushed = PUApiClient.pushProductsToCache(arr.toString());
        if (pushed) {
            System.out.println("[PUSync] Pushed " + products.size() + " products to PU cache.");
        } else {
            System.err.println("[PUSync] Warning: Failed to push products to PU cache (PU may be offline).");
        }
    }
}
