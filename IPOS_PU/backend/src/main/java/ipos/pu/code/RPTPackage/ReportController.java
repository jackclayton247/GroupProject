package ipos.pu.code.RPTPackage;

import ipos.pu.code.config.DatabaseConfig;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    /**
     * IPOS-PU-RPT: Sales within a specified period.
     * Matches Appendix 8 format.
     */
    @GetMapping("/sales")
    public String getSalesReport(@RequestParam String startDate, @RequestParam String endDate) {
        String sql = "SELECT pc.item_id, pc.description, " +
                     "SUM(oi.quantity) AS sold_packs, " +
                     "oi.unit_price, " +
                     "SUM(oi.quantity * oi.unit_price) AS total " +
                     "FROM order_items oi " +
                     "JOIN orders o ON oi.order_id = o.order_id " +
                     "JOIN product_cache pc ON oi.product_id = pc.product_id " +
                     "WHERE o.order_date BETWEEN ? AND ? " +
                     "GROUP BY pc.item_id, pc.description, oi.unit_price " +
                     "ORDER BY pc.item_id";

        StringBuilder result = new StringBuilder("{\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\",\"items\":[");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate + " 23:59:59");
            ResultSet rs = stmt.executeQuery();

            boolean first = true;
            int totalPacks = 0;
            double totalRevenue = 0;

            while (rs.next()) {
                if (!first) result.append(",");
                int sold = rs.getInt("sold_packs");
                double itemTotal = rs.getDouble("total");
                totalPacks += sold;
                totalRevenue += itemTotal;

                result.append("{")
                    .append("\"itemId\":\"").append(rs.getString("item_id")).append("\",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                    .append("\"soldPacks\":").append(sold).append(",")
                    .append("\"unitPrice\":").append(rs.getDouble("unit_price")).append(",")
                    .append("\"total\":").append(itemTotal)
                    .append("}");
                first = false;
            }

            result.append("],\"totalPacks\":").append(totalPacks)
                  .append(",\"totalRevenue\":").append(totalRevenue)
                  .append("}");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * IPOS-PU-RPT: Advertising campaigns report for a given period.
     * Matches Appendix 9 format.
     */
    @GetMapping("/campaigns")
    public String getCampaignsReport(@RequestParam String startDate, @RequestParam String endDate) {
        // Get campaigns active within the period
        String campaignSql = "SELECT p.name, p.start, p.end " +
                             "FROM promotion p " +
                             "WHERE p.start <= ? AND p.end >= ? " +
                             "ORDER BY p.name";

        String itemsSql = "SELECT pp.product_id, pc.item_id, pc.description, pp.discount, " +
                          "COALESCE((SELECT COUNT(*) FROM campaign_clicks cc WHERE cc.promotion_name = ? AND cc.product_id = pp.product_id AND cc.click_type = 'purchased'), 0) AS items_sold, " +
                          "COALESCE((SELECT SUM(oi.quantity * oi.unit_price * (1 - pp.discount/100)) FROM order_items oi " +
                          "JOIN orders o ON oi.order_id = o.order_id " +
                          "WHERE oi.product_id = pp.product_id AND o.order_date BETWEEN ? AND ?), 0) AS total_sales " +
                          "FROM promotion_product pp " +
                          "JOIN product_cache pc ON pp.product_id = pc.product_id " +
                          "WHERE pp.promotion_name = ?";

        StringBuilder result = new StringBuilder("{\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\",\"campaigns\":[");

        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement campStmt = conn.prepareStatement(campaignSql);
            campStmt.setString(1, endDate);
            campStmt.setString(2, startDate);
            ResultSet campRs = campStmt.executeQuery();

            boolean firstCamp = true;
            while (campRs.next()) {
                if (!firstCamp) result.append(",");
                firstCamp = false;

                String campName = campRs.getString("name");
                result.append("{")
                    .append("\"name\":\"").append(escapeJson(campName)).append("\",")
                    .append("\"startDate\":\"").append(campRs.getString("start")).append("\",")
                    .append("\"endDate\":\"").append(campRs.getString("end")).append("\",")
                    .append("\"items\":[");

                PreparedStatement itemStmt = conn.prepareStatement(itemsSql);
                itemStmt.setString(1, campName);
                itemStmt.setString(2, startDate);
                itemStmt.setString(3, endDate + " 23:59:59");
                itemStmt.setString(4, campName);
                ResultSet itemRs = itemStmt.executeQuery();

                boolean firstItem = true;
                double campTotal = 0;
                while (itemRs.next()) {
                    if (!firstItem) result.append(",");
                    firstItem = false;
                    double sales = itemRs.getDouble("total_sales");
                    campTotal += sales;

                    result.append("{")
                        .append("\"productId\":").append(itemRs.getInt("product_id")).append(",")
                        .append("\"itemId\":\"").append(itemRs.getString("item_id")).append("\",")
                        .append("\"description\":\"").append(escapeJson(itemRs.getString("description"))).append("\",")
                        .append("\"discount\":").append(itemRs.getFloat("discount")).append(",")
                        .append("\"itemsSold\":").append(itemRs.getInt("items_sold")).append(",")
                        .append("\"totalSales\":").append(sales)
                        .append("}");
                }
                itemStmt.close();

                result.append("],\"totalSales\":").append(campTotal).append("}");
            }
            campStmt.close();

            result.append("]}");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * IPOS-PU-RPT: Customer engagement report (conversion rates).
     * Matches Appendix 10 format.
     */
    @GetMapping("/engagement")
    public String getEngagementReport(@RequestParam String campaignName) {
        String sql = "SELECT " +
                     "COALESCE((SELECT COUNT(*) FROM campaign_clicks WHERE promotion_name = ? AND click_type = 'campaign'), 0) AS campaign_hits, " +
                     "pp.product_id, pc.item_id, pc.description, pp.discount, " +
                     "COALESCE((SELECT COUNT(*) FROM campaign_clicks cc WHERE cc.promotion_name = ? AND cc.product_id = pp.product_id AND cc.click_type = 'item_added'), 0) AS item_hits, " +
                     "COALESCE((SELECT COUNT(*) FROM campaign_clicks cc WHERE cc.promotion_name = ? AND cc.product_id = pp.product_id AND cc.click_type = 'purchased'), 0) AS purchases " +
                     "FROM promotion_product pp " +
                     "JOIN product_cache pc ON pp.product_id = pc.product_id " +
                     "WHERE pp.promotion_name = ?";

        StringBuilder result = new StringBuilder("{\"campaignName\":\"" + escapeJson(campaignName) + "\",");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, campaignName);
            stmt.setString(2, campaignName);
            stmt.setString(3, campaignName);
            stmt.setString(4, campaignName);
            ResultSet rs = stmt.executeQuery();

            int campaignHits = 0;
            StringBuilder items = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                campaignHits = rs.getInt("campaign_hits");
                if (!first) items.append(",");
                first = false;

                int itemHits = rs.getInt("item_hits");
                int purchases = rs.getInt("purchases");
                double conversionRate = itemHits > 0 ? (double) purchases / itemHits : 0;

                items.append("{")
                    .append("\"productId\":").append(rs.getInt("product_id")).append(",")
                    .append("\"itemId\":\"").append(rs.getString("item_id")).append("\",")
                    .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                    .append("\"discount\":").append(rs.getFloat("discount")).append(",")
                    .append("\"hitsCount\":").append(itemHits).append(",")
                    .append("\"purchases\":").append(purchases).append(",")
                    .append("\"conversionRate\":").append(String.format("%.4f", conversionRate))
                    .append("}");
            }
            items.append("]");

            result.append("\"campaignHits\":").append(campaignHits).append(",")
                  .append("\"items\":").append(items)
                  .append("}");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
