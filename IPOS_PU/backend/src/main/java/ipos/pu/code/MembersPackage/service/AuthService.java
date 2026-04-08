package ipos.pu.code.MembersPackage.service;

import ipos.pu.code.MembersPackage.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    public int signup(String email, String password) {
        return userRepository.addUser(email, password);
    }
    public int login(String email, String password) {
        return userRepository.validateUser(email, password);
    }

    public boolean getMerchant(String email) {
        return userRepository.getMerchant(email);
    }

}