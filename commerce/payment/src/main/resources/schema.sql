CREATE TABLE IF NOT EXISTS payment
(
    payment_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id      UUID        NOT NULL,
    state         varchar(20) NOT NULL,
    total_cost    DOUBLE PRECISION,
    delivery_cost DOUBLE PRECISION,
    fee_cost      DOUBLE PRECISION
);