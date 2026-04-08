-- Insert sample products into CA database
INSERT INTO products (item_id, description, package_type, units_in_pack, price, vat_rate, stock_quantity, min_stock_level, is_active) VALUES
('100 00001', 'Paracetamol 500mg Tablets', 'box', 20, 2.50, 20.00, 1000, 50, TRUE),
('100 00002', 'Ibuprofen 200mg Tablets', 'box', 24, 3.20, 20.00, 800, 40, TRUE),
('100 00003', 'Aspirin 300mg Tablets', 'box', 30, 1.80, 20.00, 600, 30, TRUE),
('100 00004', 'Cough Syrup 100ml', 'bottle', 1, 4.50, 20.00, 200, 20, TRUE),
('100 00005', 'Vitamin C 500mg', 'box', 60, 5.99, 20.00, 500, 25, TRUE),
('200 00001', 'Amoxicillin 250mg Capsules', 'box', 21, 8.50, 0.00, 300, 15, TRUE),
('200 00002', 'Omeprazole 20mg Capsules', 'box', 28, 6.75, 20.00, 400, 20, TRUE),
('200 00003', 'Metformin 500mg Tablets', 'box', 56, 4.25, 20.00, 350, 18, TRUE),
('300 00001', 'Antiseptic Cream 30g', 'tube', 1, 3.99, 20.00, 250, 15, TRUE),
('300 00002', 'Bandages Pack ( assorted )', 'box', 1, 2.75, 20.00, 400, 25, TRUE),
('300 00003', 'Medical Gloves (100 pack)', 'box', 100, 8.99, 20.00, 600, 30, TRUE),
('400 00001', 'Blood Pressure Monitor', 'unit', 1, 45.00, 20.00, 50, 5, TRUE),
('400 00002', 'Digital Thermometer', 'unit', 1, 12.50, 20.00, 100, 10, TRUE),
('400 00003', 'First Aid Kit ( Premium )', 'kit', 1, 25.00, 20.00, 75, 8, TRUE);
