package ipos.pu.code.CommsPackage;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comms")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public String sendEmail(@RequestBody Map<String, String> request) {
        emailService.sendEmail(
            request.get("to"),
            request.get("subject"),
            request.get("body")
        );
        return "email sent";
    }
}