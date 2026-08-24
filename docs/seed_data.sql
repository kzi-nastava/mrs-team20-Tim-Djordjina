-- =====================================================================
-- RideOn — Seed data for the defense  (schema-verified)
-- =====================================================================
-- Run AFTER the backend has started once (so the schema exists):
--     psql -d ride_on_db -f seed_data.sql
--
-- DEMO LOGINS (all use password:  password )
--   rideonacc123+admin@gmail.com     ADMIN
--   rideonacc123+rider1@gmail.com    USER    (Marko Markovic)
--   rideonacc123+rider2@gmail.com    USER    (Jovana Jovanovic)
--   rideonacc123+driver1@gmail.com   DRIVER  (Nikola Nikolic  — NS-001-AA, STANDARD)
--   rideonacc123+driver2@gmail.com   DRIVER  (Ana Anic        — NS-002-BB, LUXURY)
--
-- Password hash below is BCrypt for the text "password".
-- =====================================================================

-- ---------------------------------------------------------------------
-- OPTIONAL RESET (uncomment one option before re-seeding)
-- ---------------------------------------------------------------------
-- Option A — wipe ALL data in every table and reset id counters:
TRUNCATE TABLE
  drivers, favourite_route_stops, favourite_routes, inconsistency_reports,
  linked_passengers, notifications, panics, pricing_config,
  profile_change_requests, ratings, ride_stops, rides, users, vehicles
  RESTART IDENTITY CASCADE;

