package ipos.pu.code.SalesPackage;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService = new OrderService();

    @PostMapping
    public String placeOrder(@RequestBody OrderRequest request) {
        int orderId = orderService.placeOrder(request);
        if (orderId == -1) return "error placing order";
        return "order placed, id: " + orderId;
    }

    private final OrderRepository orderRepository = new OrderRepository();

    @GetMapping("/undelivered")
    public String getUndeliveredOrders() {
        return orderRepository.getUndeliveredOrders();
    }

    @GetMapping("/track/{orderId}")
    public String trackOrder(@PathVariable int orderId) {
        return orderRepository.getOrderStatus(orderId);
    }
}