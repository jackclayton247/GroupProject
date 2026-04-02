package ipos.pu.code.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PromotionProduct {
    @Id
    private int productId;

    @ManyToOne
    @JoinColumn(name = "promotion_name")
    private Promotion promotion;

    private float discount;


    //getters
        public int getProductId() {
        return productId;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public float getDiscount() {
        return discount;
    }

    //setters
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }
}

