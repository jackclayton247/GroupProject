package ipos.pu.code.PRMPackage.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ipos.pu.code.PRMPackage.dto.PromoProductRequest;
import ipos.pu.code.PRMPackage.service.PromoProductService;
import ipos.pu.code.model.PromotionProduct;

@RestController
@RequestMapping("/promo-product")
public class PromoProductController {
    private final PromoProductService promoProductService = new PromoProductService();

    @PostMapping("/add")
    public String addProduct(@RequestBody PromoProductRequest request) {
        
        int productId = request.getProductId();
        float discount = request.getDiscount();
        String promotionName = request.getPromotionName().trim();

        if (productId == 0 || discount == 0.0f || promotionName == null) {
            return "request error";
        }
        return promoProductService.addProduct(productId, discount, promotionName);
    }
    @PostMapping("/remove")
    public String removeProduct(int productId) {
        return promoProductService.removeProduct(productId);
    }
    @GetMapping("/all")
    public List<PromotionProduct> getAll(@RequestParam String name) {
        return promoProductService.getAll(name);
    }
}