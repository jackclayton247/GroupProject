package ipos.pu.code.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity 
public class ProductsCache {
    @Id
    @GeneratedValue
    private Long id;
    
    private String productName;
    private Double price;
    private Integer stock;
    

    //getters and setters
}
