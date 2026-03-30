package ipos.pu.code.PRMPackage.controller;

import java.time.LocalDate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ipos.pu.code.PRMPackage.dto.PromoRequest;
import ipos.pu.code.PRMPackage.service.PromoService;

@RestController
@RequestMapping("/promo")
public class PromoController {
    private final PromoService promoService = new PromoService();

    @PostMapping("/create")
    public String create(@RequestBody PromoRequest request) {
        System.out.println("Controller");

        String name = request.getName();
        LocalDate start = request.getStart();
        LocalDate end = request.getEnd();

        if (name == null || start == null || end == null) {
            return "parameter error";
        }
        
        return promoService.createPromotion(name, start, end);
    }
    @PostMapping("/cancel")
    public String cancel(@RequestParam String name) {
        System.out.println("Controller");

        if (name == null) {
            return "parameter error";
        }

        return promoService.cancelPromotion(name);
    }

}
