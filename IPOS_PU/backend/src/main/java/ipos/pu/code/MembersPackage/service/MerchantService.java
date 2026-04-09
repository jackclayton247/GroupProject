package ipos.pu.code.MembersPackage.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import ipos.pu.code.MembersPackage.repository.UserRepository;

@Service
public class MerchantService {

    private final UserRepository userRepository;

    @Autowired
    public MerchantService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String merchantRequest(String email) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String json = """
            {
                "userEmail": "%s"
            }
            """.formatted(email);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/membership/request"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.statusCode());
            System.out.println(response.body());
            return "Success";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String response(String email) {
        // Check if user exists first
        if (!userExists(email)) {
            return "User not found";
        }

        // Make merchant using JDBC method
        String result = userRepository.makeMerchant(email);
        if (result.equals("Success")) {
            return "User " + email + " is now a merchant";
        }
        return result;
    }

    private boolean userExists(String email) {
        // Check if user exists by trying to validate with empty password check
        String sql = "SELECT email FROM user WHERE email = ?";
        try (java.sql.Connection conn = ipos.pu.code.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            java.sql.ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}