-- =============================================================================
-- Hotel Management System — Seed Data
-- Run AFTER schema.sql
-- Passwords are BCrypt hashes of 'Password123!' (strength 12) — change in prod
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- Roles
-- -----------------------------------------------------------------------------
INSERT INTO roles (id, name, description) VALUES
(1, 'SUPER_ADMIN',   'Full system access — manage all modules and staff'),
(2, 'RECEPTIONIST',  'Front-desk operations: reservations, check-in/out, billing'),
(3, 'HOUSEKEEPING',  'Room cleaning status and task management'),
(4, 'GUEST',         'Self-service guest portal access');

-- -----------------------------------------------------------------------------
-- Permissions
-- -----------------------------------------------------------------------------
INSERT INTO permissions (id, name, description, resource, action) VALUES
(1,  'rooms:read',          'View rooms and room types',          'rooms',         'read'),
(2,  'rooms:write',         'Create and update rooms',            'rooms',         'write'),
(3,  'rooms:delete',        'Delete rooms',                       'rooms',         'delete'),
(4,  'reservations:read',   'View reservations',                  'reservations',  'read'),
(5,  'reservations:write',  'Create and update reservations',     'reservations',  'write'),
(6,  'reservations:delete', 'Cancel/delete reservations',         'reservations',  'delete'),
(7,  'guests:read',         'View guest profiles',                'guests',        'read'),
(8,  'guests:write',        'Create and update guest profiles',   'guests',        'write'),
(9,  'billing:read',        'View invoices and payments',         'billing',       'read'),
(10, 'billing:write',       'Create invoices and process payments','billing',      'write'),
(11, 'housekeeping:read',   'View housekeeping tasks',            'housekeeping',  'read'),
(12, 'housekeeping:write',  'Create and update housekeeping tasks','housekeeping', 'write'),
(13, 'reports:read',        'View analytics and reports',         'reports',       'read'),
(14, 'admin:access',        'Access admin settings',              'admin',         'access'),
(15, 'staff:manage',        'Manage staff accounts',              'staff',         'manage');

-- -----------------------------------------------------------------------------
-- Role ↔ Permission mappings
-- SUPER_ADMIN: all permissions
-- RECEPTIONIST: rooms/reservations/guests/billing (read+write), reports:read
-- HOUSEKEEPING: rooms:read, housekeeping (read+write)
-- GUEST: reservations:read+write, billing:read
-- -----------------------------------------------------------------------------
-- SUPER_ADMIN (role 1) — all 15
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- RECEPTIONIST (role 2)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2,1),(2,2),(2,4),(2,5),(2,6),(2,7),(2,8),(2,9),(2,10),(2,11),(2,13);

-- HOUSEKEEPING (role 3)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3,1),(3,11),(3,12);

-- GUEST (role 4)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4,4),(4,5),(4,9);

-- -----------------------------------------------------------------------------
-- Sample Hotel
-- -----------------------------------------------------------------------------
INSERT INTO hotels (id, name, address, city, state, country, postal_code, phone, email,
                    star_rating, description, check_in_time, check_out_time, timezone, is_active)
VALUES (1,
        'Grand Horizon Hotel',
        '123 Ocean Boulevard',
        'Miami',
        'FL',
        'USA',
        '33101',
        '+1-305-555-0100',
        'info@grandhorizon.com',
        5,
        'Luxury beachfront hotel offering world-class amenities, panoramic ocean views, and exceptional service.',
        '15:00:00',
        '11:00:00',
        'America/New_York',
        1);

-- -----------------------------------------------------------------------------
-- Room Types
-- -----------------------------------------------------------------------------
INSERT INTO room_types (id, hotel_id, name, description, max_occupancy, base_price, amenities, image_urls)
VALUES
(1, 1, 'Standard Single',
 'Comfortable single room with city view, perfect for business travelers.',
 1, 89.00,
 '["Free WiFi","Air Conditioning","Flat-screen TV","Mini Fridge","Safe","Hairdryer"]',
 '["/images/rooms/standard-single-1.jpg","/images/rooms/standard-single-2.jpg"]'),

