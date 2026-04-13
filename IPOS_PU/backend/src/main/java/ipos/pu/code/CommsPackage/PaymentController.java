package ipos.pu.code.CommsPackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/comms")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    public String processPayment(@RequestBody Map<String, Object> request) {
        String merchantId = (String) request.get("merchantID");
        String orderId = (String) request.get("orderID");
        String fullName = (String) request.get("fullName");
        String address = (String) request.get("address");
        String cardFirstFour = (String) request.get("cardFirstFour");
        String cardLastFour = (String) request.get("cardLastFour");
        double amount = Double.parseDouble(request.get("amount").toString());

        boolean invalid = amount <= 0 || cardFirstFour == null || cardFirstFour.isEmpty() || cardLastFour == null || cardLastFour.isEmpty();

        String status;
        if (invalid) {
            status = "declined";
        } else {
            String clientSecret = paymentService.processPayment((long)(amount * 100), "gbp");
            status = clientSecret != null ? "success" : "declined";
        }

        jdbcTemplate.update(
            "INSERT INTO ca_payments (merchant_id, order_id, payee, address, card_first_four, card_last_four, amount, status, payment_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
            merchantId, orderId, fullName, address, cardFirstFour, cardLastFour, amount, status
        );

        return status;
    }
}