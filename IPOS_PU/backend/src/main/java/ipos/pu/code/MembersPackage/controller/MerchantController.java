package ipos.pu.code.MembersPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import ipos.pu.code.MembersPackage.service.MerchantService;

@RestController
@RequestMapping("/merchant")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class MerchantController {

    private final MerchantService merchantService;

    @Autowired
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /**
     * Submit a commercial membership application.
     * Called by PU frontend or directly via curl.
     * Accepts email as query param (simple) or full details in body.
     */
    @PostMapping("/application")
    public String application(@RequestParam String email,
                               @RequestBody(required = false) Map<String, String> details) {
        if (details == null) {
            details = Map.of();
        }
        return merchantService.submitApplication(email, details);
    }

    /**
     * Called by SA to approve a commercial membership application.
     * SA sends the email of the approved user.
     */
    @PostMapping("/response")
    public String response(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return "error: missing email";
        }
        return merchantService.response(email);
    }

    /**
     * Called by SA to reject a commercial membership application.
     */
    @PostMapping("/reject")
    public String reject(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return "error: missing email";
        }
        return merchantService.reject(email);
    }

    /**
     * Get all pending merchant applications.
     * Used by admin/SA.
     */
    @GetMapping("/applications")
    public String getPendingApplications() {
        return merchantService.getPendingApplications();
    }
}
