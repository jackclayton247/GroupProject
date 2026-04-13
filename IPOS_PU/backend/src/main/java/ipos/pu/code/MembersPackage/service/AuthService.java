package ipos.pu.code.MembersPackage.service;

import ipos.pu.code.MembersPackage.repository.UserRepository;

import java.security.SecureRandom;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";

    public int signup(String email, String password) {
        return userRepository.addUser(email, password);
    }

    /**
     * Register a non-commercial member with auto-generated password.
     * Returns the generated password on success, or null on failure.
     */
    public String signupNonCommercial(String email) {
        String generatedPassword = generatePassword(10);
        int result = userRepository.addUserWithGeneratedPassword(email, generatedPassword);
        if (result == 0) {
            return generatedPassword;
        }
        return null;
    }

    public int login(String email, String password) {
        return userRepository.validateUser(email, password);
    }

    public boolean getMerchant(String email) {
        return userRepository.getMerchant(email);
    }

    public boolean getForcePasswordChange(String email) {
        return userRepository.getForcePasswordChange(email);
    }

    public int changePassword(String email, String newPassword) {
        return userRepository.changePassword(email, newPassword);
    }

    /**
     * Generate a random password of given length with letters, numbers and special symbols.
     */
    private String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
