-- Create the "users" table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(255) NOT NULL,
                       stripe_customer_id VARCHAR(255) NULL,
                       payment_method_id VARCHAR(255) NULL;
);

-- Create indexes for faster lookups
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);

COMMENT ON COLUMN users.stripe_customer_id IS 'Stripe customer identifier';
COMMENT ON COLUMN users.payment_method_id IS 'Stripe payment method identifier';

-- Create partial indexes for faster payment-related lookups
CREATE INDEX idx_users_stripe_customer ON users (stripe_customer_id)
    WHERE stripe_customer_id IS NOT NULL;

CREATE INDEX idx_users_payment_method ON users (payment_method_id)
    WHERE payment_method_id IS NOT NULL;

-- Create unique constraints
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

-- =====================================================================
--  1) subscription_groups
-- =====================================================================

CREATE TABLE IF NOT EXISTS subscription_groups (
                                                   id              BIGSERIAL       PRIMARY KEY,
                                                   title           VARCHAR(100)    NOT NULL,
                                                   max_members     INT,
                                                   duration_days   INT             CHECK (duration_days BETWEEN 1 AND 30),
                                                   start_date      DATE,
                                                   end_date        DATE,
                                                   active          BOOLEAN         NOT NULL DEFAULT FALSE,
                                                   owner_id        BIGINT,
                                                   price_amount    INT,
                                                   price_currency  VARCHAR(3),
                                                   pricing_model   VARCHAR(50)
);

-- Indexes for subscription_groups
CREATE INDEX IF NOT EXISTS idx_sub_groups_active
    ON subscription_groups (active);

CREATE INDEX IF NOT EXISTS idx_sub_groups_owner_id
    ON subscription_groups (owner_id);

-- Foreign key to users table (assuming "users(id)" is the PK in your User entity)
ALTER TABLE subscription_groups
    ADD CONSTRAINT fk_sub_groups_owner
        FOREIGN KEY (owner_id)
            REFERENCES users (id)
            ON DELETE SET NULL;  -- or CASCADE / RESTRICT as appropriate for your use case


-- =====================================================================
--  2) memberships
-- =====================================================================

CREATE TABLE IF NOT EXISTS memberships (
                                           id                       BIGSERIAL       PRIMARY KEY,
                                           paid                     BOOLEAN         NOT NULL DEFAULT FALSE,
                                           valid                    BOOLEAN         NOT NULL DEFAULT TRUE,
                                           failed_date              DATE,
                                           user_id                  BIGINT,
                                           subscription_group_id    BIGINT,
                                           payment_status           VARCHAR(50),    -- Alternatively, define a PostgreSQL ENUM type
                                           stripe_payment_intent_id VARCHAR(255)
);

-- Indexes for memberships
CREATE INDEX IF NOT EXISTS idx_memberships_user_id
    ON memberships (user_id);

CREATE INDEX IF NOT EXISTS idx_memberships_subscription_group_id
    ON memberships (subscription_group_id);

-- Foreign keys
ALTER TABLE memberships
    ADD CONSTRAINT fk_memberships_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL;  -- or CASCADE / RESTRICT as you see fit

ALTER TABLE memberships
    ADD CONSTRAINT fk_memberships_subscription_group
        FOREIGN KEY (subscription_group_id)
            REFERENCES subscription_groups (id)
            ON DELETE CASCADE;   -- This ensures memberships are removed if the parent subscription group is deleted
