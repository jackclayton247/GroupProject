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

    @Autowired
    public OrderService(OrderRepository orderRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
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

        // deduct stock for each item
        for (OrderItem item : items) {
            orderRepository.deductStock(item.getProductId(), item.getQuantity());
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