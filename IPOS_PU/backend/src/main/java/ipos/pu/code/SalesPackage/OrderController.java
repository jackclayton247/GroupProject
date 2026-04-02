package ipos.pu.code.SalesPackage;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public String placeOrder(@RequestBody OrderRequest request) {
        int orderId = orderService.placeOrder(request);
        if (orderId == -1) return "error placing order";
        return "order placed, id: " + orderId;
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
}