(2, 1, 'Standard Double',
 'Spacious double room with queen bed, ideal for couples.',
 2, 129.00,
 '["Free WiFi","Air Conditioning","Flat-screen TV","Mini Bar","Safe","Hairdryer","Bathtub"]',
 '["/images/rooms/standard-double-1.jpg","/images/rooms/standard-double-2.jpg"]'),

(3, 1, 'Deluxe Suite',
 'Elegant suite with separate living area and ocean-view balcony.',
 3, 299.00,
 '["Free WiFi","Air Conditioning","55-inch Smart TV","Full Bar","In-room Dining","Jacuzzi","Balcony","Butler Service","Nespresso Machine"]',
 '["/images/rooms/deluxe-suite-1.jpg","/images/rooms/deluxe-suite-2.jpg"]'),

(4, 1, 'Presidential Suite',
 'Our most exclusive offering — panoramic ocean views, private terrace, and dedicated butler.',
 6, 999.00,
 '["Free WiFi","Air Conditioning","75-inch Smart TV","Full Bar","Private Kitchen","Jacuzzi","Private Terrace","24/7 Butler","Rolls-Royce Transfer","Private Pool Access"]',
 '["/images/rooms/presidential-1.jpg","/images/rooms/presidential-2.jpg"]');

-- -----------------------------------------------------------------------------
-- Rooms (20 rooms across 4 floors)
-- -----------------------------------------------------------------------------
INSERT INTO rooms (hotel_id, room_type_id, room_number, floor, status) VALUES
-- Floor 1: Standard Singles
(1, 1, '101', 1, 'AVAILABLE'), (1, 1, '102', 1, 'AVAILABLE'),
(1, 1, '103', 1, 'DIRTY'),     (1, 1, '104', 1, 'AVAILABLE'),
-- Floor 2: Standard Doubles
(1, 2, '201', 2, 'AVAILABLE'), (1, 2, '202', 2, 'OCCUPIED'),
(1, 2, '203', 2, 'AVAILABLE'), (1, 2, '204', 2, 'CLEAN'),
(1, 2, '205', 2, 'AVAILABLE'),
-- Floor 3: More Doubles + Singles
(1, 2, '301', 3, 'AVAILABLE'), (1, 2, '302', 3, 'AVAILABLE'),
(1, 1, '303', 3, 'INSPECTED'), (1, 1, '304', 3, 'AVAILABLE'),
(1, 2, '305', 3, 'MAINTENANCE'),
-- Floor 4: Suites
(1, 3, '401', 4, 'AVAILABLE'), (1, 3, '402', 4, 'AVAILABLE'),
(1, 3, '403', 4, 'OCCUPIED'),
-- Penthouse
(1, 4, '501', 5, 'AVAILABLE'), (1, 4, '502', 5, 'AVAILABLE'),
(1, 4, '503', 5, 'OUT_OF_ORDER');

-- -----------------------------------------------------------------------------
-- Staff
-- All passwords are BCrypt of 'Password123!' — CHANGE IN PRODUCTION
-- -----------------------------------------------------------------------------
INSERT INTO staff (hotel_id, role_id, first_name, last_name, email, password_hash, phone, is_active)
VALUES
(1, 1, 'Admin',    'User',      'admin@grandhorizon.com',
 '$2b$12$p5C7GQeZt5PsVrEVB1IVAuA5td5H6IRUjKkcnUy4cEIHKm74aWMqq',
 '+1-305-555-0001', 1),
(1, 2, 'Sarah',    'Johnson',   'sarah.johnson@grandhorizon.com',
 '$2b$12$p5C7GQeZt5PsVrEVB1IVAuA5td5H6IRUjKkcnUy4cEIHKm74aWMqq',
 '+1-305-555-0002', 1),
(1, 3, 'Miguel',   'Rodriguez', 'miguel.r@grandhorizon.com',
 '$2b$12$p5C7GQeZt5PsVrEVB1IVAuA5td5H6IRUjKkcnUy4cEIHKm74aWMqq',
 '+1-305-555-0003', 1);

-- -----------------------------------------------------------------------------
-- Sample Guests
-- -----------------------------------------------------------------------------
INSERT INTO guests (first_name, last_name, email, phone, nationality, id_type, id_number,
                    loyalty_points, loyalty_tier, city, country, is_verified)
