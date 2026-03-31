package ipos.pu.code.SalesPackage;

import java.util.List;

public class OrderRequest {
    private String userEmail;
    private String deliveryAddress;
    private String cardType;
    private String cardFirstFour;
    private String cardLastFour;
    private String cardExpiry;
    private List<OrderItem> items;

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getCardFirstFour() { return cardFirstFour; }
    public void setCardFirstFour(String cardFirstFour) { this.cardFirstFour = cardFirstFour; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public String getCardExpiry() { return cardExpiry; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
}