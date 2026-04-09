package ipos.pu.code.MembersPackage.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import ipos.pu.code.MembersPackage.service.AuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthService authService = new AuthService();

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request, HttpSession session) {

        System.out.println("Login endpoint hit");

        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return Map.of("success", false, "message", "Missing email or password");
        }

        int outcome = authService.login(email, password);

        if (outcome == 0) {
            session.setAttribute("userEmail", email);
            boolean merchant = authService.getMerchant(email);
            return Map.of("success", true, "email", email, "merchant", merchant);
        } else if (outcome == 1) {
            return Map.of("success", false, "message", "Password is incorrect");
        } else if (outcome == 2) {
            return Map.of("success", false, "message", "User not found");
        } else {
            return Map.of("success", false, "message", "Unknown error");
        }

    }
    @PostMapping("/signup")
    public String signup(@RequestBody Map<String, String> request) {
        System.out.println("Signup endpoint hit");

        String email = request.get("email");
        String password = request.get("password");
        
        //check for null
        if (email == null || password == null) {
            return "Missing email or password";
        }

        int outcome = authService.signup(email, password);
        
        //success
        if (outcome == 0) {
            return "account created";
            //refer to login page with account created
        }
        //errors
        else if (outcome == 1) {return "User already exists with this email.";}
        else {return "an error occured trying to add this user to the database";}
    }
}
