package ipos.pu.code.PRMPackage.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ipos.pu.code.config.DatabaseConfig;
import ipos.pu.code.model.Promotion;
import ipos.pu.code.model.PromotionProduct;


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
    public int updateDiscount(int productId, float discount, String promotionName) {
        String sql = "UPDATE promotion_product SET discount = ? WHERE product_id = ? AND promotion_name = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setFloat(1, discount);
            stmt.setInt(2, productId);
            stmt.setString(3, promotionName);
            int rows = stmt.executeUpdate();
            if (rows == 0) return 1; // not found
            return 0; // success
        } catch (Exception e) {
            e.printStackTrace();
            return 2; // error
        }
    }

    public List<PromotionProduct> getAll(String promotionName) {
        String sql = "SELECT * FROM promotion_product WHERE promotion_name = ?";
        List<PromotionProduct> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, promotionName);
            ResultSet products = stmt.executeQuery();

            while (products.next()) {
            PromotionProduct pp = new PromotionProduct();

            pp.setProductId(products.getInt("product_id"));
            pp.setDiscount(products.getFloat("discount"));

            // Create Promotion object for relation
            Promotion promotion = new Promotion();
            promotion.setName(products.getString("promotion_name"));

            pp.setPromotion(promotion);

            list.add(pp);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
