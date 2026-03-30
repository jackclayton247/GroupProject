package ipos.pu.code.PRMPackage.dto;

public class PromoProductRequest {
    private int productId;
    private float discount;
    private String promotionName;

    //getters
    public int getProductId() {
        return productId;
    }

    public float getDiscount() {
        return discount;
    }

    public String getPromotionName() {
        return promotionName;
    }

    //setters
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }
}
