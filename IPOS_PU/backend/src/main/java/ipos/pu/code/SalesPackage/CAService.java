package ipos.pu.code.SalesPackage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.stereotype.Service;

/**
 * Service for checking CA online status and communicating with CA.
 * When CA is online, orders are forwarded to CA for stock handling.
 * When CA is offline, PU handles stock locally.
 */
@Service
public class CAService {

    private static final String CA_BASE_URL = "http://host.docker.internal:8081";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    /**
     * Checks if CA is online by pinging its health endpoint.
     * Uses host.docker.internal to reach CA from Docker container.
     */
    public boolean isCAOnline() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CA_BASE_URL + "/health"))
                    .GET()
                    .timeout(Duration.ofSeconds(2))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Forwards an order to CA when CA is online.
     * CA handles the sale and updates its own database.
     */
    public boolean forwardOrderToCA(String orderJson) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CA_BASE_URL + "/api/sales"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(orderJson))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            System.err.println("[CAService] Failed to forward order to CA: " + e.getMessage());
            return false;
        }
    }
}
