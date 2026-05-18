-- =============================================================================
-- Hotel Management System — Stored Procedures
-- =============================================================================

DELIMITER $$

-- -----------------------------------------------------------------------------
-- 1. Check room availability for a date range
-- Returns rooms of the given type that have no overlapping confirmed bookings
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_check_room_availability$$
CREATE PROCEDURE sp_check_room_availability(
    IN  p_hotel_id      BIGINT,
    IN  p_check_in      DATE,
    IN  p_check_out     DATE,
    IN  p_room_type_id  BIGINT
)
BEGIN
    SELECT
        r.id,
        r.room_number,
        r.floor,
        r.status,
        rt.name         AS room_type_name,
        rt.base_price,
        rt.max_occupancy,
        rt.amenities
    FROM rooms r
    JOIN room_types rt ON r.room_type_id = rt.id
    WHERE r.hotel_id     = p_hotel_id
      AND r.deleted_at   IS NULL
      AND r.status       NOT IN ('OUT_OF_ORDER','MAINTENANCE')
      AND (p_room_type_id IS NULL OR r.room_type_id = p_room_type_id)
      AND r.id NOT IN (
          SELECT DISTINCT rr.room_id
          FROM   reservation_rooms rr
          JOIN   reservations res ON rr.reservation_id = res.id
          WHERE  res.status NOT IN ('CANCELLED','NO_SHOW')
            AND  res.deleted_at IS NULL
            AND  rr.check_in_date  < p_check_out
            AND  rr.check_out_date > p_check_in
      )
    ORDER BY r.floor, r.room_number;
END$$

-- -----------------------------------------------------------------------------
-- 2. Generate a unique confirmation number
-- Format: HM-XXXXXX (6 uppercase alphanumeric chars)
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_generate_confirmation_number$$
CREATE PROCEDURE sp_generate_confirmation_number(OUT p_confirmation_number VARCHAR(20))
BEGIN
    DECLARE v_candidate VARCHAR(20);
    DECLARE v_exists    INT DEFAULT 1;
    DECLARE v_chars     VARCHAR(36) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    DECLARE v_result    VARCHAR(6)  DEFAULT '';
    DECLARE i           INT DEFAULT 1;

    WHILE v_exists > 0 DO
        SET v_result = '';
        SET i = 1;
        WHILE i <= 6 DO
            SET v_result = CONCAT(v_result,
                SUBSTRING(v_chars, FLOOR(1 + RAND() * 36), 1));
            SET i = i + 1;
        END WHILE;
        SET v_candidate = CONCAT('HM-', v_result);
        SELECT COUNT(*) INTO v_exists
        FROM reservations
        WHERE confirmation_number = v_candidate;
    END WHILE;

    SET p_confirmation_number = v_candidate;
END$$

-- -----------------------------------------------------------------------------
-- 3. Recalculate and update reservation total amount
-- Sum of (rate_per_night × nights) for all reservation_rooms
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_calculate_reservation_total$$
CREATE PROCEDURE sp_calculate_reservation_total(IN p_reservation_id BIGINT)
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0.00;

    SELECT COALESCE(SUM(
        rr.rate_per_night *
        DATEDIFF(rr.check_out_date, rr.check_in_date)
    ), 0.00)
    INTO v_total
    FROM reservation_rooms rr
    WHERE rr.reservation_id = p_reservation_id;

    UPDATE reservations
    SET total_amount = v_total,
        updated_at   = NOW()
    WHERE id = p_reservation_id;

    SELECT v_total AS total_amount;
END$$

-- -----------------------------------------------------------------------------
-- 4. Get daily occupancy statistics for a hotel over a date range
-- Returns: date, total_rooms, occupied_rooms, occupancy_pct
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_occupancy_stats$$
CREATE PROCEDURE sp_get_occupancy_stats(
    IN p_hotel_id   BIGINT,
    IN p_start_date DATE,
    IN p_end_date   DATE
)
BEGIN
    -- Uses a date dimension generated inline
    WITH RECURSIVE date_series AS (
        SELECT p_start_date AS dt
        UNION ALL
        SELECT DATE_ADD(dt, INTERVAL 1 DAY)
        FROM date_series
        WHERE dt < p_end_date
    ),
    total_rooms AS (
        SELECT COUNT(*) AS cnt
        FROM rooms
        WHERE hotel_id  = p_hotel_id
          AND deleted_at IS NULL
          AND status     NOT IN ('OUT_OF_ORDER','MAINTENANCE')
    )
    SELECT
        ds.dt                                                               AS occupancy_date,
        tr.cnt                                                              AS total_rooms,
        COUNT(DISTINCT rr.room_id)                                          AS occupied_rooms,
        ROUND(COUNT(DISTINCT rr.room_id) / NULLIF(tr.cnt,0) * 100, 2)     AS occupancy_pct,
        COALESCE(SUM(rr.rate_per_night), 0)                                AS room_revenue,
        ROUND(COALESCE(SUM(rr.rate_per_night),0) / NULLIF(tr.cnt,0), 2)   AS revpar
    FROM date_series ds
    CROSS JOIN total_rooms tr
    LEFT JOIN reservation_rooms rr
        ON  rr.check_in_date  <= ds.dt
        AND rr.check_out_date  > ds.dt
        AND rr.reservation_id IN (
            SELECT id FROM reservations
            WHERE hotel_id  = p_hotel_id
              AND status    NOT IN ('CANCELLED','NO_SHOW')
              AND deleted_at IS NULL
        )
    GROUP BY ds.dt, tr.cnt
    ORDER BY ds.dt;
