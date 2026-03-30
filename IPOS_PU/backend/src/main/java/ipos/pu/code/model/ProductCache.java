package ipos.pu.code.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity 
public class ProductCache {
    @Id
    private int productId;
    
    private String itemId;
    private String description;
    private String packageType;
    private int unitsInPack;
    private double price;
    private double vatRate;
    private int stockQuantity;
    private int minStockLevel;
}
