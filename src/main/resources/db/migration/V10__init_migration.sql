ALTER TABLE product
    MODIFY COLUMN description VARCHAR(1000);

ALTER TABLE order_detail
    DROP COLUMN created_at;

ALTER TABLE order_detail
    DROP FOREIGN KEY order_detail_ibfk_1,
    DROP COLUMN address_id;

ALTER TABLE payment_detail
    ADD COLUMN address_id BIGINT NOT NULL;

ALTER TABLE payment_detail
    ADD COLUMN currency VARCHAR(20) NOT NULL;

DROP TABLE IF EXISTS address;

DROP TABLE IF EXISTS country;

ALTER TABLE order_detail
    DROP FOREIGN KEY order_detail_ibfk_2,
    DROP COLUMN payment_detail_id;

DROP TABLE IF EXISTS payment_detail;

CREATE TABLE IF NOT EXISTS payment_detail
(
    payment_detail_id  BIGINT         NOT NULL UNIQUE AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    email              VARCHAR(255)   NOT NULL,
    phone VARCHAR(20) NOT NULL,
    payment_id         VARCHAR(255)   NOT NULL UNIQUE,
    currency           VARCHAR(20)    NOT NULL,
    amount             DECIMAL(20, 2) NOT NULL,
    payment_provider   VARCHAR(30)    NOT NULL,
    payment_status     VARCHAR(10)    NOT NULL,
    created_at         DATETIME       NOT NULL,
    PRIMARY KEY (payment_detail_id)
);

ALTER TABLE order_detail
    ADD COLUMN payment_detail_id BIGINT;

CREATE TABLE IF NOT EXISTS address
(
    address_id        BIGINT       NOT NULL UNIQUE,
    address       VARCHAR(255) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    postcode      VARCHAR(10),
    country       VARCHAR(100) NOT NULL,
    delivery_info VARCHAR(1000),
    PRIMARY KEY (address_id),
    FOREIGN KEY (address_id) REFERENCES payment_detail (payment_detail_id)
);

ALTER TABLE product_sku
    ADD CONSTRAINT chk_inventory_is_always_greater_than_zero CHECK ( inventory >= 0 );

CREATE TABLE IF NOT EXISTS order_reservation
(
    reservation_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    cookie VARCHAR(39) NOT NULL,
    sku            VARCHAR(36) NOT NULL,
    qty            INTEGER     NOT NULL,
    status         VARCHAR(10) NOT NULL,
    expire_at      DATETIME    NOT NULL,
    PRIMARY KEY (reservation_id)
);