END$$

-- -----------------------------------------------------------------------------
-- 5. Auto-assign housekeeping tasks for checked-out rooms on a given date
-- Creates CLEANING tasks for all rooms whose reservations checked out today
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_assign_housekeeping_tasks$$
CREATE PROCEDURE sp_assign_housekeeping_tasks(
    IN p_hotel_id   BIGINT,
    IN p_date       DATE
)
BEGIN
    DECLARE v_count INT DEFAULT 0;

    INSERT INTO housekeeping_tasks (room_id, task_type, status, priority, notes, scheduled_date, created_at, updated_at)
    SELECT DISTINCT
        rr.room_id,
        'CLEANING',
        'PENDING',
        'HIGH',
        CONCAT('Post-checkout cleaning — reservation #',
               res.confirmation_number, ' checked out'),
        p_date,
        NOW(),
        NOW()
    FROM reservation_rooms rr
    JOIN reservations res ON rr.reservation_id = res.id
    WHERE res.hotel_id       = p_hotel_id
      AND rr.check_out_date  = p_date
      AND res.status         = 'CHECKED_OUT'
      -- Skip rooms that already have a pending cleaning task for today
      AND rr.room_id NOT IN (
          SELECT room_id
          FROM   housekeeping_tasks
          WHERE  scheduled_date = p_date
            AND  task_type      = 'CLEANING'
            AND  status         IN ('PENDING','IN_PROGRESS')
      );

    SET v_count = ROW_COUNT();
    SELECT v_count AS tasks_created;
END$$

-- -----------------------------------------------------------------------------
-- 6. Process checkout: update reservation + room status + trigger housekeeping
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_process_checkout$$
CREATE PROCEDURE sp_process_checkout(
    IN p_reservation_id BIGINT,
    IN p_staff_id       BIGINT
)
BEGIN
    DECLARE v_hotel_id  BIGINT;
    DECLARE v_exit      TINYINT DEFAULT 0;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET v_exit = 1;

    START TRANSACTION;

    -- Validate reservation is CHECKED_IN
    SELECT hotel_id INTO v_hotel_id
    FROM reservations
    WHERE id = p_reservation_id AND status = 'CHECKED_IN'
    FOR UPDATE;

    IF v_hotel_id IS NULL OR v_exit = 1 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Reservation not found or not in CHECKED_IN status';
    END IF;

    -- Mark reservation CHECKED_OUT
    UPDATE reservations
    SET status      = 'CHECKED_OUT',
        updated_at  = NOW()
    WHERE id = p_reservation_id;

    -- Update actual_check_out timestamp
    UPDATE reservation_rooms
    SET actual_check_out = NOW(),
        updated_at       = NOW()
    WHERE reservation_id = p_reservation_id
      AND actual_check_out IS NULL;

    -- Mark rooms as DIRTY
    UPDATE rooms r
    JOIN reservation_rooms rr ON r.id = rr.room_id
    SET r.status     = 'DIRTY',
        r.updated_at = NOW()
    WHERE rr.reservation_id = p_reservation_id;

    -- Create housekeeping CLEANING tasks
    INSERT INTO housekeeping_tasks (room_id, assigned_to, task_type, status, priority, notes, scheduled_date, created_at, updated_at)
    SELECT DISTINCT
        rr.room_id,
        NULL,
        'CLEANING',
        'PENDING',
        'HIGH',
        CONCAT('Post-checkout: reservation #', res.confirmation_number),
        CURDATE(),
        NOW(),
        NOW()
    FROM reservation_rooms rr
    JOIN reservations res ON rr.reservation_id = res.id
    WHERE rr.reservation_id = p_reservation_id;

    -- Audit log
    INSERT INTO audit_logs (entity_type, entity_id, action, changed_by, new_values, created_at)
    VALUES ('RESERVATION', p_reservation_id, 'UPDATE',
            (SELECT CONCAT(first_name,' ',last_name) FROM staff WHERE id = p_staff_id),
            JSON_OBJECT('status','CHECKED_OUT','processed_by', p_staff_id),
            NOW());

    COMMIT;
    SELECT 'OK' AS result, p_reservation_id AS reservation_id;
END$$

DELIMITER ;
