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

    /**
     * Add a non-commercial user with auto-generated password.
     * force_password_change is set to true so they must change on first login.
     */
    public int addUserWithGeneratedPassword(String email, String password) {
        String sql = "INSERT INTO user (email, password, merchant, force_password_change) VALUES (?, ?, false, true)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return 0;
        }
        catch (java.sql.SQLIntegrityConstraintViolationException e) {
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
    }

    public int validateUser(String email, String password) {
        System.out.println("attempting to validate user");
        String sql = "SELECT * FROM user WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            ResultSet user = stmt.executeQuery();

            if (user.next()) {
                String userPassword = user.getString("password");

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

    public boolean getForcePasswordChange(String email) {
        String sql = "SELECT force_password_change FROM user WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("force_password_change");
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

    public int changePassword(String email, String newPassword) {
        String sql = "UPDATE user SET password = ?, force_password_change = false WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            int rows = stmt.executeUpdate();
            return rows > 0 ? 0 : 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
    }
}
