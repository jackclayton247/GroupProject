package ipos.pu.code.SalesPackage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

/**
 * Service for checking CA online status and communicating with CA.
 * When CA is online: stock is deducted from CA's database directly.
 * When CA is offline: stock is deducted from PU cache.
 */
@Service
public class CAService {

    private static final String CA_BASE_URL = System.getenv().getOrDefault("CA_BASE_URL", "http://host.docker.internal:8082");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final OrderRepository orderRepository;

    @Autowired
    public CAService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Checks if CA is online by pinging its catalogue endpoint.
     * Uses host.docker.internal to reach CA from Docker container.
     */
    public boolean isCAOnline() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CA_BASE_URL + "/api/stock/catalogue"))
                    .GET()
                    .timeout(Duration.ofSeconds(2))
                    .build();

            HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deducts stock from CA's database when CA is online.
     * Uses CA's /api/stock/deduct/{itemId} endpoint.
     * @param itemId The item_id to deduct from
     * @param quantity Quantity to deduct
     * @return true if successful
     */
    public boolean deductStockFromCA(String itemId, int quantity) {
        try {
            String jsonBody = new JSONObject().put("quantity", quantity).toString();
            // URL-encode itemId to handle spaces
            String encodedItemId = itemId.replace(" ", "%20");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CA_BASE_URL + "/api/stock/deduct/" + encodedItemId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
            boolean success = response.statusCode() == 200;
            
            if (success) {
                System.out.println("[CAService] Deducted " + quantity + " from " + itemId + " in CA database");
            } else {
                System.err.println("[CAService] Failed to deduct from CA: HTTP " + response.statusCode() + " - " + response.body());
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("[CAService] Failed to deduct stock from CA: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deducts stock from PU cache when CA is offline.
     * @param productId The product_id in PU cache
     * @param quantity Quantity to deduct
     * @return true if successful
     */
    public boolean deductStockFromPUCache(int productId, int quantity) {
        orderRepository.deductStock(productId, quantity);
        System.out.println("[CAService] Deducted " + quantity + " from product " + productId + " in PU cache (CA offline)");
        return true;
    }

    /**
     * Gets the item_id for a product_id from PU cache.
     * Needed to map PU product_id to CA item_id.
     */
    public String getItemIdFromProductId(int productId) {
        return orderRepository.getItemIdByProductId(productId);
    }
}
