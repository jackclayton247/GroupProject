package ipos.pu.code.PRMPackage.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import ipos.pu.code.config.DatabaseConfig;
import ipos.pu.code.model.Promotion;
import ipos.pu.code.model.PromotionProduct;

public class PromoRepository {
    public int createPromotion(String name, LocalDate start, LocalDate end) {
        System.out.println("Repo");
        String sql = "INSERT INTO promotion (name, start, end) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDate(2, Date.valueOf(start));
            stmt.setDate(3, Date.valueOf(end));

            stmt.executeUpdate();
            return 0; //Promotion created
        }
        catch (java.sql.SQLIntegrityConstraintViolationException e) {
            return 1; //Promotion already exists with this name
        }
        catch (Exception e) {
            e.printStackTrace(); 
            return 2; //unknown error
        }
    }

    public int cancelPromotion(String name) {
        System.out.println("Repo");
        String sql = "DELETE FROM promotion WHERE name = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted == 0) {
                return 1; //no promtion with this name
            }
            return 0; //promtion cancelled
        }
        catch (Exception e) {
            e.printStackTrace();
            return 2; //unknown error
        }
    }
    public List<PromotionProduct> getAll() {
        String sql = "SELECT * FROM promotion_product";
        List<PromotionProduct> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PromotionProduct pp = new PromotionProduct();

                pp.setProductId(rs.getInt("product_id"));
                pp.setDiscount(rs.getFloat("discount"));

                // Map promotion (foreign key)
                Promotion promotion = new Promotion();
                promotion.setName(rs.getString("promotion_name"));

                pp.setPromotion(promotion);

                list.add(pp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}