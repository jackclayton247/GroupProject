CREATE TABLE IF NOT EXISTS product_cache (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    package_type VARCHAR(255),
    units_in_pack INT NOT NULL DEFAULT 1 CHECK (units_in_pack > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    vat_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 CHECK (vat_rate >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    min_stock_level INT NOT NULL DEFAULT 0 CHECK (min_stock_level >= 0),
    is_active TINYINT(1) NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
    pending_stock_change INT NOT NULL DEFAULT 0 CHECK (pending_stock_change >= 0)
);

CREATE TABLE IF NOT EXISTS `user` (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    orderNumber INT NOT NULL DEFAULT 0,
    merchant BOOLEAN NOT NULL DEFAULT FALSE
);

-- Add merchant column to existing tables that were created without it
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS merchant BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS promotion (
    name VARCHAR(255) PRIMARY KEY,
    start DATETIME,
    end DATETIME
);

CREATE TABLE IF NOT EXISTS promotion_product (
    product_id INT PRIMARY KEY,
    promotion_name VARCHAR(255),
    discount FLOAT,
    FOREIGN KEY (promotion_name) REFERENCES promotion(name),
    FOREIGN KEY (product_id) REFERENCES product_cache(product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(255),
    order_date DATETIME DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'received',
    total_price DECIMAL(10,2),
    delivery_address TEXT,
    discount_applied DECIMAL(10,2) DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT,
    unit_price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES product_cache(product_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    card_type VARCHAR(50),
    card_first_four CHAR(4),
    card_last_four CHAR(4),
    card_expiry VARCHAR(7),
    amount DECIMAL(10,2),
    payment_time DATETIME DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'completed',
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE IF NOT EXISTS ca_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    merchant_id VARCHAR(255),
    order_id VARCHAR(255),
    payee VARCHAR(255),
    address TEXT,
    card_first_four CHAR(4),
    card_last_four CHAR(4),
    amount DECIMAL(9,2),
    status VARCHAR(50),
    payment_time DATETIME DEFAULT NOW()
);

INSERT IGNORE INTO product_cache (item_id, description, package_type, units_in_pack, price, stock_quantity, is_active) VALUES
('100 00001', 'Paracetamol', 'box', 20, 0.10, 10345, 1),
('100 00002', 'Aspirin', 'box', 20, 0.50, 12453, 1),
('100 00003', 'Analgin', 'box', 10, 1.20, 4235, 1),
('100 00004', 'Celebrex, caps 100 mg', 'box', 10, 10.00, 3420, 1),
('100 00005', 'Celebrex, caps 200 mg', 'box', 10, 18.50, 1450, 1),
('100 00006', 'Retin-A Tretin, 30 g', 'box', 20, 25.00, 2013, 1),
('100 00007', 'Lipitor TB, 20 mg', 'box', 30, 15.50, 1562, 1),
('100 00008', 'Claritin CR, 60g', 'box', 20, 19.50, 2540, 1),
('200 00004', 'Iodine tincture', 'bottle', 100, 0.30, 2213, 1),
('200 00005', 'Rhynol', 'bottle', 200, 2.50, 1908, 1),
('300 00001', 'Ospen', 'box', 20, 10.50, 809, 1),
('300 00002', 'Amopen', 'box', 30, 15.00, 1340, 1),
('400 00001', 'Vitamin C', 'box', 30, 1.20, 3258, 1),
('400 00002', 'Vitamin B12', 'box', 30, 1.30, 2673, 1);