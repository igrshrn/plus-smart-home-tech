CREATE TABLE IF NOT EXISTS warehouse_product
(
    product_id UUID PRIMARY KEY,
    fragile    BOOLEAN,
    width      DOUBLE PRECISION,
    height     DOUBLE PRECISION,
    depth      DOUBLE PRECISION,
    weight     DOUBLE PRECISION,
    quantity   BIGINT
);