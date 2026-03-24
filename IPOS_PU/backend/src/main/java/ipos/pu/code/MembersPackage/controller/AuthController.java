package ipos.pu.code.MembersPackage.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ipos.pu.code.MembersPackage.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService = new AuthService();

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> request) {

        System.out.println("Login endpoint hit");

        String username = request.get("username");
        String password = request.get("password");

        System.out.println(username);
        System.out.println(password);

        return "OK";
    }
    @PostMapping("/signup")
    public String signup(@RequestBody Map<String, String> request) {
        System.out.println("Signup endpoint hit");

        String username = request.get("username");
        String password = request.get("password");

        System.out.println(username);
        System.out.println(password);

        authService.signup(username, password);

        return "OK";
    }
}
