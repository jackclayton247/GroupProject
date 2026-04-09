package ipos.pu.code.MembersPackage.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ipos.pu.code.config.DatabaseConfig;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    public int addUser(String email, String password) {
        String sql = "INSERT INTO user (email, password, merchant) VALUES (?, ?, false)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            stmt.executeUpdate();
            return 0; //user added
        }
        catch (java.sql.SQLIntegrityConstraintViolationException e) {
            return 1; //User already exists with this email.
        }
        catch (Exception e) {
            return 2; //an error occured trying to add this user to the database
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
                    return 0; //success
                } else {
                    return 1; //password is incorrect
                }

            } else {
                return 2; //user not found
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return 3;
        }

    }

    public boolean getMerchant(String email) {
        String sql = "SELECT merchant FROM user WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("merchant");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public String makeMerchant(String email){
        String sql = "UPDATE user SET merchant = true WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}