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
    pending_stock_change INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `user` (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    orderNumber INT NOT NULL DEFAULT 0,
    merchant BOOLEAN NOT NULL DEFAULT FALSE,
    force_password_change BOOLEAN NOT NULL DEFAULT FALSE
);

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
    discount_applied DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (user_email) REFERENCES `user`(email) ON DELETE SET NULL
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

CREATE TABLE IF NOT EXISTS merchant_applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    company_reg_number VARCHAR(100),
    director_name VARCHAR(255),
    business_type VARCHAR(100),
    address TEXT,
    phone VARCHAR(50),
    status VARCHAR(50) DEFAULT 'pending',
    applied_at DATETIME DEFAULT NOW(),
    decided_at DATETIME,
    FOREIGN KEY (email) REFERENCES `user`(email) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS campaign_clicks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    promotion_name VARCHAR(255),
    product_id INT,
    click_type VARCHAR(20) NOT NULL,
    clicked_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (promotion_name) REFERENCES promotion(name),
    FOREIGN KEY (product_id) REFERENCES product_cache(product_id)
);
