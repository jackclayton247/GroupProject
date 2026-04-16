package ipos.pu.code.model;

import java.io.Serializable;
import java.util.Objects;

public class PromotionProductId implements Serializable {
    private int productId;
    private String promotion; // matches the field name in PromotionProduct; maps to promotion's @Id (name)

    public PromotionProductId() {}

    public PromotionProductId(int productId, String promotion) {
        this.productId = productId;
        this.promotion = promotion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionProductId that = (PromotionProductId) o;
        return productId == that.productId && Objects.equals(promotion, that.promotion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, promotion);
    }
}
