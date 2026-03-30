package ipos.pu.code.PRMPackage.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import ipos.pu.code.config.DatabaseConfig;


public class PromoProductRepository {
    public int addProduct(int productId, float discount, String promotionName){
        String sql = "INSERT INTO promotion_product (product_id, discount, promotion_name) VALUES (?, ?, ?);";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setFloat(2, discount);
            stmt.setString(3, promotionName);

            stmt.executeUpdate();
            return 0; //product added
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1; //unkown error
        }
        
    }
    public int removeProduct(int productId) {
        String sql = "DELETE FROM promotion_product WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted == 0) {
                return 1; //no product removed
            }
            return 0; //product removed
        }
        catch (Exception e) {
            e.printStackTrace();
            return 2; //unknown error
        }
    }
}
