package ipos.pu.code.SalesPackage.catalogue;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "product_id")
    private int productId;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "description")
    private String description;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "units_in_pack")
    private int unitsInPack;

    @Column(name = "price")
    private double price;

    @Column(name = "stock_quantity")
    private int availability;

    @Column(name = "is_active")
    private int isActive;

    public int getProductId() { return productId; }
    public String getItemId() { return itemId; }
    public String getDescription() { return description; }
    public String getPackageType() { return packageType; }
    public int getUnitsInPack() { return unitsInPack; }
    public double getPrice() { return price; }
    public int getAvailability() { return availability; }
    public int getIsActive() { return isActive; }
}
