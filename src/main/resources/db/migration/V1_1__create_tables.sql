CREATE TABLE category (
                          id VARCHAR(255) PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          parent_category_id VARCHAR(255),
                          CONSTRAINT fk_parent_category FOREIGN KEY (parent_category_id) REFERENCES category(id) ON DELETE SET NULL
);

CREATE TABLE listings (
                          id VARCHAR(255) PRIMARY KEY,
                          seller_id UUID NOT NULL,
                          title VARCHAR(255),
                          description TEXT,
                          image_url VARCHAR(255),
                          initial_price FLOAT NOT NULL,
                          current_price FLOAT NOT NULL,
                          reserve_price FLOAT NOT NULL,
                          start_time TIMESTAMP,
                          end_time TIMESTAMP,
                          bid_count INTEGER NOT NULL DEFAULT 0,
                          active BOOLEAN NOT NULL DEFAULT true,
                          category_id VARCHAR(255),
                          CONSTRAINT fk_listing_category FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE auctions (
                          id VARCHAR(255) PRIMARY KEY,
                          seller_id VARCHAR(255),
                          catalogue_listing_id VARCHAR(255),
                          initial_price FLOAT,
                          reserve_price FLOAT,
                          current_highest_bid FLOAT,
                          start_time TIMESTAMP,
                          end_time TIMESTAMP,
                          stage VARCHAR(255)
);

CREATE TABLE bids (
                      id VARCHAR(255) PRIMARY KEY,
                      auction_id VARCHAR(255),
                      bidder_id VARCHAR(255),
                      amount FLOAT,
                      timestamp TIMESTAMP
);

CREATE TABLE wallet (
                        id BIGSERIAL PRIMARY KEY,
                        user_id VARCHAR(255),
                        available_balance BIGINT DEFAULT 0,
                        held_balance BIGINT DEFAULT 0
);

CREATE TABLE wallet_transaction (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id VARCHAR(255),
                                    transaction_type VARCHAR(255),
                                    amount BIGINT,
                                    timestamp TIMESTAMP
);