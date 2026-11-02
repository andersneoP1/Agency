-- V1__init.sql: Baseline schema for Native Travel application

-- 1. Roles & Users
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    avatar_url VARCHAR(500),
    referral_code VARCHAR(50) NOT NULL UNIQUE,
    telegram_chat_id BIGINT UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE password_reset_codes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_password_reset_user_code ON password_reset_codes(user_id, code);

-- 2. Hotels, Tours, Flights
CREATE TABLE hotels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    stars INT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    description TEXT,
    amenities TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_hotels_country_city ON hotels(country, city);

CREATE TABLE tours (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    hotel_id BIGINT REFERENCES hotels(id) ON DELETE SET NULL,
    price NUMERIC(12, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration_days INT NOT NULL,
    included_services TEXT,
    image_url VARCHAR(500),
    hot_offer BOOLEAN NOT NULL DEFAULT FALSE,
    available_slots INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tours_country_dates_price ON tours(country, start_date, price);
CREATE INDEX idx_tours_hot_offer ON tours(hot_offer);

CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(50) NOT NULL,
    airline VARCHAR(100) NOT NULL,
    departure_city VARCHAR(100) NOT NULL,
    arrival_city VARCHAR(100) NOT NULL,
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time TIMESTAMP WITH TIME ZONE NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    available_seats INT NOT NULL DEFAULT 50,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_flights_cities_time ON flights(departure_city, arrival_city, departure_time);

-- 3. Promo Codes
CREATE TABLE promo_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percent INT NOT NULL CHECK (discount_percent BETWEEN 1 AND 100),
    min_order_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    max_uses INT NOT NULL DEFAULT 100,
    current_uses INT NOT NULL DEFAULT 0,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_promo_codes_code ON promo_codes(code);

-- 4. Cart & Bookings
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_id BIGINT REFERENCES tours(id) ON DELETE CASCADE,
    flight_id BIGINT REFERENCES flights(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_cart_item_target CHECK (tour_id IS NOT NULL OR flight_id IS NOT NULL)
);
CREATE INDEX idx_cart_items_user ON cart_items(user_id);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_id BIGINT REFERENCES tours(id) ON DELETE SET NULL,
    flight_id BIGINT REFERENCES flights(id) ON DELETE SET NULL,
    promo_code_id BIGINT REFERENCES promo_codes(id) ON DELETE SET NULL,
    original_price NUMERIC(12, 2) NOT NULL,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_price NUMERIC(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);

CREATE TABLE qr_codes (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    qr_data TEXT NOT NULL,
    image_path VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 5. Reviews (constraint: one review per user per tour)
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NOT NULL,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_tour_review UNIQUE (user_id, tour_id)
);
CREATE INDEX idx_reviews_tour_approved ON reviews(tour_id, approved);

-- 6. Referrals & Surveys
CREATE TABLE referrals (
    id BIGSERIAL PRIMARY KEY,
    referrer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    referred_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    reward_granted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_referrals_referrer ON referrals(referrer_id);

CREATE TABLE surveys (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    preferred_budget VARCHAR(50),
    travel_type VARCHAR(50),
    preferred_season VARCHAR(50),
    climate_preference VARCHAR(50),
    recommended_tour_id BIGINT REFERENCES tours(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 7. Favorites, Viewed, Support & Subscriptions
CREATE TABLE favorite_tours (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_favorite UNIQUE (user_id, tour_id)
);

CREATE TABLE viewed_tours (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    viewed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_viewed_tours_user ON viewed_tours(user_id, viewed_at DESC);

CREATE TABLE support_messages (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),
    sender_type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    escalated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_support_messages_session ON support_messages(session_id);

CREATE TABLE hot_offer_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255),
    telegram_chat_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_hot_offer_subs_email ON hot_offer_subscriptions(email);
