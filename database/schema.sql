-- =============================================================================
-- Hotel Management System — MySQL 8.x Full Schema
-- Engine: InnoDB | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- Conventions: soft-deletes via deleted_at, optimistic locking via version
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------------------------------
-- hotels
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS hotels;
CREATE TABLE hotels (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    address         VARCHAR(500)    NOT NULL,
    city            VARCHAR(100)    NOT NULL,
    state           VARCHAR(100),
    country         VARCHAR(100)    NOT NULL,
    postal_code     VARCHAR(20),
    phone           VARCHAR(30)     NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    star_rating     TINYINT         NOT NULL DEFAULT 3,
    description     TEXT,
    check_in_time   TIME            NOT NULL DEFAULT '14:00:00',
    check_out_time  TIME            NOT NULL DEFAULT '11:00:00',
    timezone        VARCHAR(60)     NOT NULL DEFAULT 'UTC',
    image_url       VARCHAR(500),
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL DEFAULT NULL,
    PRIMARY KEY (id),
    INDEX idx_hotels_active (is_active, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- roles
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)     NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- permissions
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS permissions;
CREATE TABLE permissions (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(255),
    resource    VARCHAR(100)    NOT NULL,
    action      VARCHAR(50)     NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_permissions_name (name),
    INDEX idx_permissions_resource (resource)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- role_permissions (join table)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS role_permissions;
CREATE TABLE role_permissions (
    role_id         BIGINT  NOT NULL,
    permission_id   BIGINT  NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- staff
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS staff;
CREATE TABLE staff (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id        BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    phone           VARCHAR(30),
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    last_login      DATETIME        NULL,
    profile_image   VARCHAR(500),
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_staff_email (email),
    INDEX idx_staff_hotel (hotel_id),
    INDEX idx_staff_role (role_id),
    INDEX idx_staff_active (is_active, deleted_at),
    CONSTRAINT fk_staff_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_staff_role  FOREIGN KEY (role_id)  REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- room_types
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS room_types;
CREATE TABLE room_types (
    id              BIGINT              NOT NULL AUTO_INCREMENT,
    hotel_id        BIGINT              NOT NULL,
    name            VARCHAR(100)        NOT NULL,
    description     TEXT,
    max_occupancy   INT                 NOT NULL DEFAULT 2,
    base_price      DECIMAL(10,2)       NOT NULL,
    amenities       JSON,
    image_urls      JSON,
    created_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME            NULL DEFAULT NULL,
    PRIMARY KEY (id),
    INDEX idx_room_types_hotel (hotel_id, deleted_at),
    CONSTRAINT fk_room_types_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- rooms
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS rooms;
CREATE TABLE rooms (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id        BIGINT          NOT NULL,
    room_type_id    BIGINT          NOT NULL,
    room_number     VARCHAR(20)     NOT NULL,
    floor           INT             NOT NULL DEFAULT 1,
    status          ENUM('AVAILABLE','OCCUPIED','DIRTY','CLEAN','INSPECTED','OUT_OF_ORDER','MAINTENANCE')
                                    NOT NULL DEFAULT 'AVAILABLE',
    notes           TEXT,
    version         BIGINT          NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version counter',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_room_number_hotel (hotel_id, room_number),
    INDEX idx_rooms_status (status),
    INDEX idx_rooms_hotel_type (hotel_id, room_type_id),
    INDEX idx_rooms_floor (hotel_id, floor),
    CONSTRAINT fk_rooms_hotel     FOREIGN KEY (hotel_id)     REFERENCES hotels(id),
    CONSTRAINT fk_rooms_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- guests
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS guests;
CREATE TABLE guests (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    phone           VARCHAR(30),
    date_of_birth   DATE,
    nationality     VARCHAR(100),
    id_type         ENUM('PASSPORT','NATIONAL_ID','DRIVERS_LICENSE'),
    id_number       VARCHAR(100),
    loyalty_points  INT             NOT NULL DEFAULT 0,
    loyalty_tier    ENUM('BRONZE','SILVER','GOLD','PLATINUM') NOT NULL DEFAULT 'BRONZE',
    address_line1   VARCHAR(255),
    address_line2   VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100),
    postal_code     VARCHAR(20),
    is_verified     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_guests_email (email),
    INDEX idx_guests_name (last_name, first_name),
    INDEX idx_guests_loyalty (loyalty_tier),
    INDEX idx_guests_phone (phone),
    FULLTEXT INDEX ft_guests_search (first_name, last_name, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- reservations
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS reservations;
CREATE TABLE reservations (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id                BIGINT          NOT NULL,
    guest_id                BIGINT          NOT NULL,
    confirmation_number     VARCHAR(20)     NOT NULL,
    status                  ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW')
                                            NOT NULL DEFAULT 'PENDING',
    check_in_date           DATE            NOT NULL,
    check_out_date          DATE            NOT NULL,
    adults                  INT             NOT NULL DEFAULT 1,
    children                INT             NOT NULL DEFAULT 0,
    special_requests        TEXT,
    cancellation_policy     ENUM('FLEXIBLE','MODERATE','STRICT') NOT NULL DEFAULT 'MODERATE',
    cancelled_at            DATETIME        NULL,
    cancellation_reason     TEXT,
    total_amount            DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    paid_amount             DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    source                  ENUM('DIRECT','OTA','PHONE','WALK_IN') NOT NULL DEFAULT 'DIRECT',
    version                 BIGINT          NOT NULL DEFAULT 0 COMMENT 'Optimistic locking — prevents double booking',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at              DATETIME        NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_confirmation_number (confirmation_number),
    INDEX idx_reservations_dates (check_in_date, check_out_date),
    INDEX idx_reservations_status (status),
    INDEX idx_reservations_guest (guest_id),
    INDEX idx_reservations_hotel (hotel_id),
    INDEX idx_reservations_hotel_dates (hotel_id, check_in_date, check_out_date),
    CONSTRAINT fk_reservations_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_reservations_guest FOREIGN KEY (guest_id) REFERENCES guests(id),
    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- reservation_rooms
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS reservation_rooms;
CREATE TABLE reservation_rooms (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    reservation_id      BIGINT          NOT NULL,
    room_id             BIGINT          NOT NULL,
    room_type_id        BIGINT          NOT NULL,
    rate_per_night      DECIMAL(10,2)   NOT NULL,
    check_in_date       DATE            NOT NULL,
    check_out_date      DATE            NOT NULL,
    actual_check_in     DATETIME        NULL,
    actual_check_out    DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    -- Composite index for availability lookups
    INDEX idx_room_availability (room_id, check_in_date, check_out_date),
    INDEX idx_rr_reservation (reservation_id),
    INDEX idx_rr_room_dates (room_id, check_in_date, check_out_date),
    CONSTRAINT fk_rr_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    CONSTRAINT fk_rr_room        FOREIGN KEY (room_id)        REFERENCES rooms(id),
    CONSTRAINT fk_rr_room_type   FOREIGN KEY (room_type_id)   REFERENCES room_types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- payments
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS payments;
CREATE TABLE payments (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    reservation_id      BIGINT          NOT NULL,
    amount              DECIMAL(10,2)   NOT NULL,
    payment_method      ENUM('CASH','CREDIT_CARD','DEBIT_CARD','BANK_TRANSFER','ONLINE') NOT NULL,
    payment_status      ENUM('PENDING','COMPLETED','FAILED','REFUNDED','PARTIAL')        NOT NULL DEFAULT 'PENDING',
    transaction_id      VARCHAR(255),
    idempotency_key     VARCHAR(255)    NOT NULL,
    paid_at             DATETIME        NULL,
    notes               TEXT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_idempotency_key (idempotency_key),
    INDEX idx_payments_reservation (reservation_id),
    INDEX idx_payments_status (payment_status),
    INDEX idx_payments_paid_at (paid_at),
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- invoices
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS invoices;
CREATE TABLE invoices (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    reservation_id  BIGINT          NOT NULL,
    guest_id        BIGINT          NOT NULL,
    invoice_number  VARCHAR(50)     NOT NULL,
    status          ENUM('DRAFT','ISSUED','PAID','CANCELLED') NOT NULL DEFAULT 'DRAFT',
    subtotal        DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    tax_rate        DECIMAL(5,2)    NOT NULL DEFAULT 10.00,
    tax_amount      DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    notes           TEXT,
    issued_at       DATETIME        NULL,
    due_at          DATETIME        NULL,
    paid_at         DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_invoice_number (invoice_number),
    INDEX idx_invoices_reservation (reservation_id),
    INDEX idx_invoices_guest (guest_id),
    INDEX idx_invoices_status (status),
    CONSTRAINT fk_invoices_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_invoices_guest       FOREIGN KEY (guest_id)       REFERENCES guests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- invoice_items
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS invoice_items;
CREATE TABLE invoice_items (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    invoice_id  BIGINT          NOT NULL,
    description VARCHAR(500)    NOT NULL,
    quantity    INT             NOT NULL DEFAULT 1,
    unit_price  DECIMAL(10,2)   NOT NULL,
    total_price DECIMAL(10,2)   NOT NULL,
    item_type   ENUM('ROOM','FOOD','SERVICE','TAX','DISCOUNT','OTHER') NOT NULL DEFAULT 'OTHER',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_invoice_items_invoice (invoice_id),
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- housekeeping_tasks
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS housekeeping_tasks;
CREATE TABLE housekeeping_tasks (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    room_id         BIGINT          NOT NULL,
    assigned_to     BIGINT          NULL COMMENT 'FK to staff.id',
    task_type       ENUM('CLEANING','INSPECTION','MAINTENANCE','TURNDOWN') NOT NULL DEFAULT 'CLEANING',
    status          ENUM('PENDING','IN_PROGRESS','COMPLETED','SKIPPED')    NOT NULL DEFAULT 'PENDING',
    priority        ENUM('LOW','NORMAL','HIGH','URGENT')                   NOT NULL DEFAULT 'NORMAL',
    notes           TEXT,
    scheduled_date  DATE            NOT NULL,
    started_at      DATETIME        NULL,
    completed_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_hk_room_status_date (room_id, status, scheduled_date),
    INDEX idx_hk_assigned (assigned_to),
    INDEX idx_hk_scheduled (scheduled_date, status),
    CONSTRAINT fk_hk_room     FOREIGN KEY (room_id)     REFERENCES rooms(id),
    CONSTRAINT fk_hk_staff    FOREIGN KEY (assigned_to) REFERENCES staff(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- refresh_tokens
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS refresh_tokens;
CREATE TABLE refresh_tokens (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    staff_id    BIGINT          NOT NULL,
    token       VARCHAR(512)    NOT NULL,
    family      VARCHAR(36)     NOT NULL COMMENT 'UUID family — all tokens in chain share same family for reuse detection',
    is_revoked  TINYINT(1)      NOT NULL DEFAULT 0,
    expires_at  DATETIME        NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_refresh_token (token),
    INDEX idx_rt_staff (staff_id),
    INDEX idx_rt_family (family),
    CONSTRAINT fk_rt_staff FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- audit_logs
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS audit_logs;
CREATE TABLE audit_logs (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    entity_type VARCHAR(100)    NOT NULL,
    entity_id   BIGINT          NOT NULL,
    action      ENUM('CREATE','UPDATE','DELETE','LOGIN','LOGOUT') NOT NULL,
    changed_by  VARCHAR(150),
    old_values  JSON,
    new_values  JSON,
    ip_address  VARCHAR(50),
    user_agent  VARCHAR(500),
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_changed_by (changed_by),
    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
