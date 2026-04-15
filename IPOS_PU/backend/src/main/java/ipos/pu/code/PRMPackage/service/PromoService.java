package ipos.pu.code.PRMPackage.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import ipos.pu.code.PRMPackage.repository.PromoRepository;
import ipos.pu.code.config.DatabaseConfig;
import ipos.pu.code.model.PromotionProduct;

public class PromoService {
    private final PromoRepository promoRepository = new PromoRepository();

    public String createPromotion(String name, LocalDate start, LocalDate end) {
        int response = promoRepository.createPromotion(name, start, end);
        if (response == 0) return "Success";
        else if (response == 1) return "Promotion already exists with this name";
        else return "error";
    }

    public String updatePromotion(String name, LocalDate start, LocalDate end) {
        int response = promoRepository.updatePromotion(name, start, end);
        if (response == 0) return "Success";
        else if (response == 1) return "Promotion not found";
        else return "error";
    }

    public String cancelPromotion(String name) {
        // First delete promotion products, then delete campaign clicks, then the promotion
        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement stmt1 = conn.prepareStatement("DELETE FROM campaign_clicks WHERE promotion_name = ?");
            stmt1.setString(1, name);
            stmt1.executeUpdate();
            stmt1.close();

            PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM promotion_product WHERE promotion_name = ?");
            stmt2.setString(1, name);
            stmt2.executeUpdate();
            stmt2.close();

            PreparedStatement stmt3 = conn.prepareStatement("DELETE FROM promotion WHERE name = ?");
            stmt3.setString(1, name);
            int rows = stmt3.executeUpdate();
            stmt3.close();

            if (rows == 0) return "No promotion exists with this name";
            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    public List<PromotionProduct> getAll() {
        return promoRepository.getAll();
    }

    /**
     * Get all active promotions with product details for the public promotions page.
     */
    public String getActivePromotions() {
        String sql = "SELECT p.name, p.start, p.end, " +
                     "pp.product_id, pp.discount, " +
                     "pc.item_id, pc.description, pc.price, pc.stock_quantity " +
                     "FROM promotion p " +
                     "JOIN promotion_product pp ON p.name = pp.promotion_name " +
                     "JOIN product_cache pc ON pp.product_id = pc.product_id " +
                     "WHERE p.end >= CURDATE() " +
                     "ORDER BY p.name, pp.product_id";

        StringBuilder result = new StringBuilder("[");
        String currentCampaign = null;
        boolean firstCampaign = true;
        boolean firstItem = true;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                if (!name.equals(currentCampaign)) {
                    if (currentCampaign != null) {
                        result.append("]},"); // close items array and previous campaign
                    }
                    if (!firstCampaign) {
                        // already appended comma above
                    }
                    currentCampaign = name;
                    firstCampaign = false;
                    firstItem = true;

                    result.append("{")
                        .append("\"name\":\"").append(escapeJson(name)).append("\",")
                        .append("\"startDate\":\"").append(rs.getString("start")).append("\",")
                        .append("\"endDate\":\"").append(rs.getString("end")).append("\",")
                        .append("\"items\":[");
                }

                if (!firstItem) result.append(",");
                firstItem = false;

                result.append("{")
                    .append("\"productId\":").append(rs.getInt("product_id")).append(",")
                    .append("\"itemId\":\"").append(escapeJson(rs.getString("item_id"))).append("\",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                    .append("\"unitCost\":").append(rs.getDouble("price")).append(",")
                    .append("\"discount\":").append(rs.getFloat("discount")).append(",")
                    .append("\"availability\":").append(rs.getInt("stock_quantity"))
                    .append("}");
            }

            if (currentCampaign != null) {
                result.append("]}"); // close last items array and campaign
            }
            result.append("]");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Record a campaign-level click.
     */
    public String recordCampaignClick(String campaignName) {
        String sql = "INSERT INTO campaign_clicks (promotion_name, click_type) VALUES (?, 'campaign')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, campaignName);
            stmt.executeUpdate();
            return "recorded";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Record item-level clicks (added to cart from a campaign).
     */
    public String recordItemClick(String campaignName, int productId, int quantity) {
        String sql = "INSERT INTO campaign_clicks (promotion_name, product_id, click_type) VALUES (?, ?, 'item_added')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < quantity; i++) {
                stmt.setString(1, campaignName);
                stmt.setInt(2, productId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            return "recorded";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Record purchase of items from a campaign.
     */
    public String recordPurchase(String campaignName, int productId, int quantity) {
        String sql = "INSERT INTO campaign_clicks (promotion_name, product_id, click_type) VALUES (?, ?, 'purchased')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < quantity; i++) {
                stmt.setString(1, campaignName);
                stmt.setInt(2, productId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            return "recorded";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
