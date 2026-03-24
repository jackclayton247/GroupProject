package ipos.pu.code.services;

import ipos.pu.code.model.User;
import ipos.pu.code.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    public boolean signup(String username, String password) {
        System.out.println("attempting sign up");
        try (Connection conn = DatabaseConfig.getConnection()) {
            return true;
        } 
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

