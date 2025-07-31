-- MEV Analytics Platform Database Schema
-- Professional-grade schema for SaaS platform

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===== CORE MEV DATA TABLES =====

-- MEV Transactions (Core data)
CREATE TABLE mev_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_hash VARCHAR(66) UNIQUE NOT NULL,
    block_number BIGINT NOT NULL,
    block_timestamp TIMESTAMP NOT NULL,
    from_address VARCHAR(42) NOT NULL,
    to_address VARCHAR(42) NOT NULL,
    mev_type VARCHAR(20) NOT NULL CHECK (mev_type IN ('ARBITRAGE', 'SANDWICH', 'LIQUIDATION', 'FRONTRUN', 'BACKRUN')),
    extracted_value_usd DECIMAL(18,8) NOT NULL DEFAULT 0,
    gas_paid_usd DECIMAL(18,8) NOT NULL DEFAULT 0,
    net_profit_usd DECIMAL(18,8) GENERATED ALWAYS AS (extracted_value_usd - gas_paid_usd) STORED,
    gas_used BIGINT,
    gas_price BIGINT,
    dex_name VARCHAR(50),
    token_pair VARCHAR(50),
    victim_address VARCHAR(42),
    confidence_score DECIMAL(3,2) DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- MEV Searchers (Bot tracking)
CREATE TABLE mev_searchers (
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(42) UNIQUE NOT NULL,
    first_seen TIMESTAMP DEFAULT NOW(),
    last_seen TIMESTAMP DEFAULT NOW(),
    total_transactions BIGINT DEFAULT 0,
    total_extracted_usd DECIMAL(18,8) DEFAULT 0,
    total_gas_paid_usd DECIMAL(18,8) DEFAULT 0,
    win_rate DECIMAL(5,2) DEFAULT 0,
    avg_profit_per_tx DECIMAL(18,8) DEFAULT 0,
    reputation_score INTEGER DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    notes TEXT
);

-- ===== SAAS BUSINESS TABLES =====

-- API Customers
CREATE TABLE api_customers (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    api_key VARCHAR(64) UNIQUE NOT NULL,
    subscription_tier VARCHAR(20) NOT NULL CHECK (subscription_tier IN ('STARTER', 'PROFESSIONAL', 'ENTERPRISE')),
    monthly_api_calls_used INTEGER DEFAULT 0,
    monthly_api_calls_limit INTEGER NOT NULL,
    subscription_start TIMESTAMP DEFAULT NOW(),
    subscription_end TIMESTAMP,
    monthly_revenue DECIMAL(10,2) DEFAULT 0,
    last_api_call TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    stripe_customer_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- API Usage Logs (for billing and analytics)
CREATE TABLE api_usage_logs (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES api_customers(id),
    api_key VARCHAR(64) NOT NULL,
    endpoint VARCHAR(200) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_timestamp TIMESTAMP DEFAULT NOW(),
    response_time_ms INTEGER,
    status_code INTEGER,
    request_size_bytes INTEGER,
    response_size_bytes INTEGER,
    ip_address INET,
    user_agent TEXT,
    error_message TEXT
);

-- ===== INDEXES FOR PERFORMANCE =====

-- MEV Transactions indexes
CREATE INDEX idx_mev_transactions_timestamp ON mev_transactions(block_timestamp);
CREATE INDEX idx_mev_transactions_from_address ON mev_transactions(from_address);
CREATE INDEX idx_mev_transactions_mev_type ON mev_transactions(mev_type);
CREATE INDEX idx_mev_transactions_dex_name ON mev_transactions(dex_name);
CREATE INDEX idx_mev_transactions_block_number ON mev_transactions(block_number);
CREATE INDEX idx_mev_transactions_net_profit ON mev_transactions(net_profit_usd DESC);

-- API usage indexes
CREATE INDEX idx_api_usage_logs_customer_id ON api_usage_logs(customer_id);
CREATE INDEX idx_api_usage_logs_timestamp ON api_usage_logs(request_timestamp);
CREATE INDEX idx_api_usage_logs_api_key ON api_usage_logs(api_key);

-- Customer indexes
CREATE INDEX idx_api_customers_api_key ON api_customers(api_key);
CREATE INDEX idx_api_customers_email ON api_customers(email);
CREATE INDEX idx_api_customers_tier ON api_customers(subscription_tier);

-- ===== SAMPLE DATA FOR DEVELOPMENT =====

-- Insert sample API customers
INSERT INTO api_customers (email, api_key, subscription_tier, monthly_api_calls_limit, monthly_revenue, stripe_customer_id) VALUES 
('demo@mevscope.com', 'mev_demo_key_123456789', 'PROFESSIONAL', 100000, 199.00, 'cus_demo123'),
('test@mevscope.com', 'mev_test_key_987654321', 'STARTER', 10000, 49.00, 'cus_test456'),
('enterprise@bigfund.com', 'mev_enterprise_key_555', 'ENTERPRISE', 1000000, 999.00, 'cus_enterprise789');

-- Insert sample MEV searchers
INSERT INTO mev_searchers (address, total_transactions, total_extracted_usd, total_gas_paid_usd, win_rate, avg_profit_per_tx) VALUES
('0x1a2b3c4d5e6f7890abcdef1234567890abcdef12', 2847, 143247.89, 8547.23, 94.2, 50.31),
('0x5e6f7g8h9i0j1234567890abcdef1234567890ab', 2156, 128934.56, 9245.67, 91.7, 59.82),
('0x9i0j1k2l3m4n567890abcdef1234567890abcdef', 1934, 112678.23, 7834.12, 89.3, 58.24),
('0x3m4n5o6p7q8r90abcdef1234567890abcdef1234', 1678, 98234.77, 6789.45, 87.8, 58.54),
('0x7q8r9s0t1u2v34567890abcdef1234567890abcd', 1456, 87456.12, 5932.87, 85.4, 60.08);

-- Insert sample MEV transactions
INSERT INTO mev_transactions (transaction_hash, block_number, block_timestamp, from_address, to_address, mev_type, extracted_value_usd, gas_paid_usd, gas_used, gas_price, dex_name, token_pair) VALUES
('0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef12', 19234567, NOW() - INTERVAL '1 day', '0x1a2b3c4d5e6f7890abcdef1234567890abcdef12', '0xDEXRouter1', 'ARBITRAGE', 1247.89, 23.45, 180000, 30000000000, 'Uniswap', 'WETH/USDC'),
('0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab', 19234568, NOW() - INTERVAL '1 day', '0x5e6f7g8h9i0j1234567890abcdef1234567890ab', '0xDEXRouter2', 'SANDWICH', 892.33, 67.23, 220000, 35000000000, 'SushiSwap', 'WBTC/USDT'),
('0x567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef123456', 19234569, NOW() - INTERVAL '2 days', '0x9i0j1k2l3m4n567890abcdef1234567890abcdef', '0xLendingProtocol', 'LIQUIDATION', 2156.78, 45.67, 150000, 28000000000, 'Compound', 'ETH/DAI');

-- Grant permissions for application user
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO mevuser;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO mevuser;

COMMIT;
