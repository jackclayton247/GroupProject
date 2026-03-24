package ipos.pu.code.MembersPackage.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ipos.pu.code.config.DatabaseConfig;

public class UserRepository {

    public int addUser(String email, String password) {
        String sql = "INSERT INTO user (email, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            stmt.executeUpdate();
            return 0;
        }
        catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.out.println("User already exists with this email.");
            return 1;
        }
        catch (Exception e) {
            System.out.println("an error occured trying to add this user to the database");
            return 2;
        }
    }

    public int validateUser(String email, String password) {
        System.out.println("attempting to validate user");
        //check login credentials
        String sql = "SELECT * FROM user WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            ResultSet user = stmt.executeQuery();

            if (user.next()) {
                // row exists
                String userEmail = user.getString("email");
                String userPassword = user.getString("password");

                System.out.println(userEmail);
                System.out.println(userPassword);

                if (userPassword.equals(password)) {
                    System.out.println("success");
                    return 0; 
                } else {
                    System.out.println("password is incorrect");
                    return 1; 
                }

            } else {
                System.out.println("user not found");
                return 2; 
            }
        }
        catch (Exception e) {
            e.printStackTrace(); 
            return 3;
        }
            
    }
}