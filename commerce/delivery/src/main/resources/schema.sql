CREATE TABLE IF NOT EXISTS address
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR(255) NOT NULL,
    city    VARCHAR(255) NOT NULL,
    street  VARCHAR(255) NOT NULL,
    house   VARCHAR(100) NOT NULL,
    flat    VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS delivery
(
    delivery_id     UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    from_address_id UUID        NOT NULL REFERENCES address (id),
    to_address_id   UUID        NOT NULL REFERENCES address (id),
    order_id        UUID        NOT NULL,
    delivery_state  varchar(20) NOT NULL DEFAULT 'CREATED'

);

CREATE INDEX IF NOT EXISTS idx_delivery_order_id ON delivery (order_id);