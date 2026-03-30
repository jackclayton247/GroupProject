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
}
