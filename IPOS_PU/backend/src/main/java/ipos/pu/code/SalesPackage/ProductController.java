package ipos.pu.code.SalesPackage.catalogue;

import ipos.pu.code.model.ProductCache;
import ipos.pu.code.SalesPackage.catalogue.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<ProductCache> getProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return productRepository.findByItemIdStartingWithAndIsActive(category, 1);
        }
        return productRepository.findByIsActive(1);
    }
}