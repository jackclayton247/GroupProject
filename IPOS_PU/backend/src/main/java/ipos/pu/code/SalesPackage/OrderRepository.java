package ipos.pu.code.SalesPackage;

import ipos.pu.code.config.DatabaseConfig;
import java.sql.*;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    public int createOrder(String userEmail, String deliveryAddress, double totalPrice, double discountApplied) {
        String sql = "INSERT INTO orders (user_email, delivery_address, total_price, discount_applied) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, userEmail);
            pst.setString(2, deliveryAddress);
            pst.setDouble(3, totalPrice);
            pst.setDouble(4, discountApplied);
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void createOrderItems(int orderId, List<OrderItem> items, List<Double> prices) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                pst.setInt(1, orderId);
                pst.setInt(2, items.get(i).getProductId());
                pst.setInt(3, items.get(i).getQuantity());
                pst.setDouble(4, prices.get(i));
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPayment(int orderId, String cardType, String cardFirstFour, String cardLastFour, String cardExpiry, double amount) {
        String sql = "INSERT INTO payments (order_id, card_type, card_first_four, card_last_four, card_expiry, amount) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, orderId);
            pst.setString(2, cardType);
            pst.setString(3, cardFirstFour);
            pst.setString(4, cardLastFour);
            pst.setString(5, cardExpiry);
            pst.setDouble(6, amount);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deductStock(int productId, int quantity) {
        // When CA is offline: deduct from stock AND add to pending_stock_change (to sync later)
        String sql = "UPDATE product_cache SET stock_quantity = stock_quantity - ?, pending_stock_change = pending_stock_change + ? WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, quantity);
            pst.setInt(2, quantity);
            pst.setInt(3, productId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deductLocalStockOnly(int productId, int quantity) {
        // When CA is online: deduct from local cache only (no pending_stock_change since CA already has the update)
        String sql = "UPDATE product_cache SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, quantity);
            pst.setInt(2, productId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getItemIdByProductId(int productId) {
        String sql = "SELECT item_id FROM product_cache WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("item_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void incrementOrderNumber(String email) {
        String sql = "UPDATE `user` SET orderNumber = orderNumber + 1 WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getOrderNumber(String email) {
        String sql = "SELECT orderNumber FROM `user` WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("orderNumber");
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public double getProductPrice(int productId) {
        String sql = "SELECT price FROM product_cache WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("price");
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getOrderStatus(int orderId) {
        String sql = "SELECT status FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, orderId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("status");
            return "order not found";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public String getUndeliveredOrders() {
        String sql = "SELECT o.order_id, o.user_email, o.delivery_address, o.order_date, o.total_price, o.status, " +
                     "oi.product_id, oi.quantity, oi.unit_price, COALESCE(pc.description, CONCAT('Product #', oi.product_id)) as product_name " +
                     "FROM orders o " +
                     "JOIN order_items oi ON o.order_id = oi.order_id " +
                     "LEFT JOIN product_cache pc ON oi.product_id = pc.product_id " +
                     "WHERE o.status = 'received' " +
                     "ORDER BY o.order_id";
        
        java.util.LinkedHashMap<Integer, StringBuilder> orderMap = new java.util.LinkedHashMap<>();
        java.util.Map<Integer, StringBuilder> itemsMap = new java.util.HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                
                if (!orderMap.containsKey(orderId)) {
                    StringBuilder orderJson = new StringBuilder();
                    orderJson.append("{")
                        .append("\"orderId\":").append(orderId).append(",")
                        .append("\"memberName\":\"").append(rs.getString("user_email")).append("\",")
                        .append("\"deliveryAddress\":\"").append(rs.getString("delivery_address")).append("\",")
                        .append("\"orderDate\":\"").append(rs.getString("order_date")).append("\",")
                        .append("\"totalValue\":").append(rs.getDouble("total_price")).append(",")
                        .append("\"status\":\"").append(rs.getString("status")).append("\"");
                    orderMap.put(orderId, orderJson);
                    itemsMap.put(orderId, new StringBuilder());
                }
                
                StringBuilder items = itemsMap.get(orderId);
                if (items.length() > 0) items.append(",");
                items.append("{")
                    .append("\"productName\":\"").append(rs.getString("product_name")).append("\",")
                    .append("\"productId\":").append(rs.getInt("product_id")).append(",")
                    .append("\"quantity\":").append(rs.getInt("quantity")).append(",")
                    .append("\"unitPrice\":").append(rs.getDouble("unit_price"))
                    .append("}");
            }
            
            StringBuilder result = new StringBuilder("[");
            boolean first = true;
            for (Integer orderId : orderMap.keySet()) {
                if (!first) result.append(",");
                result.append(orderMap.get(orderId));
                result.append(",\"items\":[").append(itemsMap.get(orderId)).append("]}");
                first = false;
            }
            result.append("]");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public String updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, orderId);
            pst.executeUpdate();
            return "status updated to: " + status;
        } catch (Exception e) {
            e.printStackTrace();
            return "error updating status";
        }
    }

    public int getOrderCountByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public String getOrdersByEmail(String email) {
        String sql = "SELECT o.order_id, o.user_email, o.delivery_address, o.order_date, o.total_price, o.status, o.discount_applied, " +
                     "oi.product_id, oi.quantity, oi.unit_price, COALESCE(pc.description, CONCAT('Product #', oi.product_id)) as product_name " +
                     "FROM orders o " +
                     "JOIN order_items oi ON o.order_id = oi.order_id " +
                     "LEFT JOIN product_cache pc ON oi.product_id = pc.product_id " +
                     "WHERE o.user_email = ? " +
                     "ORDER BY o.order_date DESC";
        
        java.util.LinkedHashMap<Integer, StringBuilder> orderMap = new java.util.LinkedHashMap<>();
        java.util.Map<Integer, StringBuilder> itemsMap = new java.util.HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                
                if (!orderMap.containsKey(orderId)) {
                    StringBuilder orderJson = new StringBuilder();
                    orderJson.append("{")
                        .append("\"orderId\":\"PU-").append(orderId).append("\",")
                        .append("\"date\":\"").append(rs.getString("order_date")).append("\",")
                        .append("\"status\":\"").append(rs.getString("status")).append("\",")
                        .append("\"total\":").append(rs.getDouble("total_price")).append(",")
                        .append("\"discount\":").append(rs.getDouble("discount_applied"))
                        .append("}");
                    orderMap.put(orderId, orderJson);
                    itemsMap.put(orderId, new StringBuilder());
                }
                
                StringBuilder items = itemsMap.get(orderId);
                if (items.length() > 0) items.append(",");
                items.append("{")
                    .append("\"description\":\"").append(rs.getString("product_name")).append("\",")
                    .append("\"qty\":").append(rs.getInt("quantity")).append(",")
                    .append("\"unitPrice\":").append(rs.getDouble("unit_price"))
                    .append("}");
            }
            
            StringBuilder result = new StringBuilder("[");
            boolean first = true;
            for (Integer orderId : orderMap.keySet()) {
                if (!first) result.append(",");
                // Parse the order JSON to insert items
                String orderStr = orderMap.get(orderId).toString();
                result.append(orderStr, 0, orderStr.length() - 1); // Remove closing }
                result.append(",\"items\":[").append(itemsMap.get(orderId)).append("]}");
                first = false;
            }
            result.append("]");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }
}