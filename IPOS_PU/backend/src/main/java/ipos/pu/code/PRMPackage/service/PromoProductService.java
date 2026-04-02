package ipos.pu.code.PRMPackage.service;

import java.util.List;

import ipos.pu.code.PRMPackage.repository.PromoProductRepository;
import ipos.pu.code.model.PromotionProduct;

public class PromoProductService {
    private final PromoProductRepository promoProductRepository = new PromoProductRepository();

    public String addProduct(int productId, float discount, String promotionName) {
        int response = promoProductRepository.addProduct(productId, discount, promotionName);
        if (response == 0) {
            return "success";
        }
        else {
            return "unknown error";
        }
    }
    public String removeProduct(int productId) {
        int response = promoProductRepository.removeProduct(productId);
        if (response == 0) {
            return "success";
        }
        else if (response == 1) {
            return "could not find product";
        }
        else {
            return "unknown error";
        }
    }
    public List<PromotionProduct> getAll(String name) {
        return promoProductRepository.getAll(name);
    }
}
