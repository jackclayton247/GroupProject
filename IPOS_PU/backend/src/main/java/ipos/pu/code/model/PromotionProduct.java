package ipos.pu.code.model;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class PromotionProduct {
    @Id
    private int productId;

    @ManyToOne
    @JoinColumn(name = "name")
    private Promotion promotion;

    private float discount;
}
