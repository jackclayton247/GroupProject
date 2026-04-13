package ipos.pu.code.CommsPackage;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    public String processPayment(long amountInPence, String currency) {
        try {
            Stripe.apiKey = secretKey;
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPence)
                .setCurrency(currency)
                .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        } catch (Exception e) {
            System.err.println("[PaymentService] Stripe error: " + e.getMessage());
            return null;
        }
    }
}