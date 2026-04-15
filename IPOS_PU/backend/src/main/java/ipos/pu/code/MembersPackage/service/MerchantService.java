package ipos.pu.code.MembersPackage.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.Map;

import ipos.pu.code.MembersPackage.repository.UserRepository;
import ipos.pu.code.config.DatabaseConfig;

@Service
public class MerchantService {

    private static final String SA_BASE_URL = System.getenv().getOrDefault("SA_BASE_URL", "http://host.docker.internal:8081");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final UserRepository userRepository;

    @Autowired
    public MerchantService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Handles a commercial membership application.
     * Stores it in PU database and forwards to SA for approval.
     */
    public String submitApplication(String email, Map<String, String> details) {
        // Check user exists
        if (!userExists(email)) {
            return "error: user not found. Please register first.";
        }

        // Check if already a merchant
        if (userRepository.getMerchant(email)) {
            return "error: user is already a merchant";
        }

        // Check for existing pending application
        if (hasPendingApplication(email)) {
            return "error: application already pending";
        }

        // Store application in PU database
        String companyName = details.getOrDefault("companyName", "");
        String companyRegNumber = details.getOrDefault("companyRegNumber", "");
        String directorName = details.getOrDefault("directorName", "");
        String businessType = details.getOrDefault("businessType", "");
        String address = details.getOrDefault("address", "");
        String phone = details.getOrDefault("phone", "");

        String insertSql = "INSERT INTO merchant_applications (email, company_name, company_reg_number, director_name, business_type, address, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, email);
            stmt.setString(2, companyName);
            stmt.setString(3, companyRegNumber);
            stmt.setString(4, directorName);
            stmt.setString(5, businessType);
            stmt.setString(6, address);
            stmt.setString(7, phone);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return "error: failed to store application - " + e.getMessage();
        }

        // Forward to SA for diligence checks
        forwardToSA(email, companyName, companyRegNumber, directorName, businessType, address, phone);

        return "application submitted successfully";
    }

    /**
     * Simple merchant application (email only) - for backwards compatibility.
     */
    public String merchantRequest(String email) {
        return submitApplication(email, Map.of());
    }

    /**
     * Forwards the commercial membership application to IPOS-SA.
     */
    private void forwardToSA(String email, String companyName, String regNumber,
                              String directorName, String businessType, String address, String phone) {
        try {
            String json = """
            {
                "userEmail": "%s",
                "companyName": "%s",
                "companyRegNumber": "%s",
                "directorName": "%s",
                "businessType": "%s",
                "address": "%s",
                "phone": "%s"
            }
            """.formatted(email, companyName, regNumber, directorName, businessType, address, phone);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SA_BASE_URL + "/api/membership/request"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[MerchantService] SA response: " + response.statusCode() + " - " + response.body());

        } catch (Exception e) {
            // SA might not be running - application is still stored locally
            System.out.println("[MerchantService] Could not forward to SA (may be offline): " + e.getMessage());
        }
    }

    /**
     * Called by SA when a commercial membership application is approved/rejected.
     */
    public String response(String email) {
        if (!userExists(email)) {
            return "error: user not found";
        }

        // Make the user a merchant
        String result = userRepository.makeMerchant(email);
        if (result.equals("Success")) {
            // Update application status
            updateApplicationStatus(email, "approved");
            return "User " + email + " is now a merchant";
        }
        return result;
    }

    /**
     * Called by SA to reject a commercial membership application.
     */
    public String reject(String email) {
        if (!userExists(email)) {
            return "error: user not found";
        }
        updateApplicationStatus(email, "rejected");
        return "Application for " + email + " has been rejected";
    }

    /**
     * Get all pending applications.
     */
    public String getPendingApplications() {
        String sql = "SELECT * FROM merchant_applications WHERE status = 'pending' ORDER BY applied_at DESC";
        StringBuilder result = new StringBuilder("[");
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) result.append(",");
                result.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"email\":\"").append(rs.getString("email")).append("\",")
                    .append("\"companyName\":\"").append(rs.getString("company_name")).append("\",")
                    .append("\"companyRegNumber\":\"").append(rs.getString("company_reg_number")).append("\",")
                    .append("\"directorName\":\"").append(rs.getString("director_name")).append("\",")
                    .append("\"businessType\":\"").append(rs.getString("business_type")).append("\",")
                    .append("\"address\":\"").append(rs.getString("address")).append("\",")
                    .append("\"phone\":\"").append(rs.getString("phone")).append("\",")
                    .append("\"status\":\"").append(rs.getString("status")).append("\",")
                    .append("\"appliedAt\":\"").append(rs.getString("applied_at")).append("\"")
                    .append("}");
                first = false;
            }
            result.append("]");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    private void updateApplicationStatus(String email, String status) {
        String sql = "UPDATE merchant_applications SET status = ?, decided_at = NOW() WHERE email = ? AND status = 'pending'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasPendingApplication(String email) {
        String sql = "SELECT id FROM merchant_applications WHERE email = ? AND status = 'pending'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean userExists(String email) {
        String sql = "SELECT email FROM user WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
