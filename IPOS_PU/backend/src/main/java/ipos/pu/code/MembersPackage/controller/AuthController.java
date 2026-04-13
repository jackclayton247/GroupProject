package ipos.pu.code.MembersPackage.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import ipos.pu.code.MembersPackage.service.AuthService;
import ipos.pu.code.CommsPackage.EmailService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthService authService = new AuthService();

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request, HttpSession session) {

        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return Map.of("success", false, "message", "Missing email or password");
        }

        int outcome = authService.login(email, password);

        if (outcome == 0) {
            session.setAttribute("userEmail", email);
            boolean merchant = authService.getMerchant(email);
            boolean forcePasswordChange = authService.getForcePasswordChange(email);
            return Map.of(
                "success", true,
                "email", email,
                "merchant", merchant,
                "forcePasswordChange", forcePasswordChange
            );
        } else if (outcome == 1) {
            return Map.of("success", false, "message", "Password is incorrect");
        } else if (outcome == 2) {
            return Map.of("success", false, "message", "User not found");
        } else {
            return Map.of("success", false, "message", "Unknown error");
        }
    }

    /**
     * Standard signup where user picks their own password.
     * Kept for backwards compatibility.
     */
    @PostMapping("/signup")
    public String signup(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return "Missing email or password";
        }

        int outcome = authService.signup(email, password);

        if (outcome == 0) return "account created";
        else if (outcome == 1) return "User already exists with this email.";
        else return "an error occured trying to add this user to the database";
    }

    /**
     * Non-commercial membership registration.
     * Per the brief: password is auto-generated (10 random chars) and emailed to the user.
     * The user must change their password on first login.
     */
    @PostMapping("/register")
    public Map<String, Object> registerNonCommercial(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return Map.of("success", false, "message", "Email is required");
        }

        String generatedPassword = authService.signupNonCommercial(email);

        if (generatedPassword != null) {
            // Send the generated password via email
            try {
                emailService.sendEmail(
                    email,
                    "Welcome to IPOS-PU - Your Login Details",
                    "Welcome to IPOS-PU!\n\n" +
                    "Your account has been created successfully.\n\n" +
                    "Login details:\n" +
                    "  Username: " + email + "\n" +
                    "  Password: " + generatedPassword + "\n\n" +
                    "You will be required to change your password when you first log in.\n\n" +
                    "Thank you for registering with IPOS-PU."
                );
            } catch (Exception e) {
                System.err.println("[AuthController] Failed to send registration email: " + e.getMessage());
            }

            return Map.of(
                "success", true,
                "message", "Account created! Your login details have been sent to " + email
            );
        } else {
            return Map.of("success", false, "message", "User already exists with this email.");
        }
    }

    /**
     * Change password endpoint.
     * Used when force_password_change is true (first login after auto-generated password).
     */
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (email == null || currentPassword == null || newPassword == null) {
            return Map.of("success", false, "message", "Missing required fields");
        }

        if (newPassword.length() < 6) {
            return Map.of("success", false, "message", "New password must be at least 6 characters");
        }

        // Verify current password
        int loginResult = authService.login(email, currentPassword);
        if (loginResult != 0) {
            return Map.of("success", false, "message", "Current password is incorrect");
        }

        // Change password
        int changeResult = authService.changePassword(email, newPassword);
        if (changeResult == 0) {
            return Map.of("success", true, "message", "Password changed successfully");
        } else {
            return Map.of("success", false, "message", "Failed to change password");
        }
    }
}
