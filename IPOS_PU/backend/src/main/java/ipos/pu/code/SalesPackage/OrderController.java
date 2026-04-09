package ipos.pu.code.SalesPackage;
import java.util.Map;

import ipos.pu.code.config.DatabaseConfig;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {
        System.out.println("[OrderController] Received order request: " + request);
        
        // Validate request
        if (request == null) {
            return ResponseEntity.badRequest().body("Request body is null");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Order items are required");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Delivery address is required");
        }
        if (request.getCardFirstFour() == null || request.getCardLastFour() == null || request.getCardExpiry() == null) {
            return ResponseEntity.badRequest().body("Card details are required");
        }
        
        try {
            int orderId = orderService.placeOrder(request);
            if (orderId == -1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing order - check product availability");
            }
            return ResponseEntity.ok("order placed, id: " + orderId);
        } catch (Exception e) {
            System.err.println("[OrderController] Error placing order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/undelivered")
    public String getUndeliveredOrders() {
        return orderRepository.getUndeliveredOrders();
    }

    @GetMapping("/track/{orderId}")
    public String trackOrder(@PathVariable int orderId) {
        return orderRepository.getOrderStatus(orderId);
    }

    @PutMapping("/{orderId}/status")
    public String updateOrderStatus(@PathVariable int orderId, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || status.isEmpty()) return "error: missing status";
        return orderRepository.updateOrderStatus(orderId, status);
    }

    /**
     * Get orders for the currently logged-in user.
     * Requires session with logged in user.
     */
    @GetMapping("/my-orders")
    public String getMyOrders(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null || email.isEmpty()) {
            return "{\"error\":\"not_logged_in\",\"message\":\"Please log in to view your orders\"}";
        }
        return orderRepository.getOrdersByEmail(email);
    }
}