VALUES
('James',   'Morrison',  'james.morrison@email.com',  '+1-212-555-1001', 'American',   'PASSPORT',       'US123456789', 2500, 'SILVER',   'New York',    'USA',      1),
('Emma',    'Clarke',    'emma.clarke@email.com',      '+44-20-555-2002', 'British',    'PASSPORT',       'GB987654321', 8750, 'GOLD',     'London',      'UK',       1),
('Hiroshi', 'Tanaka',    'h.tanaka@email.com',         '+81-3-555-3003',  'Japanese',   'PASSPORT',       'JP112233445', 500,  'BRONZE',   'Tokyo',       'Japan',    1),
('Sofia',   'Hernandez', 'sofia.h@email.com',          '+34-91-555-4004', 'Spanish',    'NATIONAL_ID',    'ESP-55667788',150,  'BRONZE',   'Madrid',      'Spain',    0),
('Michael', 'Chen',      'michael.chen@email.com',     '+1-415-555-5005', 'American',   'DRIVERS_LICENSE','CA-DL-99887', 15200,'PLATINUM', 'San Francisco','USA',     1);

-- -----------------------------------------------------------------------------
-- Sample Reservations
-- -----------------------------------------------------------------------------
INSERT INTO reservations (hotel_id, guest_id, confirmation_number, status,
                          check_in_date, check_out_date, adults, children,
                          total_amount, paid_amount, source, cancellation_policy)
VALUES
(1, 1, 'HM-A1B2C3', 'CONFIRMED',   '2026-05-25', '2026-05-28', 2, 0,  387.00, 200.00, 'DIRECT',   'MODERATE'),
(1, 2, 'HM-D4E5F6', 'CHECKED_IN',  '2026-05-18', '2026-05-21', 1, 0,  897.00, 897.00, 'OTA',      'FLEXIBLE'),
(1, 5, 'HM-G7H8I9', 'CHECKED_OUT', '2026-05-15', '2026-05-17', 2, 1, 1998.00,1998.00, 'DIRECT',   'STRICT');

-- Link reservations to rooms
INSERT INTO reservation_rooms (reservation_id, room_id, room_type_id, rate_per_night,
                                check_in_date, check_out_date)
VALUES
(1, 5,  2, 129.00, '2026-05-25', '2026-05-28'),
(2, 15, 3, 299.00, '2026-05-18', '2026-05-21'),
(3, 17, 3, 299.00, '2026-05-15', '2026-05-17');

UPDATE reservation_rooms
SET actual_check_in  = '2026-05-18 14:30:00',
    actual_check_out = NULL
WHERE reservation_id = 2;

UPDATE reservation_rooms
SET actual_check_in  = '2026-05-15 15:10:00',
    actual_check_out = '2026-05-17 10:45:00'
WHERE reservation_id = 3;

-- Sample invoice for checked-out reservation
INSERT INTO invoices (reservation_id, guest_id, invoice_number, status,
                      subtotal, tax_rate, tax_amount, total_amount,
                      issued_at, paid_at)
VALUES (3, 5, 'INV-2026-0001', 'PAID', 1816.36, 10.00, 181.64, 1998.00,
        '2026-05-17 10:00:00', '2026-05-17 10:45:00');

INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, total_price, item_type)
VALUES
(1, 'Deluxe Suite — 2 nights (May 15–17 2026)', 2, 299.00, 598.00, 'ROOM'),
(1, 'Room Service (Dinner × 2)',                 1, 185.00, 185.00, 'FOOD'),
(1, 'Spa Treatment',                             1,  90.00,  90.00, 'SERVICE'),
(1, 'Tax (10%)',                                 1, 181.64, 181.64, 'TAX');

-- Sample payment
INSERT INTO payments (reservation_id, amount, payment_method, payment_status,
                      transaction_id, idempotency_key, paid_at)
VALUES (3, 1998.00, 'CREDIT_CARD', 'COMPLETED',
        'TXN-CC-20260517-001', 'idmp-res3-final-payment', '2026-05-17 10:45:00');

SET FOREIGN_KEY_CHECKS = 1;
