CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    package_type VARCHAR(255),
    units_in_pack INT NOT NULL DEFAULT 1 CHECK (units_in_pack > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    vat_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 CHECK (vat_rate >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    min_stock_level INT NOT NULL DEFAULT 0 CHECK (min_stock_level >= 0),
    is_active TINYINT(1) NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

CREATE TABLE IF NOT EXISTS `user` (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);