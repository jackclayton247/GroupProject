package ipos.pu.code.MembersPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import ipos.pu.code.MembersPackage.service.MerchantService;

@RestController
@RequestMapping("/merchant")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MerchantController {

    private final MerchantService merchantService;

    @Autowired
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/application")
    public String application(@RequestParam String email) {
        return merchantService.merchantRequest(email);
    }
    @PostMapping("/response")
    public String response(@RequestBody Map<String, String> request) {
    String email = request.get("email");

    if (email == null || email.isEmpty()) {
        return "error: missing email";
    }

    return merchantService.response(email);
}
}