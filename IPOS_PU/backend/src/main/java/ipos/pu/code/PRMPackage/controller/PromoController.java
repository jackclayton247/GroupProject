package ipos.pu.code.PRMPackage.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ipos.pu.code.PRMPackage.service.PromoService;

@RestController
@RequestMapping("/promo")
public class PromoController {
    private final PromoService promoService = new PromoService();
    @PostMapping("/create")
    public String create() {
        
        return "OK";
    }
}
