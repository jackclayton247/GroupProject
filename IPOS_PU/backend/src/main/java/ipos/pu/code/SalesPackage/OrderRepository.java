package ipos.pu.code.SalesPackage;

import ipos.pu.code.config.DatabaseConfig;
import java.sql.*;
import java.util.List;

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
}