SELECT setval(pg_get_serial_sequence('drivers', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('favourite_route_stops', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('favourite_routes', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('inconsistency_reports', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('linked_passengers', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('notifications', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('panics', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('pricing_config', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('profile_change_requests', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('ratings', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('ride_stops', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('rides', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('users', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('vehicles', 'id'), 1, false);

--
-- Option B — remove only the demo rows (FK-safe order):
-- DELETE FROM panics;
-- DELETE FROM ratings;
-- DELETE FROM inconsistency_reports;
-- DELETE FROM linked_passengers;
-- DELETE FROM ride_stops;
-- DELETE FROM notifications;
-- DELETE FROM rides;
-- DELETE FROM vehicles;
-- DELETE FROM drivers;
-- DELETE FROM users WHERE email IN
--   ('rideonacc123+admin@gmail.com','rideonacc123+rider1@gmail.com',
--    'rideonacc123+rider2@gmail.com','rideonacc123+driver1@gmail.com',
--    'rideonacc123+driver2@gmail.com');

-- ---------------------------------------------------------------------
-- 1) USERS   (password = "password")
--    Required: first_name,last_name,email,password_hash,phone_number,
--              address,role,is_activated,is_blocked,created_at,updated_at
-- ---------------------------------------------------------------------
INSERT INTO users
  (first_name, last_name, email, password_hash, phone_number, address, role, is_activated, is_blocked, created_at, updated_at)
VALUES
 ('Admin','User','rideonacc123+admin@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','+381600000000','Novi Sad','ADMIN',  true,false, NOW(), NOW()),
 ('Marko','Markovic','rideonacc123+rider1@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','+381600000001','Novi Sad','USER',  true,false, NOW(), NOW()),
 ('Jovana','Jovanovic','rideonacc123+rider2@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','+381600000002','Novi Sad','USER', true,false, NOW(), NOW()),
 ('Nikola','Nikolic','rideonacc123+driver1@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','+381600000003','Novi Sad','DRIVER',true,false, NOW(), NOW()),
 ('Ana','Anic','rideonacc123+driver2@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','+381600000004','Novi Sad','DRIVER',true,false, NOW(), NOW());

-- ---------------------------------------------------------------------
-- 2) DRIVERS
--    Required: user_id,is_active,is_available,is_logged_in,has_active_ride,
--              has_pending_profile_changes,working_minutes_last24hours,
--              created_at,updated_at
-- ---------------------------------------------------------------------
INSERT INTO drivers
  (user_id, is_active, is_available, is_logged_in, has_active_ride, has_pending_profile_changes, working_minutes_last24hours, created_at, updated_at)
VALUES
 ((SELECT id FROM users WHERE email='rideonacc123+driver1@gmail.com'), true, true, true, false, false, 0, NOW(), NOW()),
 ((SELECT id FROM users WHERE email='rideonacc123+driver2@gmail.com'), true, true, true, false, false, 0, NOW(), NOW());

-- ---------------------------------------------------------------------
-- 3) VEHICLES   (WITH coordinates — required for maps & ETA)
--    Required: driver_id,model,vehicle_type,license_plate,seats,
--              baby_transport,pet_transport,created_at,updated_at
-- ---------------------------------------------------------------------
INSERT INTO vehicles
  (driver_id, model, vehicle_type, license_plate, seats, baby_transport, pet_transport, current_latitude, current_longitude, created_at, updated_at)
VALUES
 ((SELECT id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='rideonacc123+driver1@gmail.com')),
   'Skoda Octavia','STANDARD','NS-001-AA',4,true,false,45.2671,19.8335, NOW(), NOW()),
 ((SELECT id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='rideonacc123+driver2@gmail.com')),
   'Audi A6','LUXURY','NS-002-BB',4,false,true,45.2550,19.8450, NOW(), NOW());

-- ---------------------------------------------------------------------
-- 4) RIDES + child rows
--    Note the distance column is "distancekm" (no underscore).
--    Required on rides: rider_id,baby_transport,pet_transport,distancekm,fare
--    (we also set status/addresses/times for meaningful demo data)
-- ---------------------------------------------------------------------

-- 4a) Finished ride (rider1 + driver1) WITH a rating
WITH r AS (
  INSERT INTO rides
    (rider_id, driver_id, pickup_address, pickup_latitude, pickup_longitude,
     destination_address, destination_latitude, destination_longitude,
     vehicle_type, baby_transport, pet_transport, distancekm, fare, status,
     created_at, started_at, finished_at)
  VALUES
    ((SELECT id FROM users WHERE email='rideonacc123+rider1@gmail.com'),
     (SELECT id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='rideonacc123+driver1@gmail.com')),
     'Trg slobode', 45.2671, 19.8335,
     'Strand', 45.2400, 19.8500,
     'STANDARD', false, false, 3.2, 584.00, 'FINISHED',
     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '3 min',
     NOW() - INTERVAL '2 days' + INTERVAL '18 min')
  RETURNING id
)
INSERT INTO ratings (ride_id, rater_id, driver_rating, vehicle_rating, created_at)
SELECT r.id, (SELECT id FROM users WHERE email='rideonacc123+rider1@gmail.com'), 5, 4,
       NOW() - INTERVAL '2 days' + INTERVAL '20 min'
FROM r;

-- 4b) Finished ride (rider2 + driver2), no child rows
INSERT INTO rides
  (rider_id, driver_id, pickup_address, pickup_latitude, pickup_longitude,
   destination_address, destination_latitude, destination_longitude,
   vehicle_type, baby_transport, pet_transport, distancekm, fare, status,
   created_at, started_at, finished_at)
VALUES
  ((SELECT id FROM users WHERE email='rideonacc123+rider2@gmail.com'),
   (SELECT id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='rideonacc123+driver2@gmail.com')),
   'Bulevar oslobodjenja', 45.2450, 19.8480,
   'Petrovaradin', 45.2520, 19.8620,
   'LUXURY', false, true, 4.1, 992.00, 'FINISHED',
   NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '2 min',
   NOW() - INTERVAL '1 day' + INTERVAL '20 min');

-- 4c) Finished ride (rider1 + driver1) WITH a panic
WITH r AS (
  INSERT INTO rides
    (rider_id, driver_id, pickup_address, pickup_latitude, pickup_longitude,
     destination_address, destination_latitude, destination_longitude,
     vehicle_type, baby_transport, pet_transport, distancekm, fare, status,
     created_at, started_at, finished_at)
  VALUES
    ((SELECT id FROM users WHERE email='rideonacc123+rider1@gmail.com'),
     (SELECT id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='rideonacc123+driver1@gmail.com')),
     'Zeleznicka stanica', 45.2660, 19.8290,
     'Liman', 45.2400, 19.8380,
     'STANDARD', false, false, 2.6, 512.00, 'FINISHED',
     NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours' + INTERVAL '3 min',
     NOW() - INTERVAL '6 hours' + INTERVAL '15 min')
  RETURNING id
)
INSERT INTO panics (ride_id, triggered_by_id, note, created_at)
SELECT r.id, (SELECT id FROM users WHERE email='rideonacc123+rider1@gmail.com'),
       'Driver took an unusual route', NOW() - INTERVAL '6 hours' + INTERVAL '8 min'
FROM r;

-- 4d) Cancelled ride (rider2), cancelled by the rider
INSERT INTO rides
  (rider_id, driver_id, pickup_address, pickup_latitude, pickup_longitude,
   destination_address, destination_latitude, destination_longitude,
   vehicle_type, baby_transport, pet_transport, distancekm, fare, status,
   created_at, cancelled_by, cancel_reason)
VALUES
  ((SELECT id FROM users WHERE email='rideonacc123+rider2@gmail.com'),
   (SELECT id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='rideonacc123+driver2@gmail.com')),
   'Futoska', 45.2480, 19.8300,
   'Detelinara', 45.2600, 19.8200,
   'STANDARD', false, false, 3.0, 560.00, 'CANCELLED',
   NOW() - INTERVAL '3 hours', 'RIDER', 'Changed plans');

-- =====================================================================
-- Verify:
--   SELECT email, role FROM users;
--   SELECT status, fare, cancelled_by FROM rides ORDER BY created_at DESC;
--   SELECT license_plate, current_latitude, current_longitude FROM vehicles;
-- =====================================================================
