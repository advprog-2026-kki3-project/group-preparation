CREATE TABLE orders (
                        id               BIGSERIAL PRIMARY KEY,
                        auction_id       VARCHAR(255) NOT NULL UNIQUE,
                        buyer_username   VARCHAR(255) NOT NULL,
                        seller_username  VARCHAR(255) NOT NULL,
                        shipping_address VARCHAR(255) NOT NULL,
                        tracking_number  VARCHAR(255),
                        status           VARCHAR(255) NOT NULL,
                        created_at       TIMESTAMP    NOT NULL,
                        amount           BIGINT       NOT NULL
);

CREATE TABLE notifications (
                               id         BIGSERIAL PRIMARY KEY,
                               username   VARCHAR(255) NOT NULL,
                               type       VARCHAR(255) NOT NULL,
                               message    VARCHAR(255) NOT NULL,
                               order_id   BIGINT,
                               auction_id VARCHAR(255),
                               read       BOOLEAN      NOT NULL,
                               created_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_notifications_username ON notifications (username);
CREATE INDEX idx_orders_buyer ON orders (buyer_username);
CREATE INDEX idx_orders_seller ON orders (seller_username);