package ipos.pu.code.PRMPackage.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import ipos.pu.code.PRMPackage.dto.PromoRequest;
import ipos.pu.code.PRMPackage.service.PromoService;
import ipos.pu.code.model.PromotionProduct;

@RestController
@RequestMapping("/promo")
@CrossOrigin(origins = "*")
public class PromoController {
    private final PromoService promoService = new PromoService();

    @PostMapping("/create")
    public String create(@RequestBody PromoRequest request) {
        String name = request.getName();
        LocalDate start = request.getStart();
        LocalDate end = request.getEnd();

        if (name == null || start == null || end == null) {
            return "parameter error";
        }

        return promoService.createPromotion(name, start, end);
    }

    @PutMapping("/update")
    public String update(@RequestBody PromoRequest request) {
        String name = request.getName();
        LocalDate start = request.getStart();
        LocalDate end = request.getEnd();

        if (name == null || start == null || end == null) {
            return "parameter error";
        }

        return promoService.updatePromotion(name, start, end);
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam String name) {
        if (name == null) {
            return "parameter error";
        }
        return promoService.cancelPromotion(name);
    }

    @GetMapping("/all")
    public List<PromotionProduct> getAll() {
        return promoService.getAll();
    }

    /**
     * Get all active promotions with their products and descriptions.
     * Used by the public promotions page.
     */
    @GetMapping("/active")
    public String getActivePromotions() {
        return promoService.getActivePromotions();
    }

    /**
     * Record a click on a campaign (campaign view).
     */
    @PostMapping("/click/campaign")
    public String recordCampaignClick(@RequestBody Map<String, String> request) {
        String campaignName = request.get("campaignName");
        if (campaignName == null) return "error: missing campaignName";
        return promoService.recordCampaignClick(campaignName);
    }

    /**
     * Record a click on a specific item within a campaign (item added to cart).
     */
    @PostMapping("/click/item")
    public String recordItemClick(@RequestBody Map<String, Object> request) {
        String campaignName = (String) request.get("campaignName");
        int productId = ((Number) request.get("productId")).intValue();
        int quantity = request.containsKey("quantity") ? ((Number) request.get("quantity")).intValue() : 1;
        if (campaignName == null) return "error: missing campaignName";
        return promoService.recordItemClick(campaignName, productId, quantity);
    }

    /**
     * Record a purchase of items from a campaign (order completed).
     */
    @PostMapping("/click/purchase")
    public String recordPurchase(@RequestBody Map<String, Object> request) {
        String campaignName = (String) request.get("campaignName");
        int productId = ((Number) request.get("productId")).intValue();
        int quantity = request.containsKey("quantity") ? ((Number) request.get("quantity")).intValue() : 1;
        if (campaignName == null) return "error: missing campaignName";
        return promoService.recordPurchase(campaignName, productId, quantity);
    }
}
