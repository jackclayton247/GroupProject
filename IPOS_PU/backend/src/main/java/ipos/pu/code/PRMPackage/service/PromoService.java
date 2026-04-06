package ipos.pu.code.PRMPackage.service;

import java.time.LocalDate;
import java.util.List;

import ipos.pu.code.PRMPackage.repository.PromoRepository;
import ipos.pu.code.model.Promotion;
import ipos.pu.code.model.PromotionProduct;

public class PromoService {
    private final PromoRepository promoRepository = new PromoRepository();
    public String createPromotion(String name, LocalDate start, LocalDate end) {
        System.out.println("Service");

        int response = promoRepository.createPromotion(name, start, end);

        if (response == 0) {
            return "Success";
        }
        else if (response == 1) {
            return "Promotion already exists with this name";
        }
        else {
            return "error";
        }
        
    }
    
    public String cancelPromotion(String name) {
        System.out.println("Service");

        int response = promoRepository.cancelPromotion(name);

        if (response == 0) {
            return "Success";
        }
        else if (response == 1) {
            return "No promtion exists with this name";
        }
        else {
            return "error";
        }
    }
    public List<Promotion> getAll() {
    return promoRepository.getAll();
}
}
