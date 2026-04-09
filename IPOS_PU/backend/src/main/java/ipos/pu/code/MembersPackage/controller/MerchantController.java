package ipos.pu.code.MembersPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ipos.pu.code.MembersPackage.service.MerchantService;

@RestController
@RequestMapping("/merchant")
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
}