CREATE TABLE IF NOT EXISTS orders
(
    order_id        UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_name       VARCHAR(200) NOT NULL,
    cart_id         UUID,
    payment_id      UUID,
    delivery_id     UUID,
    state           VARCHAR(20)  NOT NULL,
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile         BOOLEAN,
    total_price     DOUBLE PRECISION,
    delivery_price  DOUBLE PRECISION,
    product_price   DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS order_products
(
    order_id   UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity   BIGINT,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT order_products_order_fk FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
);
