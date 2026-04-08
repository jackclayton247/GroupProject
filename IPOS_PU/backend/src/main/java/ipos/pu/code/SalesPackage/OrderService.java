package ipos.pu.code.SalesPackage;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ipos.pu.code.CommsPackage.EmailService;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final CAService caService;

    @Autowired
    public OrderService(OrderRepository orderRepository, EmailService emailService, CAService caService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.caService = caService;
    }

    public int placeOrder(OrderRequest request) {

        List<OrderItem> items = request.getItems();
        List<Double> prices = new ArrayList<>();

        // look up price for each item and build prices list
        double total = 0;
        for (OrderItem item : items) {
            double price = orderRepository.getProductPrice(item.getProductId());
            if (price == -1) return -1;
            prices.add(price);
            total += price * item.getQuantity();
        }

        // check if this is a logged in user and if they get 10th order discount
        double discount = 0;
        String email = request.getUserEmail();
        boolean isLoggedIn = email != null && !email.isEmpty();

        if (isLoggedIn) {
            int orderNumber = orderRepository.getOrderNumber(email);
            if (orderNumber != -1 && (orderNumber + 1) % 10 == 0) {
                discount = total * 0.10;
            }
        }

        double finalTotal = total - discount;

        // create the order row
        int orderId = orderRepository.createOrder(email, request.getDeliveryAddress(), finalTotal, discount);
        if (orderId == -1) return -1;

        // insert order items
        orderRepository.createOrderItems(orderId, items, prices);

        // record payment
        orderRepository.createPayment(orderId, request.getCardType(), request.getCardFirstFour(),
                request.getCardLastFour(), request.getCardExpiry(), finalTotal);

        // Check if CA is online
        boolean caOnline = caService.isCAOnline();
        
        if (caOnline) {
            // CA is online - forward order to CA, CA handles stock
            System.out.println("[OrderService] CA is online - forwarding order to CA");
            // Build order JSON for CA
            StringBuilder orderJson = new StringBuilder("{");
            orderJson.append("\"items\":[");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) orderJson.append(",");
                orderJson.append("{\"productId\":").append(items.get(i).getProductId())
                         .append(",\"quantity\":").append(items.get(i).getQuantity()).append("}");
            }
            orderJson.append("],\"total\":").append(finalTotal).append("}");
            
            boolean forwarded = caService.forwardOrderToCA(orderJson.toString());
            if (!forwarded) {
                System.out.println("[OrderService] Failed to forward to CA, deducting from PU cache");
                // Fallback: deduct from PU cache
                for (OrderItem item : items) {
                    orderRepository.deductStock(item.getProductId(), item.getQuantity());
                }
            }
        } else {
            // CA is offline - deduct from PU cache
            System.out.println("[OrderService] CA is offline - deducting stock from PU cache");
            for (OrderItem item : items) {
                orderRepository.deductStock(item.getProductId(), item.getQuantity());
            }
        }

        // increment order number if logged in, and send email to users
        if (email != null && !email.isEmpty()) {
            if (isLoggedIn) {
                orderRepository.incrementOrderNumber(email);
            }
            emailService.sendEmail(
                email,
                "Order Confirmation: Order #" + orderRepository.getOrderCountByEmail(email),
                "Thank you for your order!\n\nTrack your order at: http://localhost:8080/api/orders/track/" + orderId
            );
        }
        return orderId;
    }
}